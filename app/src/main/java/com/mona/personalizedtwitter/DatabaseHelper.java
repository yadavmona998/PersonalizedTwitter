package com.mona.personalizedtwitter;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import twitter4j.Status;

/**
 * Created by mona on 1/11/2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    /**db version*/
    private static final int DATABASE_VERSION = 1;
    /**database name*/
    private static final String DATABASE_NAME = "home.db";
    /**ID column*/
    private static final String HOME_COL = BaseColumns._ID;
    /**tweet text*/
    private static final String UPDATE_COL = "update_text";
    /**twitter screen name*/
    private static final String USER_COL = "user_screen";
    /**time tweeted*/
    private static final String TIME_COL = "update_time";
    /**user profile image*/
    private static final String USER_IMG = "user_img";

    /**database creation string*/
    private static final String DATABASE_CREATE = "CREATE TABLE home (" + HOME_COL + " INTEGER NOT NULL PRIMARY KEY, " + UPDATE_COL + " TEXT, " + USER_COL + " TEXT, " + TIME_COL + " INTEGER, " + USER_IMG + " TEXT);";





    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS home");
        db.execSQL("VACUUM");
        onCreate(db);
    }

    public static ContentValues getValues(Status status) {

        //prepare ContentValues to return
        ContentValues homeValues = new ContentValues();

        //get the values
        try {
            //get each value from the table
            homeValues.put(HOME_COL, status.getId());
            homeValues.put(UPDATE_COL, status.getText());
            homeValues.put(USER_COL, status.getUser().getScreenName());
            homeValues.put(TIME_COL, status.getCreatedAt().getTime());
            homeValues.put(USER_IMG, status.getUser().getProfileImageURL().toString());
        }
        catch(Exception te) { Log.e(StringTokens.TAG, te.getMessage()); }
        //return the values
        return homeValues;
    }
}
