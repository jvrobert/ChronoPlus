package com.chronoplus.chronoplus;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Binder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.felhr.usbserial.CDCSerialDevice;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jvrobert on 4/28/2017.
 */

public class ChronoService extends Service {
    private static final String TAG = "ChronoService";
    public static final int SHOT_FPS = 0;
    public static final int CTS_CHANGE = 1;
    public static final int DSR_CHANGE = 2;
    public static final int STATUS_AVAILABLE = 3;
    private static final int BAUD_RATE = 9600;
    public static boolean SERVICE_CONNECTED = false;

    private boolean serialPortConnected;
    private Handler toastHandler;


    private Handler mHandler;
    private IBinder binder = new UsbBinder();
    private UsbManager usbManager;
    private UsbDeviceConnection usbConnection;
    private UsbSerialDevice serialPort;

    private void setFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(usbReceiver, filter);
    }

    protected void toast(final String msg) {
        toastHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ChronoService.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    /*
     * onCreate will be executed when service is started. It configures an IntentFilter to listen for
     * incoming Intents (USB ATTACHED, USB DETACHED...) and it tries to open a serial port.
     */
    @Override
    public void onCreate() {
        toastHandler = new Handler();
        serialPortConnected = false;
        ChronoService.SERVICE_CONNECTED = true;
        setFilter();
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class UsbBinder extends Binder {
        public ChronoService getService() {
            return ChronoService.this;
        }
    }

    /*
     * Different notifications from OS will be received here (USB attached, detached, permission responses...)
     * About BroadcastReceiver: http://developer.android.com/reference/android/content/BroadcastReceiver.html
     */
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if (arg1.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
               closeUsb();
            }
        }
    };

    private void closeUsb() {
        if (serialPortConnected) {
            serialPort.close();
            if (usbConnection != null) {
                usbConnection.close();
                usbConnection = null;
            }
            serialPortConnected = false;
            Log.i(TAG, "Disconnected, seing disconnect signal");
            if (mHandler != null) {
                mHandler.obtainMessage(STATUS_AVAILABLE, false).sendToTarget();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ChronoService.SERVICE_CONNECTED = false;
    }

    public void setHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public boolean isAvailable() {
        return serialPortConnected;
    }

    private StringBuffer buffer = new StringBuffer();
    public final static double DISTANCE_FEET = 2.353 / 12;
    public final static double TIME_FACTOR = .000000248993;
    private long lastUpdate = 0;

    /*
     *  Data received from serial port will be received here. Just populate onReceivedData with your code
     *  In this particular example. byte stream is converted to String and send to UI thread to
     *  be treated there.
     */
    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] arg0) {
            try {
                long now = System.currentTimeMillis();
                if (lastUpdate != 0 && now - lastUpdate > 500) {
                    buffer.setLength(0);
                }
                String data = new String(arg0, "UTF-8");
                for (char c : data.toCharArray()) {
                    if (c == '\r' || c == '\n' || (c >= '0' && c <= '9')) {
                        buffer.append(c);
                    }
                }
                int idx = buffer.indexOf("\r\n");
                if (idx != -1) {
                    try {
                        int val = Integer.parseInt(buffer.substring(0, buffer.length() - 2));
                        int fps = (int)(DISTANCE_FEET / (((double)val) * TIME_FACTOR));
                        if (mHandler != null)
                            mHandler.obtainMessage(SHOT_FPS, fps).sendToTarget();
                        Log.i(TAG, "FPS: " + val + "  -> " + fps);
                    }
                    catch (NumberFormatException ex) {
                        Log.i(TAG, "Got odd message: " + buffer);
                        buffer.setLength(0);
                        return;
                    }
                    buffer.setLength(0);
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };

    /*
    * State changes in the CTS line will be received here
    */
    private UsbSerialInterface.UsbCTSCallback ctsCallback = new UsbSerialInterface.UsbCTSCallback() {
        @Override
        public void onCTSChanged(boolean state) {
            if (mHandler != null)
                mHandler.obtainMessage(CTS_CHANGE).sendToTarget();
        }
    };

    /*
     * State changes in the DSR line will be received here
     */
    private UsbSerialInterface.UsbDSRCallback dsrCallback = new UsbSerialInterface.UsbDSRCallback() {
        @Override
        public void onDSRChanged(boolean state) {
            if (mHandler != null)
                mHandler.obtainMessage(DSR_CHANGE).sendToTarget();
        }
    };

    public void attachDevice(UsbDevice dev) {
        if (! serialPortConnected) {
            new AttachThread(dev).start();
        }
    }

    private class AttachThread extends Thread {
        UsbDevice device;

        public AttachThread(UsbDevice dev) {
            device = dev;
        }

        @Override
        public void run() {
            usbConnection = usbManager.openDevice(device);
            serialPort = UsbSerialDevice.createUsbSerialDevice(device, usbConnection);
            if (serialPort.open()) {
                serialPortConnected = true;

                serialPort.setBaudRate(BAUD_RATE);
                serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                serialPort.read(mCallback);
                serialPort.getCTS(ctsCallback);
                serialPort.getDSR(dsrCallback);
                Log.i(TAG, "Opened serialPort OK!");
//                Intent intent = new Intent(ACTION_CHRONO_CONNECTED);
//                sendBroadcast(intent);
                if (mHandler != null) {
                    mHandler.obtainMessage(STATUS_AVAILABLE, true).sendToTarget();
                }
                toast("Combro connected.");
            } else {
                Log.e(TAG, "ERROR IN USB TODO");
            }
        }
    }
}
