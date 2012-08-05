package org.digitalcampus.mtrain.application;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DB extends SQLiteOpenHelper{

	static final String TAG = "DB";
	static final String DB_NAME = "mtrain.db"; 
	static final int DB_VERSION = 1; 
	
	private SQLiteDatabase db;
	
	public static final String MODULE_TABLE = "Module";
	public static final String MODULE_C_ID = BaseColumns._ID;
	public static final String MODULE_C_VERSIONID = "versionid";
	public static final String MODULE_C_TITLE = "title";
	public static final String MODULE_C_LOCATION = "location";
	
	// Constructor
	public DB(Context context) { //
		super(context, DB_NAME, null, DB_VERSION);
		db = this.getReadableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		// create Module Table
		String s_sql = "create table " + MODULE_TABLE + " (" + 
								MODULE_C_ID + " integer primary key autoincrement, " + 
								MODULE_C_VERSIONID + " int, " + 
								MODULE_C_TITLE + " text, " +
								MODULE_C_LOCATION + " text)"; 
		db.execSQL(s_sql);
		Log.d(TAG, "Module sql: " + s_sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
		// drop tables
		db.execSQL("drop table if exists " + MODULE_TABLE);
		
	}
	
	public void addModule(String versionid, String title, String location){
		// find if this is a new version or not
		Log.v(TAG,location);
		Log.v(TAG,versionid);
		Log.v(TAG,title);
	}
}
