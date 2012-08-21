package org.digitalcampus.mtrain.application;

import java.util.ArrayList;

import org.digitalcampus.mtrain.model.Activity;
import org.digitalcampus.mtrain.model.Module;
import org.digitalcampus.mtrain.task.Payload;
import org.digitalcampus.mtrain.task.SubmitMQuizTask;
import org.digitalcampus.mtrain.task.SubmitTrackerTask;
import org.json.JSONException;
import org.json.JSONObject;

import com.bugsense.trace.BugSenseHandler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {

	static final String TAG = "DbHelper";
	static final String DB_NAME = "mtrain.db";
	static final int DB_VERSION = 3;

	private SQLiteDatabase db;

	private static final String MODULE_TABLE = "Module";
	private static final String MODULE_C_ID = BaseColumns._ID;
	private static final String MODULE_C_VERSIONID = "versionid";
	private static final String MODULE_C_TITLE = "title";
	private static final String MODULE_C_SHORTNAME = "shortname";
	private static final String MODULE_C_LOCATION = "location";

	private static final String ACTIVITY_TABLE = "Activity";
	private static final String ACTIVITY_C_ID = BaseColumns._ID;
	private static final String ACTIVITY_C_MODID = "modid"; // reference to
															// MODULE_C_ID
	private static final String ACTIVITY_C_SECTIONID = "sectionid";
	private static final String ACTIVITY_C_ACTID = "activityid";
	private static final String ACTIVITY_C_ACTTYPE = "activitytype";
	private static final String ACTIVITY_C_ACTIVITYDIGEST = "digest";

	private static final String LOG_TABLE = "Log";
	private static final String LOG_C_ID = BaseColumns._ID;
	private static final String LOG_C_MODID = "modid"; // reference to
														// MODULE_C_ID
	private static final String LOG_C_DATETIME = "logdatetime";
	private static final String LOG_C_ACTIVITYDIGEST = "digest";
	private static final String LOG_C_DATA = "logdata";
	private static final String LOG_C_SUBMITTED = "logsubmitted";

	
	private static final String MQUIZRESULTS_TABLE = "results";
	private static final String MQUIZRESULTS_C_ID = BaseColumns._ID;
	private static final String MQUIZRESULTS_C_DATETIME = "resultdatetime";
	private static final String MQUIZRESULTS_C_DATA = "content";
	private static final String MQUIZRESULTS_C_SENT = "submitted";
	private static final String MQUIZRESULTS_C_MODID = "moduleid";
	
	// Constructor
	public DbHelper(Context context) { //
		super(context, DB_NAME, null, DB_VERSION);
		db = this.getWritableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createModuleTable(db);
		createActivityTable(db);
		createLogTable(db);
		createMquizResultsTable(db);
	}

	public void createModuleTable(SQLiteDatabase db){
		String m_sql = "create table " + MODULE_TABLE + " (" + MODULE_C_ID + " integer primary key autoincrement, "
				+ MODULE_C_VERSIONID + " int, " + MODULE_C_TITLE + " text, " + MODULE_C_LOCATION + " text, "
				+ MODULE_C_SHORTNAME + " text)";
		Log.d(TAG, "Module sql: " + m_sql);
		db.execSQL(m_sql);
	}
	
	public void createActivityTable(SQLiteDatabase db){
		String a_sql = "create table " + ACTIVITY_TABLE + " (" + 
									ACTIVITY_C_ID + " integer primary key autoincrement, " + 
									ACTIVITY_C_MODID + " int, " + 
									ACTIVITY_C_SECTIONID + " int, " + 
									ACTIVITY_C_ACTID + " int, " + 
									ACTIVITY_C_ACTTYPE + " text, " + 
									ACTIVITY_C_ACTIVITYDIGEST + " text)";
		Log.d(TAG, "Activity sql: " + a_sql);
		db.execSQL(a_sql);
	}
	
	public void createLogTable(SQLiteDatabase db){
		String l_sql = "create table " + LOG_TABLE + " (" + 
				LOG_C_ID + " integer primary key autoincrement, " + 
				LOG_C_MODID + " integer, " + 
				LOG_C_DATETIME + " datetime default current_timestamp, " + 
				LOG_C_ACTIVITYDIGEST + " text, " + 
				LOG_C_DATA + " text, " + 
				LOG_C_SUBMITTED + " integer default 0)";
		Log.d(TAG, "Log sql: " + l_sql);
		db.execSQL(l_sql);
	}

	public void createMquizResultsTable(SQLiteDatabase db){
		String m_sql = "create table " + MQUIZRESULTS_TABLE + " (" + 
							MQUIZRESULTS_C_ID + " integer primary key autoincrement, " + 
							MQUIZRESULTS_C_DATETIME + " datetime default current_timestamp, " + 
							MQUIZRESULTS_C_DATA + " text, " +  
							MQUIZRESULTS_C_SENT + " integer default 0, "+
							MQUIZRESULTS_C_MODID + " integer)";
		Log.d(TAG, "MQuiz results  sql: " + m_sql);
		db.execSQL(m_sql);
	}
	
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		db.execSQL("drop table if exists " + MODULE_TABLE);
		db.execSQL("drop table if exists " + ACTIVITY_TABLE);
		db.execSQL("drop table if exists " + LOG_TABLE);
		db.execSQL("drop table if exists " + MQUIZRESULTS_TABLE);
		onCreate(db);
	}

	// returns id of the row
	// TODO tidy this up now have is installed and toUpdate options
	public long addOrUpdateModule(String versionid, String title, String location, String shortname) {
		// find if this is a new version or not
		String selection = MODULE_C_LOCATION + "= ?";
		String[] selArgs = new String[] { location };
		Cursor c = db.query(MODULE_TABLE, null, selection, selArgs, null, null, null);

		ContentValues values = new ContentValues();
		values.put(MODULE_C_VERSIONID, versionid);
		values.put(MODULE_C_TITLE, title);
		values.put(MODULE_C_LOCATION, location);
		values.put(MODULE_C_SHORTNAME, shortname);

		if (c.getCount() == 0) {
			c.close();
			// just insert
			Log.v(TAG, "Record added");
			return db.insertOrThrow(MODULE_TABLE, null, values);
		} else {
			// check that the version id of new module is greater than existing
			c.moveToFirst();

			long toUpdate = 0;
			while (c.isAfterLast() == false) {
				Log.v(TAG, "Installed version: " + c.getString(c.getColumnIndex(MODULE_C_VERSIONID)));
				Log.v(TAG, "New version: " + versionid);
				if (Long.valueOf(versionid) > c.getLong(c.getColumnIndex(MODULE_C_VERSIONID))) {
					toUpdate = c.getLong(c.getColumnIndex(MODULE_C_ID));
				}
				c.moveToNext();
			}
			c.close();
			if (toUpdate != 0) {
				db.update(MODULE_TABLE, values, MODULE_C_ID + "=" + toUpdate, null);
				// remove all the old activities
				String s = ACTIVITY_C_MODID + "=?";
				String[] args = new String[] { String.valueOf(toUpdate) };
				db.delete(ACTIVITY_TABLE, s, args);
				return toUpdate;
			} else {
				return -1;
			}
		}
	}

	public void insertActivities(ArrayList<Activity> acts) {
		// acts.listIterator();
		for (Activity a : acts) {
			ContentValues values = new ContentValues();
			values.put(ACTIVITY_C_MODID, a.getModId());
			values.put(ACTIVITY_C_SECTIONID, a.getSectionId());
			values.put(ACTIVITY_C_ACTID, a.getActId());
			values.put(ACTIVITY_C_ACTTYPE, a.getActType());
			values.put(ACTIVITY_C_ACTIVITYDIGEST, a.getDigest());
			db.insertOrThrow(ACTIVITY_TABLE, null, values);
		}
	}

	public ArrayList<Module> getModules() {
		ArrayList<Module> modules = new ArrayList<Module>();
		String order = MODULE_C_TITLE + " ASC";
		Cursor c = db.query(MODULE_TABLE, null, null, null, null, null, order);
		c.moveToFirst();
		while (c.isAfterLast() == false) {
			Module m = new Module();
			m.setModId(c.getInt(c.getColumnIndex(MODULE_C_ID)));
			m.setLocation(c.getString(c.getColumnIndex(MODULE_C_LOCATION)));
			m.setProgress(this.getModuleProgress(m.getModId()));
			modules.add(m);
			c.moveToNext();
		}
		c.close();
		return modules;
	}
	
	public long insertLog(int modId, String digest, String data){
		ContentValues values = new ContentValues();
		values.put(LOG_C_MODID, modId);
		values.put(LOG_C_ACTIVITYDIGEST, digest);
		values.put(LOG_C_DATA, data);
		return db.insertOrThrow(LOG_TABLE, null, values);
	}
	
	public float getModuleProgress(int modId){
		String sql = "SELECT a."+ ACTIVITY_C_ID + ", " +
				"l."+ LOG_C_ACTIVITYDIGEST + 
				" as d FROM "+ACTIVITY_TABLE + " a " +
				" LEFT OUTER JOIN (SELECT DISTINCT " +LOG_C_ACTIVITYDIGEST +" FROM "+LOG_TABLE+") l ON a."+ ACTIVITY_C_ACTIVITYDIGEST +" = l."+LOG_C_ACTIVITYDIGEST + 
				" WHERE a."+ ACTIVITY_C_MODID +"=" + String.valueOf(modId);
		Cursor c = db.rawQuery(sql,null);
		int noActs = c.getCount();
		int noComplete = 0;
		c.moveToFirst();
		while (c.isAfterLast() == false) {
			if(c.getString(c.getColumnIndex("d")) != null){
				noComplete++;
			}
			c.moveToNext();
		}
		c.close();
		return noComplete*100/noActs;
	}
	
	public float getSectionProgress(int modId, int sectionId){
		String sql = "SELECT a."+ ACTIVITY_C_ID + ", " +
						"l."+ LOG_C_ACTIVITYDIGEST + 
						" as d FROM "+ACTIVITY_TABLE + " a " +
						" LEFT OUTER JOIN (SELECT DISTINCT " +LOG_C_ACTIVITYDIGEST +" FROM "+LOG_TABLE+") l ON a."+ ACTIVITY_C_ACTIVITYDIGEST +" = l."+LOG_C_ACTIVITYDIGEST + 
						" WHERE a."+ ACTIVITY_C_MODID +"=" + String.valueOf(modId) +
						" AND a."+ ACTIVITY_C_SECTIONID +"=" + String.valueOf(sectionId);
		
		Cursor c = db.rawQuery(sql,null);
		int noActs = c.getCount();
		int noComplete = 0;
		c.moveToFirst();
		while (c.isAfterLast() == false) {
			if(c.getString(c.getColumnIndex("d")) != null){
				noComplete++;
			}
			c.moveToNext();
		}
		c.close();
		return noComplete*100/noActs;
		
	}
	
	public int resetModule(int modId){
		// delete quiz results
		this.deleteMQuizResults(modId);
		
		String s = LOG_C_MODID + "=?";
		String[] args = new String[] { String.valueOf(modId) };
		return db.delete(LOG_TABLE, s, args);
	}
	
	public void deleteModule(int modId){
		// delete log
		resetModule(modId);
		
		// delete activities
		String s = ACTIVITY_C_MODID + "=?";
		String[] args = new String[] { String.valueOf(modId) };
		db.delete(ACTIVITY_TABLE, s, args);
		
		// delete module
		s = MODULE_C_ID + "=?";
		args = new String[] { String.valueOf(modId) };
		db.delete(MODULE_TABLE, s, args);
		
		// delete any quiz attempts
		this.deleteMQuizResults(modId);
	}
	
	public boolean isInstalled(String shortname){
		String s = MODULE_C_SHORTNAME + "=?";
		String[] args = new String[] { shortname };
		Cursor c = db.query(MODULE_TABLE, null, s, args, null, null, null);
		if(c.getCount() == 0){
			c.close();
			return false;
		} else {
			c.close();
			return true;
		}
	}
	
	public boolean toUpdate(String shortname, Double version){
		String s = MODULE_C_SHORTNAME + "=? AND "+ MODULE_C_VERSIONID + "< ?";
		String[] args = new String[] { shortname, String.format("%.0f", version) };
		Cursor c = db.query(MODULE_TABLE, null, s, args, null, null, null);
		if(c.getCount() == 0){
			c.close();
			return false;
		} else {
			c.close();
			return true;
		}
	}
	
	public Payload getUnsentLog(){
		String s = LOG_C_SUBMITTED + "=? ";
		String[] args = new String[] { "0" };
		Cursor c = db.query(LOG_TABLE, null, s, args, null, null, null);
		int count = c.getCount();
		c.moveToFirst();
		int counter = 0;
		org.digitalcampus.mtrain.model.Log[] sl = new org.digitalcampus.mtrain.model.Log[count];
		while (c.isAfterLast() == false) {
			org.digitalcampus.mtrain.model.Log so = new org.digitalcampus.mtrain.model.Log();
			so.id = c.getLong(c.getColumnIndex(LOG_C_ID));
			so.digest = c.getString(c.getColumnIndex(LOG_C_ACTIVITYDIGEST));
			String content = "";
			try {
				JSONObject json = new JSONObject(c.getString(c.getColumnIndex(LOG_C_DATA)));
				json.put("datetime", c.getString(c.getColumnIndex(LOG_C_DATETIME)));
				json.put("digest", c.getString(c.getColumnIndex(LOG_C_ACTIVITYDIGEST)));
				content = json.toString();
			} catch (JSONException e) {
				e.printStackTrace();
				BugSenseHandler.log(TAG, e);
			}
			
			so.content  = content;
			sl[counter] = so;
			counter++;
			c.moveToNext();
		}
		Payload p = new Payload(SubmitTrackerTask.SUBMIT_LOG_TASK,sl);
		c.close();
		
		return p;
	}
	
	public int markLogSubmitted(long rowId){
		ContentValues values = new ContentValues();
		values.put(LOG_C_SUBMITTED, 1);
		
		return db.update(LOG_TABLE, values, LOG_C_ID + "=" + rowId, null);
	}
	
	public long insertMQuizResult(String data, int modId){
		ContentValues values = new ContentValues();
		values.put(MQUIZRESULTS_C_DATA, data);
		values.put(MQUIZRESULTS_C_MODID, modId);
		return db.insertOrThrow(MQUIZRESULTS_TABLE, null, values);
	}
	
	public Payload getUnsentMquiz(){
		String s = MQUIZRESULTS_C_SENT + "=? ";
		String[] args = new String[] { "0" };
		Cursor c = db.query(MQUIZRESULTS_TABLE, null, s, args, null, null, null);
		int count = c.getCount();
		c.moveToFirst();
		int counter = 0;
		org.digitalcampus.mtrain.model.Log[] sl = new org.digitalcampus.mtrain.model.Log[count];
		while (c.isAfterLast() == false) {
			org.digitalcampus.mtrain.model.Log so = new org.digitalcampus.mtrain.model.Log();
			so.id = c.getLong(c.getColumnIndex(MQUIZRESULTS_C_ID));
			so.content  = c.getString(c.getColumnIndex(MQUIZRESULTS_C_DATA));
			sl[counter] = so;
			counter++;
			c.moveToNext();
		}
		Payload p = new Payload(SubmitMQuizTask.SUBMIT_MQUIZ_TASK,sl);
		c.close();
		
		return p;
	}
	
	public int markMQuizSubmitted(long rowId){
		ContentValues values = new ContentValues();
		values.put(MQUIZRESULTS_C_SENT, 1);
		
		return db.update(MQUIZRESULTS_TABLE, values, MQUIZRESULTS_C_ID + "=" + rowId, null);
	}
	
	public void deleteMQuizResults(int modId){
		// delete any quiz attempts
		String s = MQUIZRESULTS_C_MODID + "=?";
		String[] args = new String[] { String.valueOf(modId) };
		db.delete(MQUIZRESULTS_TABLE, s, args);
	}
}
