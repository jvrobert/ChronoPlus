package com.chronoplus.chronoplus;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import nl.qbusict.cupboard.QueryResultIterable;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * Created by jvrobert on 4/30/2017.
 */

public class DataAccess extends SQLiteOpenHelper {
    private static DataAccess sInstance;

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
    private static final int DATABASE_VERSION = 5;


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
        return cupboard().withDatabase(db).put(rec);
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
