package org.digitalcampus.mtrain.application;

import java.util.ArrayList;

import org.digitalcampus.mtrain.model.Activity;
import org.digitalcampus.mtrain.model.Module;

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
	static final int DB_VERSION = 1;

	private SQLiteDatabase db;

	public static final String MODULE_TABLE = "Module";
	public static final String MODULE_C_ID = BaseColumns._ID;
	public static final String MODULE_C_VERSIONID = "versionid";
	public static final String MODULE_C_TITLE = "title";
	public static final String MODULE_C_LOCATION = "location";

	public static final String ACTIVITY_TABLE = "Activity";
	public static final String ACTIVITY_C_ID = BaseColumns._ID;
	public static final String ACTIVITY_C_MODID = "modid"; // reference to
															// MODULE_C_ID
	public static final String ACTIVITY_C_SECTIONID = "sectionid";
	public static final String ACTIVITY_C_ACTID = "activityid";
	public static final String ACTIVITY_C_ACTTYPE = "activitytype";
	public static final String ACTIVITY_C_ACTIVITYDIGEST = "digest";

	public static final String LOG_TABLE = "Log";
	public static final String LOG_C_ID = BaseColumns._ID;
	public static final String LOG_C_MODID = "modid"; // reference to
														// MODULE_C_ID
	public static final String LOG_C_DATETIME = "logdatetime";
	public static final String LOG_C_ACTIVITYDIGEST = "digest";
	public static final String LOG_C_DATA = "logdata";
	public static final String LOG_C_SUBMITTED = "logsubmitted";

	// Constructor
	public DbHelper(Context context) { //
		super(context, DB_NAME, null, DB_VERSION);
		db = this.getReadableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createModuleTable(db);
		createActivityTable(db);
		createLogTable(db);
	}

	public void createModuleTable(SQLiteDatabase db){
		String m_sql = "create table " + MODULE_TABLE + " (" + MODULE_C_ID + " integer primary key autoincrement, "
				+ MODULE_C_VERSIONID + " int, " + MODULE_C_TITLE + " text, " + MODULE_C_LOCATION + " text)";
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

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


	}

	// returns id of the row
	public long addOrUpdateModule(String versionid, String title, String location) {
		// find if this is a new version or not
		String selection = MODULE_C_LOCATION + "= ?";
		String[] selArgs = new String[] { location };
		Cursor c = db.query(MODULE_TABLE, null, selection, selArgs, null, null, null);

		ContentValues values = new ContentValues();
		values.put(DbHelper.MODULE_C_VERSIONID, versionid);
		values.put(DbHelper.MODULE_C_TITLE, title);
		values.put(DbHelper.MODULE_C_LOCATION, location);

		if (c.getCount() == 0) {
			c.close();
			// just insert
			Log.v(TAG, "Record added");
			return db.insertOrThrow(DbHelper.MODULE_TABLE, null, values);
		} else {
			// check that the version id of new module is greater than existing
			c.moveToFirst();

			long toUpdate = 0;
			while (c.isAfterLast() == false) {
				Log.v(TAG, "Installed version: " + c.getString(c.getColumnIndex(DbHelper.MODULE_C_VERSIONID)));
				Log.v(TAG, "New version: " + versionid);
				if (Long.valueOf(versionid) > c.getLong(c.getColumnIndex(DbHelper.MODULE_C_VERSIONID))) {
					toUpdate = c.getLong(c.getColumnIndex(DbHelper.MODULE_C_ID));
				}
				c.moveToNext();
			}
			c.close();
			if (toUpdate != 0) {
				db.update(DbHelper.MODULE_TABLE, values, DbHelper.MODULE_C_ID + "=" + toUpdate, null);
				// remove all the old activities
				String s = DbHelper.ACTIVITY_C_MODID + "=?";
				String[] args = new String[] { String.valueOf(toUpdate) };
				db.delete(DbHelper.ACTIVITY_TABLE, s, args);
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
			values.put(DbHelper.ACTIVITY_C_MODID, a.getModId());
			values.put(DbHelper.ACTIVITY_C_SECTIONID, a.getSectionId());
			values.put(DbHelper.ACTIVITY_C_ACTID, a.getActId());
			values.put(DbHelper.ACTIVITY_C_ACTTYPE, a.getActType());
			values.put(DbHelper.ACTIVITY_C_ACTIVITYDIGEST, a.getDigest());
			db.insertOrThrow(DbHelper.ACTIVITY_TABLE, null, values);
		}
	}

	public ArrayList<Module> getModules() {
		ArrayList<Module> modules = new ArrayList<Module>();
		String order = MODULE_C_TITLE + " ASC";
		Cursor c = db.query(MODULE_TABLE, null, null, null, null, null, order);
		c.moveToFirst();
		while (c.isAfterLast() == false) {
			Module m = new Module();
			m.setModId(c.getInt(c.getColumnIndex(DbHelper.MODULE_C_ID)));
			m.setLocation(c.getString(c.getColumnIndex(DbHelper.MODULE_C_LOCATION)));
			m.setTitle(c.getString(c.getColumnIndex(DbHelper.MODULE_C_TITLE)));
			m.setProgress(this.getModuleProgress(m.getModId()));
			modules.add(m);
			c.moveToNext();
		}
		c.close();
		return modules;
	}
	
	public long insertLog(int modId, String digest, String data){
		ContentValues values = new ContentValues();
		values.put(DbHelper.LOG_C_MODID, modId);
		values.put(DbHelper.LOG_C_ACTIVITYDIGEST, digest);
		values.put(DbHelper.LOG_C_DATA, data);
		return db.insertOrThrow(DbHelper.LOG_TABLE, null, values);
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
		String s = DbHelper.LOG_C_MODID + "=?";
		String[] args = new String[] { String.valueOf(modId) };
		return db.delete(DbHelper.LOG_TABLE, s, args);
	}
	
	public void deleteModule(int modId){
		// delete log
		resetModule(modId);
		
		// delete activities
		String s = DbHelper.ACTIVITY_C_MODID + "=?";
		String[] args = new String[] { String.valueOf(modId) };
		db.delete(DbHelper.ACTIVITY_TABLE, s, args);
		
		// delete module
		s = DbHelper.MODULE_C_ID + "=?";
		args = new String[] { String.valueOf(modId) };
		db.delete(DbHelper.MODULE_TABLE, s, args);
	}
}
