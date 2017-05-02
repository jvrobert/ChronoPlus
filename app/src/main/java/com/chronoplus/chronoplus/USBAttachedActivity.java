package com.chronoplus.chronoplus;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.Set;

public class USBAttachedActivity extends AppCompatActivity {
    private static final String TAG = "USBAttached";
    private ChronoService usbService;
    private UsbDevice device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     //   setContentView(R.layout.activity_usbattached);
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!ChronoService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((ChronoService.UsbBinder) arg1).getService();
            if (device != null) {
                usbService.attachDevice(device);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };


    @Override
    public void onPause() {
        super.onPause();
        unbindService(usbConnection);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.i(TAG, "USB CALL!!!!!!");
        Intent intent = getIntent();

        if (intent == null) {
            return;
        }

        if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
            startService(ChronoService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
            Parcelable usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            device = (UsbDevice) usbDevice;
            if (usbService != null) {
                usbService.attachDevice(device);
            }
        }
        if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
            Log.i(TAG, "USB DETACHED!!!!");
        }
//
//                // Create a new intent and put the usb device in as an extra
//                Intent broadcastIntent = new Intent(ACTION_USB_DEVICE_ATTACHED);
//                broadcastIntent.putExtra(UsbManager.EXTRA_DEVICE, usbDevice);
//
//                // Broadcast this event so we can receive it
//                sendBroadcast(broadcastIntent);
//            }
//        }

        // Close the activity
        finish();
    }
}
