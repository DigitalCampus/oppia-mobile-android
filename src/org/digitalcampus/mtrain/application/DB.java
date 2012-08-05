package org.digitalcampus.mtrain.application;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DB extends SQLiteOpenHelper{

	static final String TAG = "DB";
	static final String DB_NAME = "mtrain.db"; 
	static final int DB_VERSION = 1; 
	
	private SQLiteDatabase db;
	
	// Constructor
	public DB(Context context) { //
		super(context, DB_NAME, null, DB_VERSION);
		db = this.getReadableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
		// drop tables
		//db.execSQL("drop table if exists " + PROPS_TABLE);
		
	}
}
