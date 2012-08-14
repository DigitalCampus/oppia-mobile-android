package org.digitalcampus.mquiz;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class MQuizDb extends SQLiteOpenHelper {

	static final String TAG = "MQuizDb";
	static final String DB_NAME = "mquiz.db";
	static final int DB_VERSION = 2;
	
	public static final String MQUIZRESULTS_TABLE = "results";
	public static final String MQUIZRESULTS_C_ID = BaseColumns._ID;
	public static final String MQUIZRESULTS_C_DATETIME = "resultdatetime";
	public static final String MQUIZRESULTS_C_DATA = "content";
	public static final String MQUIZRESULTS_C_SENT = "submitted";
	
	private SQLiteDatabase db;
	
	// Constructor
	public MQuizDb(Context context) { //
		super(context, DB_NAME, null, DB_VERSION);
		db = this.getWritableDatabase();
	}
		
	@Override
	public void onCreate(SQLiteDatabase db) {
		createResultsTable(db);
		
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("drop table if exists " + MQUIZRESULTS_TABLE);
		onCreate(db);
		
	}
	
	public void createResultsTable(SQLiteDatabase db){
		String m_sql = "create table " + MQUIZRESULTS_TABLE + " (" + 
							MQUIZRESULTS_C_ID + " integer primary key autoincrement, " + 
							MQUIZRESULTS_C_DATETIME + " datetime default current_timestamp, " + 
							MQUIZRESULTS_C_DATA + " text, " +  
							MQUIZRESULTS_C_SENT + " integer default 0)";
		Log.d(TAG, "Module sql: " + m_sql);
		db.execSQL(m_sql);
	}
	
	public long insertResult(String data){
		ContentValues values = new ContentValues();
		values.put(MQUIZRESULTS_C_DATA, data);
		return db.insertOrThrow(MQUIZRESULTS_TABLE, null, values);
	}

}
