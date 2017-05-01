package com.chronoplus.chronoplus;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import me.grantland.widget.AutofitHelper;

public class MainActivity extends AppCompatActivity {
    public static final String STRING_ID = "chronoplus.SS_ID";

    private static final String TAG = "ChronoPlusApp";
    //private MyHandler mHandler;
    private TextView display;
    private ZXingScannerView mScannerView;
    private boolean showing = false;
    private ListView mListView;


//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case CAMERA_PERMS: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
////                    Button startButton = (Button)findViewById(R.id.button);
////                    startButton.setOnClickListener(new View.OnClickListener() {
////                        @Override
////                        public void onClick(View view) {
////                            ViewGroup contentFrame = (ViewGroup) findViewById(R.id.contentframe);
////                            if (! showing) {
////                                mScannerView = new ZXingScannerView(MainActivity.this);   // Programmatically initialize the scanner view
////
////                                contentFrame.addView(mScannerView);
////                                mScannerView.setResultHandler(MainActivity.this);
////                                ArrayList<BarcodeFormat> fs = new ArrayList<BarcodeFormat>();
////                                fs.add(BarcodeFormat.UPC_E);
////                                fs.add(BarcodeFormat.UPC_A);
////                                fs.add(BarcodeFormat.UPC_EAN_EXTENSION);
////                                mScannerView.setFormats(fs);
////                                mScannerView.setAutoFocus(true);
////                                mScannerView.startCamera();
////                                showing = true;
////                            }
////                            else {
////                                Log.i(TAG, "HIDE!!!");
////                                mScannerView.stopCameraPreview();
////                                mScannerView.stopCamera();
////                                Log.i(TAG, "HIDE!!! OK!!!!");
////                                contentFrame.setVisibility(View.GONE);
////                            }
////
////                            //fbs.StartMultiScan(true, 4, "pfx:scorecard:", this, null);
//////                            fbs.StartScan(false, MainActivity.this, null);
////                        }
//                    //});
//
//
//                    // permission was granted, yay! Do the
//                    // contacts-related task you need to do.
//
//                } else {
//
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                }
//                return;
//            }
//
//            // other 'case' lines to check for other
//            // permissions this app might request
//        }
//    }
//
//    /*
//     * Notifications from UsbService will be received here.
//     */
//    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            switch (intent.getAction()) {
//                case ChronoService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
//                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
//                    break;
//                case ChronoService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
//                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
//                    break;
//                case ChronoService.ACTION_NO_USB: // NO USB CONNECTED
//                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
//                    break;
//                case ChronoService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
//                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
//                    break;
//                case ChronoService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
//                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
//                    break;
//            }
//        }
//    };
//    private ChronoService usbService;

    @Override
    public void onResume() {
        super.onResume();
        //setFilters();  // Start listening notifications from UsbService
        //startService(ChronoService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

//    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
//        if (!ChronoService.SERVICE_CONNECTED) {
//            Intent startService = new Intent(this, service);
//            if (extras != null && !extras.isEmpty()) {
//                Set<String> keys = extras.keySet();
//                for (String key : keys) {
//                    String extra = extras.getString(key);
//                    startService.putExtra(key, extra);
//                }
//            }
//            startService(startService);
//        }
//        Intent bindingIntent = new Intent(this, service);
//        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
//    }

    @Override
    public void onPause() {
        super.onPause();
        //unregisterReceiver(mUsbReceiver);
        //unbindService(usbConnection);
    }

//    private void setFilters() {
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(ChronoService.ACTION_USB_PERMISSION_GRANTED);
//        filter.addAction(ChronoService.ACTION_NO_USB);
//        filter.addAction(ChronoService.ACTION_USB_DISCONNECTED);
//        filter.addAction(ChronoService.ACTION_USB_NOT_SUPPORTED);
//        filter.addAction(ChronoService.ACTION_USB_PERMISSION_NOT_GRANTED);
//        registerReceiver(mUsbReceiver, filter);
//    }
//    private final ServiceConnection usbConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
//            usbService = ((ChronoService.UsbBinder) arg1).getService();
//            usbService.setHandler(mHandler);
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName arg0) {
//            usbService = null;
//        }
//    };

//    public final static int CAMERA_PERMS = 1;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.new_string:
                Intent intent = new Intent(this, NewStringActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.top);
        mListView = (ListView) findViewById(R.id.listview);
        String[] listItems = new String[2];
        listItems[0] = "+ NEW";
        listItems[1] = "NEW2";
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems);
        mListView.setAdapter(adapter);



//        mHandler = new MyHandler(this);
//        display = (TextView) findViewById(R.id.textView);
//
//        Typeface typeface=Typeface.createFromAsset(getAssets(), "fonts/DSEG7Modern-Regular.ttf");
//        display.setTypeface(typeface);
//        AutofitHelper hlp = AutofitHelper.create(display);
//        hlp.setEnabled(true);
//        hlp.setMaxLines(1);
//        hlp.setMaxTextSize(4000);
//        hlp.setMinTextSize(50);
//
//        TextView fps = (TextView) findViewById(R.id.fpslabel);
//        AutofitHelper hlp2 = AutofitHelper.create(fps);
//        hlp2.setEnabled(true);
//        hlp2.setMaxLines(1);
//        hlp2.setMaxTextSize(4000);
//        hlp2.setMinTextSize(10);
//
//        ActivityCompat.requestPermissions(this,
//                new String[]{Manifest.permission.CAMERA, Manifest.permission.CAMERA},
//                CAMERA_PERMS);

    }

//    @Override
//    public void handleResult(Result result) {
//        Toast.makeText(this, "Contents = " + result.getText() +
//                ", Format = " + result.getBarcodeFormat().toString(), Toast.LENGTH_SHORT).show();
//        Log.i(TAG, "Contents = " + result.getText() +
//                ", Format = " + result.getBarcodeFormat().toString());
//
//        // Note:
//        // * Wait 2 seconds to resume the preview.
//        // * On older devices continuously stopping and resuming camera preview can result in freezing the app.
//        // * I don't know why this is the case but I don't have the time to figure out.
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mScannerView.resumeCameraPreview(MainActivity.this);
//            }
//        }, 2000);
//    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */

}
