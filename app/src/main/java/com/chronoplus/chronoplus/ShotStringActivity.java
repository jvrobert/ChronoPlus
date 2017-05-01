package com.chronoplus.chronoplus;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Set;

import me.grantland.widget.AutofitHelper;

public class ShotStringActivity extends AppCompatActivity {
    private static final String TAG = "ShotStringActivity";
    public final static double DISTANCE_FEET = 2.353 / 12;
    public final static double TIME_FACTOR = .000000248993;
    private DataAccess da;
    private long id;
    private ShotString ss_rec;
    private TextView fps;
    private ChronoService usbService;
    private MyHandler mHandler;
    private ImageView statusIcon;
    private TextView statusText;

    protected void setStyles() {
        fps = (TextView) findViewById(R.id.fps);
        Typeface typeface= Typeface.createFromAsset(getAssets(), "fonts/DSEG7Modern-Regular.ttf");
        fps.setTypeface(typeface);
        AutofitHelper hlp = AutofitHelper.create(fps);
        hlp.setEnabled(true);
        hlp.setMaxLines(1);
        hlp.setMaxTextSize(4000);
        hlp.setMinTextSize(50);

        TextView fpslabel = (TextView) findViewById(R.id.fpslabel);
        AutofitHelper hlp2 = AutofitHelper.create(fpslabel);
        hlp2.setEnabled(true);
        hlp2.setMaxLines(1);
        hlp2.setMaxTextSize(4000);
        hlp2.setMinTextSize(5);
    }

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((ChronoService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(ChronoService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
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

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ChronoService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case ChronoService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case ChronoService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case ChronoService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case ChronoService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ChronoService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(ChronoService.ACTION_NO_USB);
        filter.addAction(ChronoService.ACTION_USB_DISCONNECTED);
        filter.addAction(ChronoService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(ChronoService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shot_string);
        da = DataAccess.getInstance(this);
        id = getIntent().getLongExtra(MainActivity.STRING_ID, -1);
        ss_rec = da.getString(id);
        statusIcon = (ImageView)findViewById(R.id.statusIcon);
        statusText = (TextView)findViewById(R.id.usb_status);
        setStyles();
    }
    private void setCombroStatus(boolean avail) {
        if (avail) {
            statusIcon.setImageResource(R.drawable.bad);
            statusText.setText("Combro not connected.");
        }
        else {
            statusIcon.setImageResource(R.drawable.ok);
            statusText.setText("Combro connected.");
        }
    }

    private static class MyHandler extends Handler {
        private static StringBuffer msgBuf = new StringBuffer();
        private static long lastUpdate = 0;
        private final WeakReference<ShotStringActivity> mActivity;

        public MyHandler(ShotStringActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ChronoService.STATUS_AVAILABLE:
                    mActivity.get().setCombroStatus((boolean)msg.obj);
                    Log.i(TAG, "STATUS AVAIL:: "  + msg.obj);
                    break;
                case ChronoService.MESSAGE_FROM_SERIAL_PORT:
                    Log.i(TAG, "MSG");
                    String data = (String) msg.obj;
                    long now = System.currentTimeMillis();
                    if (lastUpdate != 0 && now - lastUpdate > 500) {
                        msgBuf.setLength(0);
                    }
                    msgBuf.append(data);
                    int idx = msgBuf.indexOf("\r\n");
                    if (idx != -1) {
                        try {
                            int val = Integer.parseInt(msgBuf.substring(0, msgBuf.length() - 2));
                            int fps = (int)(DISTANCE_FEET / (((double)val) * TIME_FACTOR));
                            Log.i(TAG, "FPS: " + val + "  -> " + fps);
                        }
                        catch (NumberFormatException ex) {
                            Log.i(TAG, "Got odd message: " + msgBuf);
                            msgBuf.setLength(0);
                            return;
                        }
                        msgBuf.setLength(0);
                    }
                    break;
                case ChronoService.CTS_CHANGE:
                    Log.i(TAG, "CTS_CHANGE");
                    Toast.makeText(mActivity.get(), "CTS_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case ChronoService.DSR_CHANGE:
                    Log.i(TAG, "DSR_CHANGE");
                    Toast.makeText(mActivity.get(), "DSR_CHANGE",Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }
}
