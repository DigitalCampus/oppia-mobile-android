/* 
 * This file is part of OppiaMobile - http://oppia-mobile.org/
 * 
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalcampus.mobile.learning.application;

import java.util.ArrayList;
import java.util.Locale;

import org.digitalcampus.mobile.learning.model.Activity;
import org.digitalcampus.mobile.learning.model.ActivitySchedule;
import org.digitalcampus.mobile.learning.model.MessageFeed;
import org.digitalcampus.mobile.learning.model.Module;
import org.digitalcampus.mobile.learning.model.TrackerLog;
import org.digitalcampus.mobile.learning.task.Payload;
import org.digitalcampus.mobile.learning.task.SubmitMQuizTask;
import org.digitalcampus.mobile.learning.task.SubmitTrackerTask;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;

public class DbHelper extends SQLiteOpenHelper {

	static final String TAG = DbHelper.class.getSimpleName();
	static final String DB_NAME = "mobilelearning.db";
	static final int DB_VERSION = 11;

	private SQLiteDatabase db;
	
	private static final String MODULE_TABLE = "Module";
	private static final String MODULE_C_ID = BaseColumns._ID;
	private static final String MODULE_C_VERSIONID = "versionid";
	private static final String MODULE_C_TITLE = "title";
	private static final String MODULE_C_SHORTNAME = "shortname";
	private static final String MODULE_C_LOCATION = "location";
	private static final String MODULE_C_SCHEDULE = "schedule";
	
	private static final String ACTIVITY_TABLE = "Activity";
	private static final String ACTIVITY_C_ID = BaseColumns._ID;
	private static final String ACTIVITY_C_MODID = "modid"; // reference to
															// MODULE_C_ID
	private static final String ACTIVITY_C_SECTIONID = "sectionid";
	private static final String ACTIVITY_C_ACTID = "activityid";
	private static final String ACTIVITY_C_ACTTYPE = "activitytype";
	private static final String ACTIVITY_C_ACTIVITYDIGEST = "digest";
	private static final String ACTIVITY_C_STARTDATE = "startdate";
	private static final String ACTIVITY_C_ENDDATE = "enddate";
	private static final String ACTIVITY_C_TITLE = "title";

	private static final String TRACKER_LOG_TABLE = "TrackerLog";
	private static final String TRACKER_LOG_C_ID = BaseColumns._ID;
	private static final String TRACKER_LOG_C_MODID = "modid"; // reference to MODULE_C_ID
	private static final String TRACKER_LOG_C_DATETIME = "logdatetime";
	private static final String TRACKER_LOG_C_ACTIVITYDIGEST = "digest";
	private static final String TRACKER_LOG_C_DATA = "logdata";
	private static final String TRACKER_LOG_C_SUBMITTED = "logsubmitted";
	private static final String TRACKER_LOG_C_INPROGRESS = "loginprogress";
	
	private static final String MQUIZRESULTS_TABLE = "results";
	private static final String MQUIZRESULTS_C_ID = BaseColumns._ID;
	private static final String MQUIZRESULTS_C_DATETIME = "resultdatetime";
	private static final String MQUIZRESULTS_C_DATA = "content";
	private static final String MQUIZRESULTS_C_SENT = "submitted";
	private static final String MQUIZRESULTS_C_MODID = "moduleid";
	
	// Constructor
	public DbHelper(Context ctx) { //
		super(ctx, DB_NAME, null, DB_VERSION);
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
				+ MODULE_C_SHORTNAME + " text," + MODULE_C_SCHEDULE + " int)";
		db.execSQL(m_sql);
	}
	
	public void createActivityTable(SQLiteDatabase db){
		String a_sql = "create table " + ACTIVITY_TABLE + " (" + 
									ACTIVITY_C_ID + " integer primary key autoincrement, " + 
									ACTIVITY_C_MODID + " int, " + 
									ACTIVITY_C_SECTIONID + " int, " + 
									ACTIVITY_C_ACTID + " int, " + 
									ACTIVITY_C_ACTTYPE + " text, " + 
									ACTIVITY_C_STARTDATE + " datetime null, " + 
									ACTIVITY_C_ENDDATE + " datetime null, " + 
									ACTIVITY_C_ACTIVITYDIGEST + " text, "+
									ACTIVITY_C_TITLE + " text)";
		db.execSQL(a_sql);
	}
	
	public void createLogTable(SQLiteDatabase db){
		String l_sql = "create table " + TRACKER_LOG_TABLE + " (" + 
				TRACKER_LOG_C_ID + " integer primary key autoincrement, " + 
				TRACKER_LOG_C_MODID + " integer, " + 
				TRACKER_LOG_C_DATETIME + " datetime default current_timestamp, " + 
				TRACKER_LOG_C_ACTIVITYDIGEST + " text, " + 
				TRACKER_LOG_C_DATA + " text, " + 
				TRACKER_LOG_C_SUBMITTED + " integer default 0, " + 
				TRACKER_LOG_C_INPROGRESS + " integer default 0)";
		db.execSQL(l_sql);
	}

	public void createMquizResultsTable(SQLiteDatabase db){
		String m_sql = "create table " + MQUIZRESULTS_TABLE + " (" + 
							MQUIZRESULTS_C_ID + " integer primary key autoincrement, " + 
							MQUIZRESULTS_C_DATETIME + " datetime default current_timestamp, " + 
							MQUIZRESULTS_C_DATA + " text, " +  
							MQUIZRESULTS_C_SENT + " integer default 0, "+
							MQUIZRESULTS_C_MODID + " integer)";
		db.execSQL(m_sql);
	}
	
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		if(oldVersion < 7){
			db.execSQL("drop table if exists " + MODULE_TABLE);
			db.execSQL("drop table if exists " + ACTIVITY_TABLE);
			db.execSQL("drop table if exists " + TRACKER_LOG_TABLE);
			db.execSQL("drop table if exists " + MQUIZRESULTS_TABLE);
			createModuleTable(db);
			createActivityTable(db);
			createLogTable(db);
			createMquizResultsTable(db);
			return;
		}
		
		if(oldVersion <= 7 && newVersion >= 8){
			String sql = "ALTER TABLE " + ACTIVITY_TABLE + " ADD COLUMN " + ACTIVITY_C_STARTDATE + " datetime null;";
			db.execSQL(sql);
			sql = "ALTER TABLE " + ACTIVITY_TABLE + " ADD COLUMN " + ACTIVITY_C_ENDDATE + " datetime null;";
			db.execSQL(sql);
		}
		
		if(oldVersion <= 8 && newVersion >= 9){
			String sql = "ALTER TABLE " + MODULE_TABLE + " ADD COLUMN " + MODULE_C_SCHEDULE + " int null;";
			db.execSQL(sql);
		}
		
		if(oldVersion <= 9 && newVersion >= 10){
			String sql = "ALTER TABLE " + ACTIVITY_TABLE + " ADD COLUMN " + ACTIVITY_C_TITLE  + " text null;";
			db.execSQL(sql);
		}
		
		// This is a fix as previous versioning may not have upgraded db tables correctly
		if(oldVersion <= 10 && newVersion >=11){
			String sql1 = "ALTER TABLE " + ACTIVITY_TABLE + " ADD COLUMN " + ACTIVITY_C_STARTDATE + " datetime null;";
			String sql2 = "ALTER TABLE " + ACTIVITY_TABLE + " ADD COLUMN " + ACTIVITY_C_ENDDATE + " datetime null;";
			String sql3 = "ALTER TABLE " + MODULE_TABLE + " ADD COLUMN " + MODULE_C_SCHEDULE + " int null;";
			String sql4 = "ALTER TABLE " + ACTIVITY_TABLE + " ADD COLUMN " + ACTIVITY_C_TITLE  + " text null;";
			try {
				db.execSQL(sql1);
			} catch (Exception e){
				
			}
			try {
				db.execSQL(sql2);
			} catch (Exception e){
				
			}
			try {
				db.execSQL(sql3);
			} catch (Exception e){
				
			}
			try {
				db.execSQL(sql4);
			} catch (Exception e){
				
			}
		}
	}

	public void onLogout(){
		db.execSQL("DELETE FROM "+ TRACKER_LOG_TABLE);
		db.execSQL("DELETE FROM "+ MQUIZRESULTS_TABLE);
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
		values.put(MODULE_C_SHORTNAME, shortname.toLowerCase(Locale.US));

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

	public int getModuleID(String shortname){
		String s = MODULE_C_SHORTNAME + "=?";
		String[] args = new String[] { shortname };
		Cursor c = db.query(MODULE_TABLE, null, s, args, null, null, null);
		if(c.getCount() == 0){
			c.close();
			return 0;
		} else {
			c.moveToFirst();
			int modId = c.getInt(c.getColumnIndex(MODULE_C_ID));
			c.close();
			return modId;
		}
	}
	
	public void updateScheduleVersion(long modId, long scheduleVersion){
		ContentValues values = new ContentValues();
		values.put(MODULE_C_SCHEDULE, scheduleVersion);
		db.update(MODULE_TABLE, values, MODULE_C_ID + "=" + modId, null);
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
			values.put(ACTIVITY_C_TITLE, a.getTitleJSONString());
			db.insertOrThrow(ACTIVITY_TABLE, null, values);
		}
	}

	public void insertSchedule(ArrayList<ActivitySchedule> actsched) {
		// acts.listIterator();
		for (ActivitySchedule as : actsched) {
			ContentValues values = new ContentValues();
			values.put(ACTIVITY_C_STARTDATE, as.getStartTimeString());
			values.put(ACTIVITY_C_ENDDATE, as.getEndTimeString());
			db.update(ACTIVITY_TABLE, values, ACTIVITY_C_ACTIVITYDIGEST + "='" + as.getDigest() + "'", null);
		}
	}
	
	public void insertTrackers(ArrayList<TrackerLog> trackers, long modId) {
		// acts.listIterator();
		for (TrackerLog t : trackers) {
			ContentValues values = new ContentValues();
			values.put(TRACKER_LOG_C_DATETIME, t.getDateTimeString());
			values.put(TRACKER_LOG_C_ACTIVITYDIGEST, t.getDigest());
			values.put(TRACKER_LOG_C_SUBMITTED, t.isSubmitted());
			values.put(TRACKER_LOG_C_MODID, modId);
			db.insertOrThrow(TRACKER_LOG_TABLE, null, values);
		}
	}
	
	public void resetSchedule(int modId){
		ContentValues values = new ContentValues();
		values.put(ACTIVITY_C_STARTDATE,"");
		values.put(ACTIVITY_C_ENDDATE,"");
		db.update(ACTIVITY_TABLE, values, ACTIVITY_C_MODID + "=" + modId, null);
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
	
	public Module getModule(long modId) {
		Module m = null;
		String s = MODULE_C_ID + "=?";
		String[] args = new String[] { String.valueOf(modId) };
		Cursor c = db.query(MODULE_TABLE, null, s, args, null, null, null);
		c.moveToFirst();
		while (c.isAfterLast() == false) {
			m = new Module();
			m.setModId(c.getInt(c.getColumnIndex(MODULE_C_ID)));
			m.setLocation(c.getString(c.getColumnIndex(MODULE_C_LOCATION)));
			m.setProgress(this.getModuleProgress(m.getModId()));
			c.moveToNext();
		}
		c.close();
		return m;
	}
	
	public long insertLog(int modId, String digest, String data){
		ContentValues values = new ContentValues();
		values.put(TRACKER_LOG_C_MODID, modId);
		values.put(TRACKER_LOG_C_ACTIVITYDIGEST, digest);
		values.put(TRACKER_LOG_C_DATA, data);
		return db.insertOrThrow(TRACKER_LOG_TABLE, null, values);
	}
	
	public float getModuleProgress(int modId){
		String sql = "SELECT a."+ ACTIVITY_C_ID + ", " +
				"l."+ TRACKER_LOG_C_ACTIVITYDIGEST + 
				" as d FROM "+ACTIVITY_TABLE + " a " +
				" LEFT OUTER JOIN (SELECT DISTINCT " +TRACKER_LOG_C_ACTIVITYDIGEST +" FROM "+TRACKER_LOG_TABLE+") l ON a."+ ACTIVITY_C_ACTIVITYDIGEST +" = l."+TRACKER_LOG_C_ACTIVITYDIGEST + 
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
						"l."+ TRACKER_LOG_C_ACTIVITYDIGEST + 
						" as d FROM "+ACTIVITY_TABLE + " a " +
						" LEFT OUTER JOIN (SELECT DISTINCT " +TRACKER_LOG_C_ACTIVITYDIGEST +" FROM "+TRACKER_LOG_TABLE+") l ON a."+ ACTIVITY_C_ACTIVITYDIGEST +" = l."+TRACKER_LOG_C_ACTIVITYDIGEST + 
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
		
		String s = TRACKER_LOG_C_MODID + "=?";
		String[] args = new String[] { String.valueOf(modId) };
		return db.delete(TRACKER_LOG_TABLE, s, args);
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
	
	public boolean toUpdateSchedule(String shortname, Double scheduleVersion){
		String s = MODULE_C_SHORTNAME + "=? AND "+ MODULE_C_SCHEDULE + "< ?";
		String[] args = new String[] { shortname, String.format("%.0f", scheduleVersion) };
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
		String s = TRACKER_LOG_C_SUBMITTED + "=? ";
		String[] args = new String[] { "0" };
		Cursor c = db.query(TRACKER_LOG_TABLE, null, s, args, null, null, null);
		c.moveToFirst();

		ArrayList<Object> sl = new ArrayList<Object>();
		while (c.isAfterLast() == false) {
			TrackerLog so = new TrackerLog();
			so.setId(c.getLong(c.getColumnIndex(TRACKER_LOG_C_ID)));
			so.setDigest(c.getString(c.getColumnIndex(TRACKER_LOG_C_ACTIVITYDIGEST)));
			String content = "";
			try {
				JSONObject json = new JSONObject();
				json.put("data", c.getString(c.getColumnIndex(TRACKER_LOG_C_DATA)));
				json.put("tracker_date", c.getString(c.getColumnIndex(TRACKER_LOG_C_DATETIME)));
				json.put("digest", c.getString(c.getColumnIndex(TRACKER_LOG_C_ACTIVITYDIGEST)));
				content = json.toString();
			} catch (JSONException e) {
				e.printStackTrace();
				BugSenseHandler.sendException(e);
			}
			
			so.setContent(content);
			sl.add(so);
			c.moveToNext();
		}
		Payload p = new Payload(SubmitTrackerTask.SUBMIT_LOG_TASK,sl);
		c.close();
		
		return p;
	}
	
	public int markLogSubmitted(long rowId){
		ContentValues values = new ContentValues();
		values.put(TRACKER_LOG_C_SUBMITTED, 1);
		
		return db.update(TRACKER_LOG_TABLE, values, TRACKER_LOG_C_ID + "=" + rowId, null);
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
		c.moveToFirst();
		ArrayList<Object> sl = new ArrayList<Object>();
		while (c.isAfterLast() == false) {
			TrackerLog so = new TrackerLog();
			so.setId(c.getLong(c.getColumnIndex(MQUIZRESULTS_C_ID)));
			so.setContent(c.getString(c.getColumnIndex(MQUIZRESULTS_C_DATA)));
			sl.add(so);
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
	
	public boolean digestInLog(int modId, String digest){
		String s = TRACKER_LOG_C_ACTIVITYDIGEST + "=? AND " + TRACKER_LOG_C_MODID + "=?";
		String[] args = new String[] { digest, String.valueOf(modId) };
		Cursor c = db.query(TRACKER_LOG_TABLE, null, s, args, null, null, null);
		if(c.getCount() == 0){
			c.close();
			return false;
		} else {
			c.close();
			return true;
		}
	}
	
	public MessageFeed getMessageFeed(){
		MessageFeed mf = new MessageFeed();
		// TODO get messages from db...
		return mf;
	}
	
	public ArrayList<Activity> getActivitiesDue(int max){
		
		ArrayList<Activity> activities = new ArrayList<Activity>();
		DateTime now = new DateTime();
		String nowDateString = MobileLearning.DATE_FORMAT.print(now);
		String sql = "SELECT a.* from "+ ACTIVITY_TABLE + " a " +
					" INNER JOIN " + MODULE_TABLE + " m ON a."+ ACTIVITY_C_MODID + " = m."+MODULE_C_ID +
					" LEFT OUTER JOIN " + TRACKER_LOG_TABLE + " tl ON a."+ ACTIVITY_C_ACTIVITYDIGEST + " = tl."+ TRACKER_LOG_C_ACTIVITYDIGEST +
					" WHERE tl."+TRACKER_LOG_C_ID + " IS NULL "+
					" AND a."+ACTIVITY_C_STARTDATE + "<='" + nowDateString + "'" +
					" ORDER BY a."+ACTIVITY_C_ENDDATE + " ASC" +
					" LIMIT " + max;
							
		
		Cursor c = db.rawQuery(sql,null);
		c.moveToFirst();
		while (c.isAfterLast() == false) {
			Activity a = new Activity();
			if(c.getString(c.getColumnIndex(ACTIVITY_C_TITLE)) != null){
				a.setTitlesFromJSONString(c.getString(c.getColumnIndex(ACTIVITY_C_TITLE)));
				a.setModId(c.getLong(c.getColumnIndex(ACTIVITY_C_MODID)));
				a.setDigest(c.getString(c.getColumnIndex(ACTIVITY_C_ACTIVITYDIGEST)));
				activities.add(a);
			}
			c.moveToNext();
		}
		return activities;
	}
}
