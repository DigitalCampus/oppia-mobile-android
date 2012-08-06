package org.digitalcampus.mtrain.application;

import java.util.ArrayList;

import org.digitalcampus.mtrain.model.Activity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper{

	static final String TAG = "DbHelper";
	static final String DB_NAME = "mtrain.db"; 
	static final int DB_VERSION = 6; 
	
	private SQLiteDatabase db;
	
	public static final String MODULE_TABLE = "Module";
	public static final String MODULE_C_ID = BaseColumns._ID;
	public static final String MODULE_C_VERSIONID = "versionid";
	public static final String MODULE_C_TITLE = "title";
	public static final String MODULE_C_LOCATION = "location";
	
	public static final String ACTIVITY_TABLE = "Activity";
	public static final String ACTIVITY_C_ID = BaseColumns._ID;
	public static final String ACTIVITY_C_MODID = "modid"; // reference to MODULE_C_ID
	public static final String ACTIVITY_C_SECTIONID = "sectionid";
	public static final String ACTIVITY_C_ACTID = "activityid";
	public static final String ACTIVITY_C_ACTTYPE = "activitytype";
	
	public static final String LOG_TABLE = "Log";
	public static final String LOG_C_ID = BaseColumns._ID;
	public static final String LOG_C_ACTID = "actid"; // reference to ACTIVITY_C_ID
	public static final String LOG_C_DATETIME = "datetime";
	
	// Constructor
	public DbHelper(Context context) { //
		super(context, DB_NAME, null, DB_VERSION);
		db = this.getReadableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		// create Module Table
		String m_sql = "create table " + MODULE_TABLE + " (" + 
								MODULE_C_ID + " integer primary key autoincrement, " + 
								MODULE_C_VERSIONID + " int, " + 
								MODULE_C_TITLE + " text, " +
								MODULE_C_LOCATION + " text)"; 
		db.execSQL(m_sql);
		Log.d(TAG, "Module sql: " + m_sql);
		
		String a_sql = "create table " + ACTIVITY_TABLE + " (" + 
								ACTIVITY_C_ID + " integer primary key autoincrement, " + 
								ACTIVITY_C_MODID + " int, " + 
								ACTIVITY_C_SECTIONID + " int, " + 
								ACTIVITY_C_ACTID + " int, " +
								ACTIVITY_C_ACTTYPE + " text)";
		db.execSQL(a_sql);
		Log.d(TAG, "Activity sql: " + a_sql);
		
		String l_sql = "create table " + LOG_TABLE + " (" + 
								LOG_C_ID + " integer primary key autoincrement, " + 
								LOG_C_ACTID + " int, " + 
								LOG_C_DATETIME + " datetime default current_timestamp)";
		db.execSQL(l_sql);
		Log.d(TAG, "Activity sql: " + l_sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
		// drop tables
		db.execSQL("drop table if exists " + MODULE_TABLE);
		db.execSQL("drop table if exists " + ACTIVITY_TABLE);
		db.execSQL("drop table if exists " + LOG_TABLE);
		
		 // run onCreate to get new database
		Log.d(TAG, "onUpdated: DB tables dropped");
		onCreate(db);
	}
	
	// returns id of the row
	public long addOrUpdateModule(String versionid, String title, String location){
		// find if this is a new version or not		
		String selection = MODULE_C_LOCATION + "= ?";
		String[] selArgs = new String[] {location};
		Cursor c = db.query(MODULE_TABLE, null, selection , selArgs, null, null, null);
		
		ContentValues values = new ContentValues();
		values.put(DbHelper.MODULE_C_VERSIONID, versionid);
		values.put(DbHelper.MODULE_C_TITLE, title);
		values.put(DbHelper.MODULE_C_LOCATION, location);
		
		if(c.getCount() == 0){
			c.close();
			// just insert
			Log.d(TAG,"Record added");
			return db.insertOrThrow(DbHelper.MODULE_TABLE, null, values);
		} else {
			// check that the version id of new module is greater than existing
			c.moveToFirst();
			
			long toUpdate = 0;
			while(c.isAfterLast() == false){
				Log.d(TAG,c.getString(c.getColumnIndex(DbHelper.MODULE_C_VERSIONID)));
				Log.d(TAG,versionid);
				if (Long.valueOf(versionid) > c.getLong(c.getColumnIndex(DbHelper.MODULE_C_VERSIONID))){
					Log.d(TAG,"getting record to update");
					toUpdate = c.getLong(c.getColumnIndex(DbHelper.MODULE_C_ID));
				}
				c.moveToNext();
			}
			c.close();
			if(toUpdate != 0){
				db.update(DbHelper.MODULE_TABLE, values, DbHelper.MODULE_C_ID + "=" + toUpdate, null);
				Log.d(TAG,"Record updated");
				return toUpdate;
			} else {
				Log.d(TAG,"Record not updated");
				return -1;
			}
		}
	}
	
	public void insertActivities(ArrayList<Activity> acts){
		
	}
}
