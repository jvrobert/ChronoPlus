package com.chronoplus.chronoplus;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.ocpsoft.prettytime.PrettyTime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import me.grantland.widget.AutofitHelper;


public class ShotStringActivity extends AppCompatActivity {
    private ListView mListView;
    ShotRecordAdapter adapter;
    private TextView fps_label;
    private TextView fps_text;
    private TextView fpe;

    public class ShotRecordAdapter extends ArrayAdapter<ShotRecord> {
        PrettyTime p = new PrettyTime();


        private List<ShotRecord> dataSet;
        Context mContext;

        public ShotRecordAdapter(Context context, List<ShotRecord> data) {
            super(context, 0, data);
            dataSet = data;
            mContext = context;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ShotRecord item = getItem(dataSet.size() - 1 - position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.shot_row_layout, parent, false);

            }
            TextView idx = (TextView) convertView.findViewById(R.id.shot_count);
            idx.setText(item.shotCount + ".");
            TextView fps = (TextView) convertView.findViewById(R.id.shot_fps);
            fps.setText((int) item.fps + " FPS");
            if (item.fpe != null) {
                TextView fpe = (TextView) convertView.findViewById(R.id.shot_fpe);
                String fpev = ((Integer) Math.round(item.fpe)).toString();
                fpe.setText(fpev + " FPE");
            }
            TextView date = (TextView) convertView.findViewById(R.id.shot_date);
            date.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(item.inserted));
            return convertView;
        }
    }

    private static final String TAG = "ShotStringActivity";

    private DataAccess da;
    private long id;
    private ShotString ss_rec;
    private ChronoService usbService;
    private MyHandler mHandler;
    private ImageView statusIcon;
    private TextView statusText;
    List<ShotRecord> shots;

    protected void setStyles() {
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/DSEG7Modern-Regular.ttf");
        fps_text.setTypeface(typeface);
        AutofitHelper hlp = AutofitHelper.create(fps_text);
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
            setCombroStatus(usbService.isAvailable());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        startService(ChronoService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it


        shots.clear();
        shots.addAll(da.getShots(ss_rec._id));
        Log.i(TAG, "LOADED " + shots.size() + " shots!!");
//        addShot(100);
//        addShot(200);
//        addShot(300);
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

    @Override
    public void onPause() {
        super.onPause();
        unbindService(usbConnection);
    }


    public void addShot(float fps) {
        fps_label.setVisibility(View.VISIBLE);
        String fps_val = ((Integer) (int) fps).toString();
        fps_text.setText(fps_val);
        fps_text.setVisibility(View.VISIBLE);

        ShotRecord rec = new ShotRecord();
        rec.inserted = new Date();
        rec.fps = fps;
        if (ss_rec.pellet_weight != null) {
            rec.fpe = (ss_rec.pellet_weight * fps * fps) / 450240;
            String fpev = ((Integer) Math.round(rec.fpe)).toString();
            fpe.setText(fpev + " FPE");
        }
        rec.shotStringId = ss_rec._id;
        rec.shotCount = shots.size() + 1;
        shots.add(rec);
        long id = da.saveShot(rec);
        Log.i(TAG, "SAVED SHOT " + rec.fps + " ID=" + id);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shot_string);
        da = DataAccess.getInstance(this);
        id = getIntent().getLongExtra(MainActivity.STRING_ID, -1);
        ss_rec = da.getString(id);
        getSupportActionBar().setTitle(ss_rec.name == null || ss_rec.name.length() == 0 ? "Untitled Shot String" : ss_rec.name);
        statusIcon = (ImageView) findViewById(R.id.statusIcon);
        statusText = (TextView) findViewById(R.id.usb_status);
        fps_text = (TextView) findViewById(R.id.txt_fps);
        fps_label = (TextView) findViewById(R.id.fpslabel);
        fpe = (TextView) findViewById(R.id.fpe);
        setStyles();
        mHandler = new MyHandler(this);

        mListView = (ListView) findViewById(R.id.lv_shots);
        shots = new ArrayList<>();
        adapter = new ShotRecordAdapter(this, shots);
        mListView.setAdapter(adapter);
//        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
//                ShotString o = (ShotString)mListView.getItemAtPosition(position);
//                Intent intent = new Intent(MainActivity.this, ShotStringActivity.class);
//                intent.putExtra(MainActivity.STRING_ID, o._id);
//                startActivity(intent);
//            }
//        });

    }

    private void setCombroStatus(boolean avail) {
        if (!avail) {
            statusIcon.setImageResource(R.drawable.bad);
            statusText.setText("Combro not connected.");
        } else {
            statusIcon.setImageResource(R.drawable.ok);
            statusText.setText("Combro connected.");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.shotstring_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.share_string:
                StringBuilder sb = new StringBuilder();
                for (ShotRecord rec : shots) {
                    sb.append(rec.shotCount + "," + ((Integer) Math.round(rec.fps)).toString() + "," + (rec.fpe == null ? "0" : ((Integer) Math.round(rec.fpe)).toString()) + "\n");

                }
                Log.i(TAG, "ST: " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS));
                Intent sendIntent = new Intent();
                File file = null;
                File root = getExternalFilesDir(null);
                Log.i(TAG, "DIR: " + root);
                if (root.canWrite()) {
                    File dir = new File(root.getAbsolutePath() + "/csvs");
                    dir.mkdirs();
                    file = new File(dir, "Data.csv");
                    FileOutputStream out = null;
                    try {
                        out = new FileOutputStream(file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        out.write(sb.toString().getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Uri u1 = null;
                u1 = Uri.fromFile(file);
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("application/csv");
                sendIntent.putExtra(android.content.Intent.EXTRA_EMAIL, "");
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, "");
                sendIntent.putExtra(Intent.EXTRA_STREAM, u1);
                startActivity(sendIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
                    mActivity.get().setCombroStatus((boolean) msg.obj);
                    Log.i(TAG, "STATUS AVAIL:: " + msg.obj);
                    break;
                case ChronoService.SHOT_FPS:
                    int fps = (int) msg.obj;
                    Log.i(TAG, "FPS: " + fps);
                    mActivity.get().addShot(fps);
                    break;
                case ChronoService.CTS_CHANGE:
                    Log.i(TAG, "CTS_CHANGE");
                    Toast.makeText(mActivity.get(), "CTS_CHANGE", Toast.LENGTH_LONG).show();
                    break;
                case ChronoService.DSR_CHANGE:
                    Log.i(TAG, "DSR_CHANGE");
                    Toast.makeText(mActivity.get(), "DSR_CHANGE", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }
}
