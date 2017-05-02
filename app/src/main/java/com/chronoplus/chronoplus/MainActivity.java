package com.chronoplus.chronoplus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<ShotString> shots;
    public class ShotStringAdapter extends ArrayAdapter<ShotString> {
        PrettyTime p = new PrettyTime();


        private List<ShotString> dataSet;
        Context mContext;
        public ShotStringAdapter(Context context, List<ShotString> data) {
            super(context, 0, data);
            dataSet = data;
            mContext=context;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ShotString item = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.string_row_layout, parent, false);

            }
            TextView ssname = (TextView) convertView.findViewById(R.id.ss_name);
            ssname.setText(item.name);
            TextView ssdate = (TextView) convertView.findViewById(R.id.ss_date);
            ssdate.setText(p.format(item.created));
            TextView ssdesc = (TextView) convertView.findViewById(R.id.ss_desc);
            String desc = "";
            if (item.note != null) {
                desc += item.note;
            }
            if (item.pellet_weight != null) {
                desc += "\n" + item.pellet_weight + " gr.";
            }
            ssdesc.setText(desc);
            return convertView;
        }
    }
    public static final String STRING_ID = "chronoplus.SS_ID";
    private ListView mListView;
    private DataAccess da;
    ShotStringAdapter adapter;


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
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        shots.clear();
        shots.addAll(da.getStrings());
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.top);
        da = DataAccess.getInstance(this);
        mListView = (ListView) findViewById(R.id.listview);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                ShotString o = (ShotString)mListView.getItemAtPosition(position);
                Intent intent = new Intent(MainActivity.this, ShotStringActivity.class);
                intent.putExtra(MainActivity.STRING_ID, o._id);
                startActivity(intent);
            }
        });
        shots = da.getStrings();
        adapter = new ShotStringAdapter(this, shots);
        mListView.setAdapter(adapter);

    }
}
