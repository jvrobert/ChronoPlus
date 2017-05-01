package com.chronoplus.chronoplus;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class NewStringActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler, AdapterView.OnItemSelectedListener {
    private ZXingScannerView mScannerView;
    private DataAccess da;
    private Spinner mUpclist;
    private ArrayList<String> upcs;
    private String currentUPC = null;
    private boolean is_loaded = false;
    public static final String TAG = "NewStringActivity";

    private TextView stringNameField;
    private TextView stringDescField;
    private TextView pelletNameField;
    private TextView pelletWeightField;

    protected void startScanning() {
        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.scannerframe);
        mScannerView = new ZXingScannerView(NewStringActivity.this);   // Programmatically initialize the scanner view
        contentFrame.addView(mScannerView);
        mScannerView.setResultHandler(NewStringActivity.this);
        ArrayList<BarcodeFormat> fs = new ArrayList<BarcodeFormat>();
        fs.add(BarcodeFormat.UPC_E);
        fs.add(BarcodeFormat.UPC_A);
        fs.add(BarcodeFormat.UPC_EAN_EXTENSION);
        mScannerView.setFormats(fs);
        mScannerView.setAutoFocus(true);
        mScannerView.startCamera();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.newstring_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.save_string:
                Long pellet_id = null;
                String gr = pelletWeightField.getText().toString();
                Float weight = null;
                if (currentUPC != null) {

                    PelletRecord rec = da.getPellet(currentUPC);
                    if (rec == null) {
                        rec = new PelletRecord();
                    }
                    rec.name = pelletNameField.getText().toString();
                    if (gr.length() > 0)
                        rec.grains = weight = Float.parseFloat(gr);
                    else
                        rec.grains = null;
                    rec.upc = currentUPC;
                    if (rec.name != null && rec.name.length() > 0) {
                        pellet_id = da.savePellet(rec);
                        Log.i(TAG, "Saved pellet id " + pellet_id);
                    }
                }
                else {
                    PelletRecord rec = da.getPelletByName(pelletNameField.getText().toString());
                    if (rec == null) {
                        rec = new PelletRecord();
                        rec.name = pelletNameField.getText().toString();
                    }

                    if (gr.length() > 0) {
                        rec.grains = weight = Float.parseFloat(gr);
                    }
                    else {
                        rec.grains = null;
                    }
                    if (rec.name != null && rec.name.length() > 0) {
                        pellet_id = da.savePellet(rec);
                        Log.i(TAG, "Saved pellet id " + pellet_id);
                    }
                }

                ShotString ss = new ShotString();
                ss.created = new Date();
                ss.name = stringNameField.getText().toString();
                if (ss.name.length() == 0) {
                    ss.name = "Unnamed";
                }
                ss.note = stringDescField.getText().toString();
                ss.pellet_id = null;
                ss.pellet_weight = weight;
                long id = da.newString(ss);
                Log.i(TAG, "Saved string id " + id);

                Intent intent = new Intent(this, ShotStringActivity.class);
                intent.putExtra(MainActivity.STRING_ID, id);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newstring);
        da = DataAccess.getInstance(this);
        upcs = new ArrayList<String>();
        upcs.add("");



        List<PelletRecord> recs = da.getPellets();
        for (PelletRecord rec : recs) {
            upcs.add(rec.name);
        }
        mUpclist = (Spinner) findViewById(R.id.spinner);
        mUpclist.setOnItemSelectedListener(this);


        stringNameField = (TextView)findViewById(R.id.stringName);
        stringDescField = (TextView)findViewById(R.id.stringDesc);
        pelletNameField = (TextView)findViewById(R.id.pelletName);
        pelletWeightField = (TextView)findViewById(R.id.pelletWeight);



        if (mUpclist == null) {
            Log.e(TAG, "EXCEPTION NULL");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_layout, upcs);
        mUpclist.setAdapter(adapter);

        startScanning();
    }

    @Override
    public void handleResult(Result result) {
        //Toast.makeText(this, "RESULT: " + result.getText(), Toast.LENGTH_SHORT).show();
        TextView txt = (TextView) findViewById(R.id.upccode);
        txt.setVisibility(View.VISIBLE);
        currentUPC = result.getText();
        PelletRecord rec = da.getPellet(currentUPC);
        if (rec != null) {
            pelletNameField.setText(rec.name);
            pelletWeightField.setText(rec.grains.toString());
            int idx = upcs.indexOf(rec.name);
            if (idx >= 0) {
                mUpclist.setSelection(idx);
            }
            txt.setText("Loaded pellet data for UPC: " + result.getText());
        }
        else {
            txt.setText("Will save in DB as UPC: " + result.getText());
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.i(TAG, "LOAD: " + parent.getItemAtPosition(position).toString());
        PelletRecord rec = da.getPelletByName(parent.getItemAtPosition(position).toString());
        if (rec != null) {
            TextView txt = (TextView) findViewById(R.id.upccode);
            txt.setVisibility(View.VISIBLE);
            pelletNameField.setText(rec.name);
            pelletWeightField.setText(rec.grains.toString());
            txt.setText("Loaded pellet data for '" + rec.name + "'");
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
