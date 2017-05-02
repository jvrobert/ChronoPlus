package com.chronoplus.chronoplus;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import nl.qbusict.cupboard.QueryResultIterable;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * Created by jvrobert on 4/30/2017.
 */

public class DataAccess extends SQLiteOpenHelper {
    private static DataAccess sInstance;
    private final static String TAG = "SQLHelper";
    // ...

    public static synchronized DataAccess getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DataAccess(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */

    private static final String DATABASE_NAME = "chronoplus.db";
    private static final int DATABASE_VERSION = 11;


    static {
        cupboard().register(ShotString.class);
        cupboard().register(ShotRecord.class);
        cupboard().register(PelletRecord.class);
    }

    private DataAccess(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public long savePellet(PelletRecord rec) {
        SQLiteDatabase db = getWritableDatabase();
        return cupboard().withDatabase(db).put(rec);
    }

    public long newString(ShotString rec) {
        SQLiteDatabase db = getWritableDatabase();
        long ret= cupboard().withDatabase(db).put(rec);
        Log.i(TAG, "SAVESTRING " + ret);
        return ret;
    }

    public long saveShot(ShotRecord rec) {
        SQLiteDatabase db = getWritableDatabase();
        long ret = cupboard().withDatabase(db).put(rec);
        Log.i(TAG, "SAVE SHOT: " + ret);
        return ret;
    }

    public class CustomComparator implements Comparator<ShotString> {
        @Override
        public int compare(ShotString o1, ShotString o2) {
            return o2.created.compareTo(o1.created);
        }
    }

    public class CustomComparatorShot implements Comparator<ShotRecord> {
        @Override
        public int compare(ShotRecord o1, ShotRecord o2) {
            return o1.shotCount.compareTo(o2.shotCount);
        }
    }

    public List<ShotString> getStrings()
    {
        SQLiteDatabase db = getWritableDatabase();
        QueryResultIterable<ShotString> res = cupboard().withDatabase(db).query(ShotString.class).query();
        List<ShotString> ret = res.list();
        Collections.sort(ret, new CustomComparator());
        return ret;
    }

    public List<PelletRecord> getPellets() {
        SQLiteDatabase db = getWritableDatabase();
        QueryResultIterable<PelletRecord> res = cupboard().withDatabase(db).query(PelletRecord.class).query();
        return res.list();
    }

    public ShotString getString(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return cupboard().withDatabase(db).get(ShotString.class, id);
    }

    public PelletRecord getPellet(String upc) {
        SQLiteDatabase db = getWritableDatabase();
        return cupboard().withDatabase(db).query(PelletRecord.class).withSelection("upc = ?", upc).get();
    }

    public PelletRecord getPelletByName(String name) {
        SQLiteDatabase db = getWritableDatabase();
        return cupboard().withDatabase(db).query(PelletRecord.class).withSelection("name = ?", name).get();
    }

    public List<ShotRecord> getShots(long ss) {
        SQLiteDatabase db = getWritableDatabase();
        QueryResultIterable<ShotRecord> res = cupboard().withDatabase(db).query(ShotRecord.class).withSelection("shotStringId = ?", ((Long)ss).toString()).query();
        List<ShotRecord> ret = res.list(true);
        Log.i(TAG, "Loaded: " + ret.size() + " for id=" + ss);
        Collections.sort(ret, new CustomComparatorShot());
        return ret;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        cupboard().withDatabase(db).createTables();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        cupboard().withDatabase(db).upgradeTables();
        cupboard().withDatabase(db).delete(PelletRecord.class, null);
        cupboard().withDatabase(db).delete(ShotRecord.class, null);
        cupboard().withDatabase(db).delete(ShotString.class, null);
    }
}
