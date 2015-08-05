/* 
 * This file is part of OppiaMobile - https://digital-campus.org/
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

package org.digitalcampus.oppia.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.listener.DBListener;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.ActivitySchedule;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.QuizStats;
import org.digitalcampus.oppia.model.SearchResult;
import org.digitalcampus.oppia.model.TrackerLog;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.xmlreaders.CourseXMLReader;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import com.splunk.mint.Mint;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {

	static final String TAG = DbHelper.class.getSimpleName();
	static final String DB_NAME = "mobilelearning.db";
	static final int DB_VERSION = 18;

	private static SQLiteDatabase db;
	private SharedPreferences prefs;
	
	private static final String COURSE_TABLE = "Module";
	private static final String COURSE_C_ID = BaseColumns._ID;
	private static final String COURSE_C_VERSIONID = "versionid";
	private static final String COURSE_C_TITLE = "title";
	private static final String COURSE_C_DESC = "description";
	private static final String COURSE_C_SHORTNAME = "shortname";
	private static final String COURSE_C_LOCATION = "location";
	private static final String COURSE_C_SCHEDULE = "schedule";
	private static final String COURSE_C_IMAGE = "imagelink";
	private static final String COURSE_C_LANGS = "langs";
	private static final String COURSE_C_ORDER_PRIORITY = "orderpriority";
	
	private static final String ACTIVITY_TABLE = "Activity";
	private static final String ACTIVITY_C_ID = BaseColumns._ID;
	private static final String ACTIVITY_C_COURSEID = "modid"; // reference to
															// COURSE_C_ID
	private static final String ACTIVITY_C_SECTIONID = "sectionid";
	private static final String ACTIVITY_C_ACTID = "activityid";
	private static final String ACTIVITY_C_ACTTYPE = "activitytype";
	private static final String ACTIVITY_C_ACTIVITYDIGEST = "digest";
	private static final String ACTIVITY_C_STARTDATE = "startdate";
	private static final String ACTIVITY_C_ENDDATE = "enddate";
	private static final String ACTIVITY_C_TITLE = "title";

	private static final String TRACKER_LOG_TABLE = "TrackerLog";
	private static final String TRACKER_LOG_C_ID = BaseColumns._ID;
	private static final String TRACKER_LOG_C_COURSEID = "modid"; // reference to COURSE_C_ID
	private static final String TRACKER_LOG_C_DATETIME = "logdatetime";
	private static final String TRACKER_LOG_C_ACTIVITYDIGEST = "digest";
	private static final String TRACKER_LOG_C_DATA = "logdata";
	private static final String TRACKER_LOG_C_SUBMITTED = "logsubmitted";
	private static final String TRACKER_LOG_C_INPROGRESS = "loginprogress";
	private static final String TRACKER_LOG_C_COMPLETED = "completed";
	private static final String TRACKER_LOG_C_USERID = "userid";
	
	private static final String QUIZRESULTS_TABLE = "results";
	private static final String QUIZRESULTS_C_ID = BaseColumns._ID;
	private static final String QUIZRESULTS_C_DATETIME = "resultdatetime";
	private static final String QUIZRESULTS_C_DATA = "content";
	private static final String QUIZRESULTS_C_SENT = "submitted";
	private static final String QUIZRESULTS_C_COURSEID = "moduleid";
	private static final String QUIZRESULTS_C_USERID = "userid";
	
	private static final String SEARCH_TABLE = "search";
	private static final String SEARCH_C_TEXT = "fulltext";
	private static final String SEARCH_C_COURSETITLE = "coursetitle";
	private static final String SEARCH_C_SECTIONTITLE = "sectiontitle";
	private static final String SEARCH_C_ACTIVITYTITLE = "activitytitle";
	
	private static final String USER_TABLE = "user";
	private static final String USER_C_ID = BaseColumns._ID;
	private static final String USER_C_USERNAME = "username";
	private static final String USER_C_FIRSTNAME = "firstname";
	private static final String USER_C_LASTNAME = "lastname";
	private static final String USER_C_PASSWORD = "passwordencrypted";
	private static final String USER_C_APIKEY = "apikey";
		
	// Constructor
	public DbHelper(Context ctx) { //
		super(ctx, DB_NAME, null, DB_VERSION);
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		DatabaseManager.initializeInstance(this);
		db = DatabaseManager.getInstance().openDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createCourseTable(db);
		createActivityTable(db);
		createLogTable(db);
		createQuizResultsTable(db);
		createSearchTable(db);
		createUserTable(db);
	}

	public void createCourseTable(SQLiteDatabase db){
		String m_sql = "create table " + COURSE_TABLE + " (" + COURSE_C_ID + " integer primary key autoincrement, "
				+ COURSE_C_VERSIONID + " int, " + COURSE_C_TITLE + " text, " + COURSE_C_LOCATION + " text, "
				+ COURSE_C_SHORTNAME + " text," + COURSE_C_SCHEDULE + " int,"
				+ COURSE_C_IMAGE + " text,"
				+ COURSE_C_DESC + " text,"
				+ COURSE_C_ORDER_PRIORITY + " integer default 0, " 
				+ COURSE_C_LANGS + " text)";
		db.execSQL(m_sql);
	}
	
	public void createActivityTable(SQLiteDatabase db){
		String a_sql = "create table " + ACTIVITY_TABLE + " (" + 
									ACTIVITY_C_ID + " integer primary key autoincrement, " + 
									ACTIVITY_C_COURSEID + " int, " + 
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
				TRACKER_LOG_C_COURSEID + " integer, " + 
				TRACKER_LOG_C_DATETIME + " datetime default current_timestamp, " + 
				TRACKER_LOG_C_ACTIVITYDIGEST + " text, " + 
				TRACKER_LOG_C_DATA + " text, " + 
				TRACKER_LOG_C_SUBMITTED + " integer default 0, " + 
				TRACKER_LOG_C_INPROGRESS + " integer default 0, " +
				TRACKER_LOG_C_COMPLETED + " integer default 0, " + 
				TRACKER_LOG_C_USERID + " integer default 0 " +
				")";
		db.execSQL(l_sql);
	}

	public void createQuizResultsTable(SQLiteDatabase db){
		String sql = "create table " + QUIZRESULTS_TABLE + " (" + 
							QUIZRESULTS_C_ID + " integer primary key autoincrement, " + 
							QUIZRESULTS_C_DATETIME + " datetime default current_timestamp, " + 
							QUIZRESULTS_C_DATA + " text, " +  
							QUIZRESULTS_C_SENT + " integer default 0, "+
							QUIZRESULTS_C_COURSEID + " integer, " +
							QUIZRESULTS_C_USERID + " integer default 0 )";
		db.execSQL(sql);
	}
	
	public void createSearchTable(SQLiteDatabase db){
		String sql = "CREATE VIRTUAL TABLE "+SEARCH_TABLE+" USING FTS3 (" +
                SEARCH_C_TEXT + " text, " +
                SEARCH_C_COURSETITLE + " text, " +
                SEARCH_C_SECTIONTITLE + " text, " +
                SEARCH_C_ACTIVITYTITLE + " text " +
            ")";
		db.execSQL(sql);
	}
	
	public void createUserTable(SQLiteDatabase db){
		String sql = "CREATE TABLE ["+USER_TABLE+"] (" +
                "["+USER_C_ID+"]" + " integer primary key autoincrement, " +
                "["+USER_C_USERNAME +"]" + " integer default 0, "+
                "["+USER_C_FIRSTNAME +"] TEXT, " +
                "["+USER_C_LASTNAME+"] TEXT, " +
                "["+USER_C_PASSWORD +"] TEXT, " +
                "["+USER_C_APIKEY +"] TEXT " +
            ");";
		db.execSQL(sql);
	}
	
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		if(oldVersion < 7){
			db.execSQL("drop table if exists " + COURSE_TABLE);
			db.execSQL("drop table if exists " + ACTIVITY_TABLE);
			db.execSQL("drop table if exists " + TRACKER_LOG_TABLE);
			db.execSQL("drop table if exists " + QUIZRESULTS_TABLE);
			createCourseTable(db);
			createActivityTable(db);
			createLogTable(db);
			createQuizResultsTable(db);
			return;
		}
		
		if(oldVersion <= 7 && newVersion >= 8){
			String sql = "ALTER TABLE " + ACTIVITY_TABLE + " ADD COLUMN " + ACTIVITY_C_STARTDATE + " datetime null;";
			db.execSQL(sql);
			sql = "ALTER TABLE " + ACTIVITY_TABLE + " ADD COLUMN " + ACTIVITY_C_ENDDATE + " datetime null;";
			db.execSQL(sql);
		}
		
		if(oldVersion <= 8 && newVersion >= 9){
			String sql = "ALTER TABLE " + COURSE_TABLE + " ADD COLUMN " + COURSE_C_SCHEDULE + " int null;";
			db.execSQL(sql);
		}
		
		if(oldVersion <= 9 && newVersion >= 10){
			String sql = "ALTER TABLE " + ACTIVITY_TABLE + " ADD COLUMN " + ACTIVITY_C_TITLE  + " text null;";
			db.execSQL(sql);
		}
		
		// This is a fix as previous versions may not have upgraded db tables correctly
		if(oldVersion <= 10 && newVersion >=11){
			String sql1 = "ALTER TABLE " + ACTIVITY_TABLE + " ADD COLUMN " + ACTIVITY_C_STARTDATE + " datetime null;";
			String sql2 = "ALTER TABLE " + ACTIVITY_TABLE + " ADD COLUMN " + ACTIVITY_C_ENDDATE + " datetime null;";
			String sql3 = "ALTER TABLE " + COURSE_TABLE + " ADD COLUMN " + COURSE_C_SCHEDULE + " int null;";
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
		
		if(oldVersion <= 11 && newVersion >= 12){
			String sql = "ALTER TABLE " + COURSE_TABLE + " ADD COLUMN " + COURSE_C_LANGS  + " text null;";
			db.execSQL(sql);
			sql = "ALTER TABLE " + COURSE_TABLE + " ADD COLUMN " + COURSE_C_IMAGE  + " text null;";
			db.execSQL(sql);
		}
		
		if(oldVersion <= 12 && newVersion >= 13){
			String sql = "ALTER TABLE " + TRACKER_LOG_TABLE + " ADD COLUMN " + TRACKER_LOG_C_COMPLETED  + " integer default 0;";
			db.execSQL(sql);
		}
		// skip jump from 13 to 14
		if(oldVersion <= 14 && newVersion >= 15){
			ContentValues values = new ContentValues();
			values.put(TRACKER_LOG_C_COMPLETED,true);
			db.update(TRACKER_LOG_TABLE, values, null, null);
		}
		
		if(oldVersion <= 15 && newVersion >= 16){
			String sql = "ALTER TABLE " + COURSE_TABLE + " ADD COLUMN " + COURSE_C_DESC + " text null;";
			db.execSQL(sql);
		}
		
		if(oldVersion <= 16 && newVersion >= 17){
			String sql = "ALTER TABLE " + COURSE_TABLE + " ADD COLUMN " + COURSE_C_ORDER_PRIORITY + " integer default 0;";
			db.execSQL(sql);
		}
		
		if(oldVersion <= 17 && newVersion >= 18){
			//create search table
			this.createSearchTable(db);
			
			// alter quiz results table
			String sql1 = "ALTER TABLE " + QUIZRESULTS_TABLE + " ADD COLUMN " + QUIZRESULTS_C_USERID + " integer default 0;";
			db.execSQL(sql1);
			
			// alter tracker table
			String sql2 = "ALTER TABLE " + TRACKER_LOG_TABLE + " ADD COLUMN " + TRACKER_LOG_C_USERID + " integer default 0;";
			db.execSQL(sql2);
			
			// create user table
			this.createUserTable(db);
			
		}	
	
	}

	public void updateV43(long userId){
		// update existing trackers
		ContentValues values = new ContentValues();
		values.put(TRACKER_LOG_C_USERID, userId);
		
		db.update(TRACKER_LOG_TABLE, values, "1=1", null);
		
		// update existing trackers
		ContentValues values2 = new ContentValues();
		values2.put(QUIZRESULTS_C_USERID, userId);
		
		db.update(QUIZRESULTS_TABLE, values2, "1=1", null);
	}
	
	
	// returns id of the row
	public long addOrUpdateCourse(Course course) {

		ContentValues values = new ContentValues();
		values.put(COURSE_C_VERSIONID, course.getVersionId());
		values.put(COURSE_C_TITLE, course.getTitleJSONString());
		values.put(COURSE_C_SHORTNAME, course.getShortname());
		values.put(COURSE_C_LANGS, course.getLangsJSONString());
		values.put(COURSE_C_IMAGE, course.getImageFile());
		values.put(COURSE_C_DESC, course.getDescriptionJSONString());
		values.put(COURSE_C_ORDER_PRIORITY, course.getPriority());
		
		if (!this.isInstalled(course.getShortname())) {
			Log.v(TAG, "Record added");
			return db.insertOrThrow(COURSE_TABLE, null, values);
		} else if(this.toUpdate(course.getShortname(), course.getVersionId())){
			long toUpdate = this.getCourseID(course.getShortname());
			
			// remove existing course info from search index
			this.searchIndexRemoveCourse(toUpdate);
			
			if (toUpdate != 0) {
				db.update(COURSE_TABLE, values, COURSE_C_ID + "=" + toUpdate, null);
				// remove all the old activities
				String s = ACTIVITY_C_COURSEID + "=?";
				String[] args = new String[] { String.valueOf(toUpdate) };
				db.delete(ACTIVITY_TABLE, s, args);
				return toUpdate;
			}
		} 
		return -1;
	}

	// returns id of the row
	public long addOrUpdateUser(User user) {
		ContentValues values = new ContentValues();
		values.put(USER_C_USERNAME, user.getUsername());
		values.put(USER_C_FIRSTNAME, user.getFirstname());
		values.put(USER_C_LASTNAME, user.getLastname());
		values.put(USER_C_PASSWORD, user.getPasswordEncrypted());
		values.put(USER_C_APIKEY, user.getApiKey());
		
		long userId = this.isUser(user.getUsername());
		if (userId == -1) {
			Log.v(TAG, "Record added");
			return db.insertOrThrow(USER_TABLE, null, values);
		} else {
			db.update(USER_TABLE, values, USER_C_ID + "=" + userId, null);
			return userId;
		} 
	}
	
	public long isUser(String username){
		String s = USER_C_USERNAME + "=?";
		String[] args = new String[] { username };
		Cursor c = db.query(USER_TABLE, null, s, args, null, null, null);
		if(c.getCount() == 0){
			c.close();
			return -1;
		} else {
			c.moveToFirst();
			int userId = c.getInt(c.getColumnIndex(USER_C_ID));
			c.close();
			return userId;
		}
	}
	
	
	public int getCourseID(String shortname){
		String s = COURSE_C_SHORTNAME + "=?";
		String[] args = new String[] { shortname };
		Cursor c = db.query(COURSE_TABLE, null, s, args, null, null, null);
		if(c.getCount() == 0){
			c.close();
			return 0;
		} else {
			c.moveToFirst();
			int modId = c.getInt(c.getColumnIndex(COURSE_C_ID));
			c.close();
			return modId;
		}
	}
	
	public void updateScheduleVersion(long modId, long scheduleVersion){
		ContentValues values = new ContentValues();
		values.put(COURSE_C_SCHEDULE, scheduleVersion);
		db.update(COURSE_TABLE, values, COURSE_C_ID + "=" + modId, null);
	}
	
	public void insertActivities(ArrayList<Activity> acts) {
		for (Activity a : acts) {
			ContentValues values = new ContentValues();
			values.put(ACTIVITY_C_COURSEID, a.getCourseId());
			values.put(ACTIVITY_C_SECTIONID, a.getSectionId());
			values.put(ACTIVITY_C_ACTID, a.getActId());
			values.put(ACTIVITY_C_ACTTYPE, a.getActType());
			values.put(ACTIVITY_C_ACTIVITYDIGEST, a.getDigest());
			values.put(ACTIVITY_C_TITLE, a.getTitleJSONString());
			db.insertOrThrow(ACTIVITY_TABLE, null, values);

		}
	}

	public void insertSchedule(ArrayList<ActivitySchedule> actsched) {
		for (ActivitySchedule as : actsched) {
			ContentValues values = new ContentValues();
			values.put(ACTIVITY_C_STARTDATE, as.getStartTimeString());
			values.put(ACTIVITY_C_ENDDATE, as.getEndTimeString());
			db.update(ACTIVITY_TABLE, values, ACTIVITY_C_ACTIVITYDIGEST + "='" + as.getDigest() + "'", null);
		}
	}
	
	public void insertTrackers(ArrayList<TrackerLog> trackers, long courseId) {
		long userId = this.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));
		
		for (TrackerLog t : trackers) {
			ContentValues values = new ContentValues();
			values.put(TRACKER_LOG_C_DATETIME, t.getDateTimeString());
			values.put(TRACKER_LOG_C_ACTIVITYDIGEST, t.getDigest());
			values.put(TRACKER_LOG_C_SUBMITTED, t.isSubmitted());
			values.put(TRACKER_LOG_C_COURSEID, courseId);
			values.put(TRACKER_LOG_C_COMPLETED, true);
			values.put(TRACKER_LOG_C_USERID, userId);
			db.insertOrThrow(TRACKER_LOG_TABLE, null, values);
		}
	}
	
	public void resetSchedule(int courseId){
		ContentValues values = new ContentValues();
		values.put(ACTIVITY_C_STARTDATE,"");
		values.put(ACTIVITY_C_ENDDATE,"");
		db.update(ACTIVITY_TABLE, values, ACTIVITY_C_COURSEID + "=" + courseId, null);
	}
	
	public ArrayList<Course> getAllCourses() {
		ArrayList<Course> courses = new ArrayList<Course>();
		String order = COURSE_C_ORDER_PRIORITY + " DESC, " + COURSE_C_TITLE + " ASC";
		Cursor c = db.query(COURSE_TABLE, null, null, null, null, null, order);
		c.moveToFirst();
		while (c.isAfterLast() == false) {
			Course course = new Course(prefs.getString(PrefsActivity.PREF_STORAGE_LOCATION, ""));
			course.setModId(c.getInt(c.getColumnIndex(COURSE_C_ID)));
			course.setVersionId(c.getDouble(c.getColumnIndex(COURSE_C_VERSIONID)));
			course.setTitlesFromJSONString(c.getString(c.getColumnIndex(COURSE_C_TITLE)));
			course.setImageFile(c.getString(c.getColumnIndex(COURSE_C_IMAGE)));
			course.setLangsFromJSONString(c.getString(c.getColumnIndex(COURSE_C_LANGS)));
			course.setShortname(c.getString(c.getColumnIndex(COURSE_C_SHORTNAME)));
			course.setPriority(c.getInt(c.getColumnIndex(COURSE_C_ORDER_PRIORITY)));
			courses.add(course);
			c.moveToNext();
		}
		c.close();
		return courses;
	}
	
	public ArrayList<Course> getCourses(long userId) {
		ArrayList<Course> courses = new ArrayList<Course>();
		String order = COURSE_C_ORDER_PRIORITY + " DESC, " + COURSE_C_TITLE + " ASC";
		Cursor c = db.query(COURSE_TABLE, null, null, null, null, null, order);
		c.moveToFirst();
		while (c.isAfterLast() == false) {
			
			Course course = new Course(prefs.getString(PrefsActivity.PREF_STORAGE_LOCATION, ""));
			course.setModId(c.getInt(c.getColumnIndex(COURSE_C_ID)));
			course.setVersionId(c.getDouble(c.getColumnIndex(COURSE_C_VERSIONID)));
			course.setTitlesFromJSONString(c.getString(c.getColumnIndex(COURSE_C_TITLE)));
			course.setImageFile(c.getString(c.getColumnIndex(COURSE_C_IMAGE)));
			course.setLangsFromJSONString(c.getString(c.getColumnIndex(COURSE_C_LANGS)));
			course.setShortname(c.getString(c.getColumnIndex(COURSE_C_SHORTNAME)));
			course.setPriority(c.getInt(c.getColumnIndex(COURSE_C_ORDER_PRIORITY)));
			course.setDescriptionsFromJSONString(c.getString(c.getColumnIndex(COURSE_C_DESC)));
			course = this.courseSetProgress(course, userId);
			courses.add(course);
			c.moveToNext();
		}
		c.close();
		return courses;
	}
	
	public Course getCourse(long courseId, long userId) {
		Course course = null;
		String s = COURSE_C_ID + "=?";
		String[] args = new String[] { String.valueOf(courseId) };
		Cursor c = db.query(COURSE_TABLE, null, s, args, null, null, null);
		c.moveToFirst();
		while (c.isAfterLast() == false) {
			course = new Course(prefs.getString(PrefsActivity.PREF_STORAGE_LOCATION, ""));
			course.setModId(c.getInt(c.getColumnIndex(COURSE_C_ID)));
			course.setVersionId(c.getDouble(c.getColumnIndex(COURSE_C_VERSIONID)));
			course.setTitlesFromJSONString(c.getString(c.getColumnIndex(COURSE_C_TITLE)));
			course.setImageFile(c.getString(c.getColumnIndex(COURSE_C_IMAGE)));
			course.setLangsFromJSONString(c.getString(c.getColumnIndex(COURSE_C_LANGS)));
			course.setShortname(c.getString(c.getColumnIndex(COURSE_C_SHORTNAME)));
			course.setPriority(c.getInt(c.getColumnIndex(COURSE_C_ORDER_PRIORITY)));
			course.setDescriptionsFromJSONString(c.getString(c.getColumnIndex(COURSE_C_DESC)));
			course = this.courseSetProgress(course, userId);
			c.moveToNext();
		}
		c.close();
		return course;
	}
	
	private Course courseSetProgress(Course course, long userId){
		// get no activities
		String s = ACTIVITY_C_COURSEID + "=?";
		String[] args = new String[] { String.valueOf(course.getCourseId()) };
		Cursor c = db.query(ACTIVITY_TABLE, null, s, args, null, null, null);
		course.setNoActivities(c.getCount());
		c.close();
		
		// get no completed
		String sqlCompleted = "SELECT DISTINCT " + TRACKER_LOG_C_ACTIVITYDIGEST + " FROM " + TRACKER_LOG_TABLE +
						" WHERE " + TRACKER_LOG_C_COURSEID + "=" + course.getCourseId() + 
						" AND " + TRACKER_LOG_C_USERID + "=" + userId +
						" AND " + TRACKER_LOG_C_COMPLETED + "=1" +
						" AND " + TRACKER_LOG_C_ACTIVITYDIGEST + " IN ( SELECT " + ACTIVITY_C_ACTIVITYDIGEST + " FROM " + ACTIVITY_TABLE + " WHERE " + ACTIVITY_C_COURSEID + "=" + course.getCourseId() + ")";
		c = db.rawQuery(sqlCompleted,null);
		course.setNoActivitiesCompleted(c.getCount());
		c.close();
		
		// get no started
		String sqlStarted = "SELECT DISTINCT " + TRACKER_LOG_C_ACTIVITYDIGEST + " FROM " + TRACKER_LOG_TABLE +
				" WHERE " + TRACKER_LOG_C_COURSEID + "=" + course.getCourseId() + 
				" AND " + TRACKER_LOG_C_USERID + "=" + userId +
				" AND " + TRACKER_LOG_C_COMPLETED + "=0" +
				" AND " + TRACKER_LOG_C_ACTIVITYDIGEST + " NOT IN (" + sqlCompleted + ")" +
				" AND " + TRACKER_LOG_C_ACTIVITYDIGEST + " IN ( SELECT " + ACTIVITY_C_ACTIVITYDIGEST + " FROM " + ACTIVITY_TABLE + " WHERE " + ACTIVITY_C_COURSEID + "=" + course.getCourseId() + ")";
		c = db.rawQuery(sqlStarted,null);
		course.setNoActivitiesStarted(c.getCount());
		c.close();
		
		return course;
	}
	
	public ArrayList<Activity> getCourseActivities(long courseId){
		ArrayList<Activity> activities = new  ArrayList<Activity>();
		String s = ACTIVITY_C_COURSEID + "=?";
		String[] args = new String[] { String.valueOf(courseId) };
		Cursor c = db.query(ACTIVITY_TABLE, null, s, args, null, null, null);
		c.moveToFirst();
		while (c.isAfterLast() == false) {
			Activity activity = new Activity();
			activity.setDbId(c.getInt(c.getColumnIndex(ACTIVITY_C_ID)));
			activity.setTitlesFromJSONString(c.getString(c.getColumnIndex(ACTIVITY_C_TITLE)));
			activities.add(activity);
			c.moveToNext();
		}
		c.close();
		return activities;
	}
	
	public void insertTracker(int modId, String digest, String data, boolean completed){
		//get current user id
		long userId = this.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));
		
		ContentValues values = new ContentValues();
		values.put(TRACKER_LOG_C_COURSEID, modId);
		values.put(TRACKER_LOG_C_ACTIVITYDIGEST, digest);
		values.put(TRACKER_LOG_C_DATA, data);
		values.put(TRACKER_LOG_C_COMPLETED, completed);
		values.put(TRACKER_LOG_C_USERID, userId);
		db.insertOrThrow(TRACKER_LOG_TABLE, null, values);
	}
	
	/*public float getCourseProgress(int courseId, long userId){
		String sql = "SELECT a."+ ACTIVITY_C_ID + ", " +
				"l."+ TRACKER_LOG_C_ACTIVITYDIGEST + 
				" as d FROM "+ACTIVITY_TABLE + " a " +
				" LEFT OUTER JOIN (SELECT DISTINCT " +TRACKER_LOG_C_ACTIVITYDIGEST +" FROM " + TRACKER_LOG_TABLE + 
									" WHERE " + TRACKER_LOG_C_COMPLETED + "=1 AND " + TRACKER_LOG_C_COURSEID + "=" + String.valueOf(courseId) + 
									" AND " + TRACKER_LOG_C_USERID + "=" + String.valueOf(userId) +") l " +
									" ON a."+ ACTIVITY_C_ACTIVITYDIGEST +" = l."+TRACKER_LOG_C_ACTIVITYDIGEST + 
				" WHERE a."+ ACTIVITY_C_COURSEID +"=" + String.valueOf(courseId);
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
	}*/
	
	public int resetCourse(int courseId, long userId){
		// delete quiz results
		this.deleteQuizResults(courseId, userId);
		
		String s = TRACKER_LOG_C_COURSEID + "=? AND " + TRACKER_LOG_C_USERID + "=? ";
		String[] args = new String[] { String.valueOf(courseId), String.valueOf(userId) };
		return db.delete(TRACKER_LOG_TABLE, s, args);
	}
	
	public void deleteCourse(int courseId){
		// remove from search index
		this.searchIndexRemoveCourse(courseId);
		
		// delete activities
		String s = ACTIVITY_C_COURSEID + "=?";
		String[] args = new String[] { String.valueOf(courseId) };
		db.delete(ACTIVITY_TABLE, s, args);
		
		// delete course
		s = COURSE_C_ID + "=?";
		args = new String[] { String.valueOf(courseId) };
		db.delete(COURSE_TABLE, s, args);
		
		
	}
	
	public boolean isInstalled(String shortname){
		String s = COURSE_C_SHORTNAME + "=?";
		String[] args = new String[] { shortname };
		Cursor c = db.query(COURSE_TABLE, null, s, args, null, null, null);
		if(c.getCount() == 0){
			c.close();
			return false;
		} else {
			c.close();
			return true;
		}
	}
	
	public boolean toUpdate(String shortname, Double version){
		String s = COURSE_C_SHORTNAME + "=? AND "+ COURSE_C_VERSIONID + "< ?";
		String[] args = new String[] { shortname, String.format("%.0f", version) };
		Cursor c = db.query(COURSE_TABLE, null, s, args, null, null, null);
		if(c.getCount() == 0){
			c.close();
			return false;
		} else {
			c.close();
			return true;
		}
	}
	
	public boolean toUpdateSchedule(String shortname, Double scheduleVersion){
		String s = COURSE_C_SHORTNAME + "=? AND "+ COURSE_C_SCHEDULE + "< ?";
		String[] args = new String[] { shortname, String.format("%.0f", scheduleVersion) };
		Cursor c = db.query(COURSE_TABLE, null, s, args, null, null, null);
		if(c.getCount() == 0){
			c.close();
			return false;
		} else {
			c.close();
			return true;
		}
	}
	
	public long getUserId(String username){
		String s = USER_C_USERNAME + "=? ";
		String[] args = new String[] { username };
		Cursor c = db.query(USER_TABLE, null, s, args, null, null, null);
		c.moveToFirst();
		long userId = -1;
		while (c.isAfterLast() == false) {
			userId = c.getLong(c.getColumnIndex(USER_C_ID));
			c.moveToNext();
		}
		c.close();
		return userId;
	}
	
	public ArrayList<User> getAllUsers(){
		Cursor c = db.query(USER_TABLE, null, null, null, null, null, null);
		c.moveToFirst();
		
		ArrayList<User> users = new ArrayList<User>();
		while (c.isAfterLast() == false) {
			User u = new User();
			u.setUserid(c.getInt(c.getColumnIndex(USER_C_ID)));
			u.setApiKey(c.getString(c.getColumnIndex(USER_C_APIKEY)));
			u.setUsername(c.getString(c.getColumnIndex(USER_C_USERNAME)));
			u.setFirstname(c.getString(c.getColumnIndex(USER_C_FIRSTNAME)));
			u.setLastname(c.getString(c.getColumnIndex(USER_C_LASTNAME)));
			users.add(u);
			c.moveToNext();
		}
		c.close();
		return users;
	}
	
	public int getSentTrackersCount(long userId){
		String s = TRACKER_LOG_C_SUBMITTED + "=? AND " + TRACKER_LOG_C_USERID + "=? ";
		String[] args = new String[] { "1", String.valueOf(userId) };
		Cursor c = db.query(TRACKER_LOG_TABLE, null, s, args, null, null, null);
		int count = c.getCount();
		c.close();
		return count;
	}
	
	public int getUnsentTrackersCount(long userId){
		String s = TRACKER_LOG_C_SUBMITTED + "=? AND " + TRACKER_LOG_C_USERID + "=? ";
		String[] args = new String[] { "0", String.valueOf(userId) };
		Cursor c = db.query(TRACKER_LOG_TABLE, null, s, args, null, null, null);
		int count = c.getCount();
		c.close();
		return count;
	}
	
	
	public Payload getUnsentTrackers(long userId){
		String s = TRACKER_LOG_C_SUBMITTED + "=? AND " + TRACKER_LOG_C_USERID + "=? ";
		String[] args = new String[] { "0", String.valueOf(userId) };
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
				json.put("completed", c.getInt(c.getColumnIndex(TRACKER_LOG_C_COMPLETED)));
				Course m = this.getCourse(c.getLong(c.getColumnIndex(TRACKER_LOG_C_COURSEID)), userId);
				if (m != null){
					json.put("course", m.getShortname());
				}
				content = json.toString();
			} catch (JSONException e) {
				Mint.logException(e);
				e.printStackTrace();
			}
			
			so.setContent(content);
			sl.add(so);
			c.moveToNext();
		}
		Payload p = new Payload(sl);
		c.close();
		
		return p;
	}
	
	public int markLogSubmitted(long rowId){
		ContentValues values = new ContentValues();
		values.put(TRACKER_LOG_C_SUBMITTED, 1);
		
		return db.update(TRACKER_LOG_TABLE, values, TRACKER_LOG_C_ID + "=" + rowId, null);
	}
	
	public long insertQuizResult(String data, int courseId){
		long userId = this.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));
		ContentValues values = new ContentValues();
		values.put(QUIZRESULTS_C_DATA, data);
		values.put(QUIZRESULTS_C_COURSEID, courseId);
		values.put(QUIZRESULTS_C_USERID, userId);
		return db.insertOrThrow(QUIZRESULTS_TABLE, null, values);
	}
	
	public ArrayList<TrackerLog>  getUnsentQuizResults(long userId){
		String s = QUIZRESULTS_C_SENT + "=? AND " + QUIZRESULTS_C_USERID + "=? ";
		String[] args = new String[] { "0", String.valueOf(userId) };
		Cursor c = db.query(QUIZRESULTS_TABLE, null, s, args, null, null, null);
		c.moveToFirst();
		ArrayList<TrackerLog> sl = new ArrayList<TrackerLog>();
		while (c.isAfterLast() == false) {
			TrackerLog so = new TrackerLog();
			so.setId(c.getLong(c.getColumnIndex(QUIZRESULTS_C_ID)));
			so.setContent(c.getString(c.getColumnIndex(QUIZRESULTS_C_DATA)));
			sl.add(so);
			c.moveToNext();
		}	
		c.close();
		return sl;
	}
	
	public int markQuizSubmitted(long rowId){
		ContentValues values = new ContentValues();
		values.put(QUIZRESULTS_C_SENT, 1);
		
		return db.update(QUIZRESULTS_TABLE, values, QUIZRESULTS_C_ID + "=" + rowId, null);
	}
	
	public void deleteQuizResults(int courseId, long userId){
		// delete any quiz attempts
		String s = QUIZRESULTS_C_COURSEID + "=? AND " + QUIZRESULTS_C_USERID +"=?";
		String[] args = new String[] { String.valueOf(courseId), String.valueOf(userId) };
		db.delete(QUIZRESULTS_TABLE, s, args);
	}
	
	public boolean activityAttempted(long courseId, String digest, long userId){
		String s = TRACKER_LOG_C_ACTIVITYDIGEST + "=? AND " + 
					TRACKER_LOG_C_USERID + "=? AND " +
					TRACKER_LOG_C_COURSEID + "=?";
		String[] args = new String[] { digest, String.valueOf(userId), String.valueOf(courseId) };
		Cursor c = db.query(TRACKER_LOG_TABLE, null, s, args, null, null, null);
		if(c.getCount() == 0){
			c.close();
			return false;
		} else {
			c.close();
			return true;
		}
	}
	
	public boolean activityCompleted(int courseId, String digest, long userId){
		String s = TRACKER_LOG_C_ACTIVITYDIGEST + "=? AND " + 
					TRACKER_LOG_C_COURSEID + "=? AND " + 
					TRACKER_LOG_C_USERID + "=? AND " +
					TRACKER_LOG_C_COMPLETED + "=1";
		String[] args = new String[] { digest, String.valueOf(courseId), String.valueOf(userId) };
		Cursor c = db.query(TRACKER_LOG_TABLE, null, s, args, null, null, null);
		if(c.getCount() == 0){
			c.close();
			return false;
		} else {
			c.close();
			return true;
		}
	}

    public void getCourseQuizResults(ArrayList<QuizStats> stats, int courseId, long userId){

        String quizResultsWhereClause = QUIZRESULTS_C_COURSEID+" =? AND " + QUIZRESULTS_C_USERID + "=?";
        String[] quizResultsArgs = new String[] { String.valueOf(courseId), String.valueOf(userId) };
        String[] quizResultsColumns = new String[]{ QUIZRESULTS_C_DATA};

        //We get the attempts made by the user for this course's quizzes
        Cursor c = db.query(QUIZRESULTS_TABLE, quizResultsColumns, quizResultsWhereClause, quizResultsArgs, null, null, null);
        if (c.getCount() <= 0) return; //we return the empty array

        if (stats == null) stats = new ArrayList<QuizStats>();
        //Instead of parsing each JSON, we extract the data with a RegEx
        Pattern quizDataPattern = Pattern.compile(QuizStats.fromQuizResultRegex);
        Matcher matcher = quizDataPattern.matcher("");

        c.moveToFirst();
        while (!c.isAfterLast()) {
            String quizData = c.getString(c.getColumnIndex(QUIZRESULTS_C_DATA));

            matcher.reset(quizData);
            boolean find = matcher.find();
            if (find){
                //We get the captured parts by the RegEx
                int score = (int)(Float.parseFloat(matcher.group(1)) * 100);
                int maxScore = Integer.parseInt(matcher.group(2)) * 100;
                int quizID = Integer.parseInt(matcher.group(3));

                boolean alreadyInserted = false;
                for (QuizStats quiz : stats){
                    if (quiz.getQuizId() == quizID){
                        if (quiz.getUserScore() < score) quiz.setUserScore(score);
                        if (quiz.getMaxScore() < maxScore) quiz.setMaxScore(maxScore);
                        quiz.setAttempted(true);
                        alreadyInserted = true;
                        break;
                    }
                }
                if (!alreadyInserted){
                    QuizStats quiz = new QuizStats(quizID);
                    quiz.setAttempted(true);
                    quiz.setUserScore(score);
                    quiz.setMaxScore(maxScore);
                    stats.add(quiz);
                }
            }
            c.moveToNext();
        }
        c.close();

    }
	
	public Activity getActivityByDigest(String digest){
		String sql = "SELECT * FROM  "+ ACTIVITY_TABLE + " a " +
					" WHERE " + ACTIVITY_C_ACTIVITYDIGEST + "='"+ digest + "'";
		Cursor c = db.rawQuery(sql,null);
		c.moveToFirst();
		Activity a = new Activity();
		while (c.isAfterLast() == false) {
			
			if(c.getString(c.getColumnIndex(ACTIVITY_C_TITLE)) != null){
				a.setDigest(c.getString(c.getColumnIndex(ACTIVITY_C_ACTIVITYDIGEST)));
				a.setDbId(c.getInt(c.getColumnIndex(ACTIVITY_C_ID)));
				a.setTitlesFromJSONString(c.getString(c.getColumnIndex(ACTIVITY_C_TITLE)));
				a.setSectionId(c.getInt(c.getColumnIndex(ACTIVITY_C_SECTIONID)));
			}
			c.moveToNext();
		}
		c.close();
		return a;
	}
	
	public Activity getActivityByActId(int actId){
		String sql = "SELECT * FROM  "+ ACTIVITY_TABLE + " a " +
					" WHERE " + ACTIVITY_C_ACTID + "="+ actId;
		Cursor c = db.rawQuery(sql,null);
		c.moveToFirst();
		Activity a = new Activity();
		while (c.isAfterLast() == false) {
			
			if(c.getString(c.getColumnIndex(ACTIVITY_C_TITLE)) != null){
				a.setDigest(c.getString(c.getColumnIndex(ACTIVITY_C_ACTIVITYDIGEST)));
				a.setDbId(c.getInt(c.getColumnIndex(ACTIVITY_C_ID)));
				a.setTitlesFromJSONString(c.getString(c.getColumnIndex(ACTIVITY_C_TITLE)));
				a.setSectionId(c.getInt(c.getColumnIndex(ACTIVITY_C_SECTIONID)));
			}
			c.moveToNext();
		}
		c.close();
		return a;
	}
	
	
	public ArrayList<Activity> getActivitiesDue(int max, long userId){
		
		ArrayList<Activity> activities = new ArrayList<Activity>();
		DateTime now = new DateTime();
		String nowDateString = MobileLearning.DATETIME_FORMAT.print(now);
		String sql = "SELECT a.* FROM "+ ACTIVITY_TABLE + " a " +
					" INNER JOIN " + COURSE_TABLE + " m ON a."+ ACTIVITY_C_COURSEID + " = m."+COURSE_C_ID +
					" LEFT OUTER JOIN (SELECT * FROM " + TRACKER_LOG_TABLE + " WHERE " + TRACKER_LOG_C_COMPLETED + "=1 AND "+ TRACKER_LOG_C_USERID +"="+ userId + ") tl ON a."+ ACTIVITY_C_ACTIVITYDIGEST + " = tl."+ TRACKER_LOG_C_ACTIVITYDIGEST +
					" WHERE tl." + TRACKER_LOG_C_ID + " IS NULL "+
					" AND a." + ACTIVITY_C_STARTDATE + "<='" + nowDateString + "'" +
					" AND a." + ACTIVITY_C_TITLE + " IS NOT NULL " +
					" ORDER BY a." + ACTIVITY_C_ENDDATE + " ASC" +
					" LIMIT " + max;
							
		
		Cursor c = db.rawQuery(sql,null);
		c.moveToFirst();
		while (c.isAfterLast() == false) {
			Activity a = new Activity();
			if(c.getString(c.getColumnIndex(ACTIVITY_C_TITLE)) != null){
				a.setTitlesFromJSONString(c.getString(c.getColumnIndex(ACTIVITY_C_TITLE)));
				a.setCourseId(c.getLong(c.getColumnIndex(ACTIVITY_C_COURSEID)));
				a.setDigest(c.getString(c.getColumnIndex(ACTIVITY_C_ACTIVITYDIGEST)));
				activities.add(a);
			}
			c.moveToNext();
		}
		
		if(c.getCount() < max){
			//just add in some extra suggested activities unrelated to the date/time
			String sql2 = "SELECT a.* FROM "+ ACTIVITY_TABLE + " a " +
					" INNER JOIN " + COURSE_TABLE + " m ON a."+ ACTIVITY_C_COURSEID + " = m."+COURSE_C_ID +
					" LEFT OUTER JOIN (SELECT * FROM " + TRACKER_LOG_TABLE + " WHERE " + TRACKER_LOG_C_COMPLETED + "=1) tl ON a."+ ACTIVITY_C_ACTIVITYDIGEST + " = tl."+ TRACKER_LOG_C_ACTIVITYDIGEST +
					" WHERE (tl." + TRACKER_LOG_C_ID + " IS NULL "+
					" OR tl." + TRACKER_LOG_C_COMPLETED + "=0)" +
					" AND a." + ACTIVITY_C_TITLE + " IS NOT NULL " +
					" AND a." + ACTIVITY_C_ID + " NOT IN (SELECT " + ACTIVITY_C_ID + " FROM (" + sql + ") b)" +
					" LIMIT " + (max-c.getCount());
			Cursor c2 = db.rawQuery(sql2,null);
			c2.moveToFirst();
			while (c2.isAfterLast() == false) {
				Activity a = new Activity();
				if(c2.getString(c.getColumnIndex(ACTIVITY_C_TITLE)) != null){
					a.setTitlesFromJSONString(c2.getString(c2.getColumnIndex(ACTIVITY_C_TITLE)));
					a.setCourseId(c2.getLong(c2.getColumnIndex(ACTIVITY_C_COURSEID)));
					a.setDigest(c2.getString(c2.getColumnIndex(ACTIVITY_C_ACTIVITYDIGEST)));
					activities.add(a);
				}
				c2.moveToNext();
			}
			c2.close();
		}
		c.close();
		return activities;
	}
	
	/*
	 * SEARCH Functions
	 * 
	 */
	
	public void searchIndexRemoveCourse(long courseId){
		ArrayList<Activity> activities = this.getCourseActivities(courseId);
		Log.d(TAG,"deleting course from index: "+ courseId);
		for(Activity a: activities){
			this.deleteSearchRow(a.getDbId());
		}
	}
	
	public void insertActivityIntoSearchTable(String courseTitle, String sectionTitle, String activityTitle, int activityDbId, String fullText){
		// strip out all html tags from string (not needed for search)
		String noHTMLString = fullText.replaceAll("\\<.*?\\>", " ");
		
		ContentValues values = new ContentValues();
		values.put("docid", activityDbId);
		values.put(SEARCH_C_TEXT, noHTMLString);
		values.put(SEARCH_C_COURSETITLE, courseTitle);
		values.put(SEARCH_C_SECTIONTITLE, sectionTitle);
		values.put(SEARCH_C_ACTIVITYTITLE, activityTitle);
		db.insertOrThrow(SEARCH_TABLE, null, values);
	}
	
	/*
	 * Perform a search
	 */
	public ArrayList<SearchResult> search(String searchText, int limit, long userId, Context ctx, DBListener listener){
		ArrayList<SearchResult> results = new ArrayList<SearchResult>();
		String sqlSeachFullText = String.format("SELECT c.%s AS courseid, a.%s as activitydigest, a.%s as sectionid, 1 AS ranking FROM %s ft " +
									" INNER JOIN %s a ON a.%s = ft.docid" +
									" INNER JOIN %s c ON a.%s = c.%s " +
									" WHERE %s MATCH '%s' ",
										COURSE_C_ID, ACTIVITY_C_ACTIVITYDIGEST, ACTIVITY_C_SECTIONID, SEARCH_TABLE, 
										ACTIVITY_TABLE, ACTIVITY_C_ID, 
										COURSE_TABLE, ACTIVITY_C_COURSEID, COURSE_C_ID,
										SEARCH_C_TEXT, searchText);
		String sqlActivityTitle = String.format("SELECT c.%s AS courseid, a.%s as activitydigest, a.%s as sectionid, 5 AS ranking FROM %s ft " +
				" INNER JOIN %s a ON a.%s = ft.docid" +
				" INNER JOIN %s c ON a.%s = c.%s " +
				" WHERE %s MATCH '%s' ",
					COURSE_C_ID, ACTIVITY_C_ACTIVITYDIGEST, ACTIVITY_C_SECTIONID, SEARCH_TABLE, 
					ACTIVITY_TABLE, ACTIVITY_C_ID, 
					COURSE_TABLE, ACTIVITY_C_COURSEID, COURSE_C_ID,
					SEARCH_C_ACTIVITYTITLE, searchText);
		
		String sqlSectionTitle = String.format("SELECT c.%s AS courseid, a.%s as activitydigest, a.%s as sectionid, 10 AS ranking FROM %s ft " +
				" INNER JOIN %s a ON a.%s = ft.docid" +
				" INNER JOIN %s c ON a.%s = c.%s " +
				" WHERE %s MATCH '%s' ",
					COURSE_C_ID, ACTIVITY_C_ACTIVITYDIGEST, ACTIVITY_C_SECTIONID, SEARCH_TABLE, 
					ACTIVITY_TABLE, ACTIVITY_C_ID, 
					COURSE_TABLE, ACTIVITY_C_COURSEID, COURSE_C_ID,
					SEARCH_C_SECTIONTITLE, searchText);
		String sqlCourseTitle = String.format("SELECT c.%s AS courseid, a.%s as activitydigest, a.%s as sectionid, 15 AS ranking FROM %s ft " +
				" INNER JOIN %s a ON a.%s = ft.docid" +
				" INNER JOIN %s c ON a.%s = c.%s " +
				" WHERE %s MATCH '%s' ",
					COURSE_C_ID, ACTIVITY_C_ACTIVITYDIGEST, ACTIVITY_C_SECTIONID, SEARCH_TABLE, 
					ACTIVITY_TABLE, ACTIVITY_C_ID, 
					COURSE_TABLE, ACTIVITY_C_COURSEID, COURSE_C_ID,
					SEARCH_C_COURSETITLE, searchText);
		
		String sql = String.format("SELECT DISTINCT courseid, activitydigest FROM ( SELECT * FROM (" +
				"%s UNION %s UNION %s UNION %s) ORDER BY ranking DESC LIMIT 0,%d)",
				sqlSeachFullText, sqlActivityTitle, sqlSectionTitle, sqlCourseTitle, limit);
		
		Cursor c = db.rawQuery(sql,null);
	    if(c !=null && c.getCount()>0){

            //We inform the AsyncTask that the query has been performed
            listener.onQueryPerformed();

            long startTime = System.currentTimeMillis();
            HashMap<Long, Course> fetchedCourses = new HashMap<Long, Course>();
            HashMap<Long, CourseXMLReader> fetchedXMLCourses = new HashMap<Long, CourseXMLReader>();

            c.moveToFirst();
            while (!c.isAfterLast()) {
                SearchResult result = new SearchResult();

                long courseId = c.getLong(c.getColumnIndex("courseid"));
                Course course = fetchedCourses.get(courseId);
                if (course == null){
                    course = this.getCourse(courseId, userId);
                    fetchedCourses.put(courseId, course);
                }
                result.setCourse(course);
	    		
	    		int digest = c.getColumnIndex("activitydigest");
	    		Activity activity = this.getActivityByDigest(c.getString(digest));
	    		result.setActivity(activity);
				
	    		int sectionOrderId = activity.getSectionId();
	    		CourseXMLReader cxr = fetchedXMLCourses.get(courseId);
				try {
                    if (cxr == null){
                        cxr = new CourseXMLReader(course.getCourseXMLLocation(), course.getCourseId(), ctx);
                        fetchedXMLCourses.put(courseId, cxr);
                    }
					result.setSection(cxr.getSection(sectionOrderId));
		    		results.add(result);
				} catch (InvalidXMLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		c.moveToNext();
			}
            long ellapsedTime = System.currentTimeMillis() - startTime;
            Log.d(TAG, "Performing search query and fetching. " + ellapsedTime + " ms ellapsed");
	    }
        if(c !=null) { c.close(); }
	    return results;

	}
	
	/*
	 * Delete the entire search index
	 */
	public void deleteSearchIndex(){
		db.execSQL("DELETE FROM "+ SEARCH_TABLE);
		Log.d(TAG,"Deleted search index...");
	}
	
	/*
	 * Delete a particular activity from the search index
	 */
	public void deleteSearchRow(int activityDbId) {
		String s = "docid=?";
		String[] args = new String[] { String.valueOf(activityDbId) };
		db.delete(SEARCH_TABLE, s, args);
	}
	
	/*
	 * 
	 */
	public boolean isPreviousSectionActivitiesCompleted(Course course, Activity activity, long userId){
		// get this activity
		
		Log.d(TAG,"this digest = " + activity.getDigest());
		Log.d(TAG,"this actid = " + activity.getActId());
		Log.d(TAG,"this modid = " + activity.getCourseId());
		Log.d(TAG,"this sectionid = " + activity.getSectionId());
		// get all the previous activities in this section
		String sql =  String.format("SELECT * FROM " + ACTIVITY_TABLE + 
						" WHERE " + ACTIVITY_C_ACTID + " < %d " +
						" AND " + ACTIVITY_C_COURSEID + " = %d " +
						" AND " + ACTIVITY_C_SECTIONID + " = %d", activity.getActId(), activity.getCourseId(), activity.getSectionId());
		
		Log.d(TAG,"sql: " + sql);
		Cursor c = db.rawQuery(sql,null);
	    if(c !=null && c.getCount()>0){
	    	c.moveToFirst();
	    	boolean completed = true;
	    	// check if each activity has been completed or not
	    	while (c.isAfterLast() == false) {
	    		String sqlCheck = String.format("SELECT * FROM " + TRACKER_LOG_TABLE +
						" WHERE " + TRACKER_LOG_C_ACTIVITYDIGEST + " = '%s'" +
						" AND " + TRACKER_LOG_C_COMPLETED + " =1" +
						" AND " + TRACKER_LOG_C_USERID + " = %d",c.getString(c.getColumnIndex(ACTIVITY_C_ACTIVITYDIGEST)), userId );
	    		Cursor c2 = db.rawQuery(sqlCheck,null);
	    		if (c2 == null || c2.getCount() == 0){
	    			completed = false;
	    			break;
	    		}
	    		c2.close();
	    		c.moveToNext();
	    	}
	    	c.close();
	    	return completed;
	    } else {
	    	c.close();
	    	return true;
	    }
	}
	
	/*
	 * 
	 */
	public boolean isPreviousCourseActivitiesCompleted(Course course, Activity activity, long userId){
		
		Log.d(TAG,"this digest = " + activity.getDigest());
		Log.d(TAG,"this actid = " + activity.getActId());
		Log.d(TAG,"this modid = " + activity.getCourseId());
		Log.d(TAG,"this sectionid = " + activity.getSectionId());
		// get all the previous activities in this section
		String sql =  String.format("SELECT * FROM " + ACTIVITY_TABLE + 
						" WHERE (" + ACTIVITY_C_COURSEID + " = %d " +
						" AND " + ACTIVITY_C_SECTIONID + " < %d )" +
						" OR (" + ACTIVITY_C_ACTID + " < %d " +
						" AND " + ACTIVITY_C_COURSEID + " = %d " +
						" AND " + ACTIVITY_C_SECTIONID + " = %d)", activity.getCourseId(), activity.getSectionId(), activity.getActId(), activity.getCourseId(), activity.getSectionId());
		
		Log.d(TAG,"sql: " + sql);
		Cursor c = db.rawQuery(sql,null);
	    if(c !=null && c.getCount()>0){
	    	c.moveToFirst();
	    	boolean completed = true;
	    	// check if each activity has been completed or not
	    	while (c.isAfterLast() == false) {
	    		String sqlCheck = String.format("SELECT * FROM " + TRACKER_LOG_TABLE +
	    										" WHERE " + TRACKER_LOG_C_ACTIVITYDIGEST + " = '%s'" +
	    										" AND " + TRACKER_LOG_C_COMPLETED + " =1" +
	    										" AND " + TRACKER_LOG_C_USERID + " = %d",c.getString(c.getColumnIndex(ACTIVITY_C_ACTIVITYDIGEST)), userId );
	    		Cursor c2 = db.rawQuery(sqlCheck,null);
	    		if (c2 == null || c2.getCount() == 0){
	    			completed = false;
	    			break;
	    		}
	    		c2.close();
	    		c.moveToNext();
	    	}
	    	c.close();
	    	return completed;
	    } else {
	    	c.close();
	    	return true;
	    }
	}
}
