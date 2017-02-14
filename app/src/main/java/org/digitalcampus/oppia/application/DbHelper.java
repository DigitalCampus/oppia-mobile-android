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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.listener.DBListener;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.ActivitySchedule;
import org.digitalcampus.oppia.model.CompleteCourse;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.QuizAttempt;
import org.digitalcampus.oppia.model.QuizStats;
import org.digitalcampus.oppia.model.SearchResult;
import org.digitalcampus.oppia.model.TrackerLog;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.xmlreaders.CourseXMLReader;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.util.Log;
import android.util.Pair;

import com.splunk.mint.Mint;

public class DbHelper extends SQLiteOpenHelper {

	static final String TAG = DbHelper.class.getSimpleName();
	static final String DB_NAME = "mobilelearning.db";
	static final int DB_VERSION = 25;

    private static DbHelper instance;
	private SQLiteDatabase db;
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
    private static final String COURSE_C_SEQUENCING = "sequencing";
	
	private static final String ACTIVITY_TABLE = "Activity";
	private static final String ACTIVITY_C_ID = BaseColumns._ID;
	private static final String ACTIVITY_C_COURSEID = "modid"; // reference to COURSE_C_ID
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
	private static final String TRACKER_LOG_C_TYPE = "type";
	
	private static final String QUIZATTEMPTS_TABLE = "results";
	private static final String QUIZATTEMPTS_C_ID = BaseColumns._ID;
	private static final String QUIZATTEMPTS_C_DATETIME = "resultdatetime";
	private static final String QUIZATTEMPTS_C_DATA = "content";
	private static final String QUIZATTEMPTS_C_SENT = "submitted";
	private static final String QUIZATTEMPTS_C_COURSEID = "moduleid";
	private static final String QUIZATTEMPTS_C_USERID = "userid";
	private static final String QUIZATTEMPTS_C_SCORE = "score";
	private static final String QUIZATTEMPTS_C_MAXSCORE = "maxscore";
	private static final String QUIZATTEMPTS_C_PASSED = "passed";
	private static final String QUIZATTEMPTS_C_ACTIVITY_DIGEST = "actdigest";

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
	private static final String USER_C_PASSWORDENCRYPTED = "passwordencrypted";
	private static final String USER_C_APIKEY = "apikey";
	private static final String USER_C_LAST_LOGIN_DATE = "lastlogin";
	private static final String USER_C_NO_LOGINS = "nologins";
	private static final String USER_C_POINTS = "points";
	private static final String USER_C_BADGES = "badges";

	private static final String USER_PREFS_TABLE = "userprefs";
    private static final String USER_PREFS_C_USERNAME = "username";
    private static final String USER_PREFS_C_PREFKEY = "preference";
    private static final String USER_PREFS_C_PREFVALUE = "value";

	// Constructor
	private DbHelper(Context ctx) { //
		super(ctx, DB_NAME, null, DB_VERSION);
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        db = this.getWritableDatabase();
	}

    public static synchronized DbHelper getInstance(Context ctx){
        if (instance == null){
            instance = new DbHelper(ctx.getApplicationContext());
        }
        return instance;
    }

    public synchronized void resetDatabase(){
        //Remove the data from all the tables
        List<String> tables = Arrays.asList(USER_PREFS_TABLE, USER_TABLE, SEARCH_TABLE, QUIZATTEMPTS_TABLE,
                                            TRACKER_LOG_TABLE, ACTIVITY_TABLE, COURSE_TABLE);
        for (String tablename : tables){
            db.delete(tablename, null, null);
        }
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		createCourseTable(db);
		createActivityTable(db);
		createLogTable(db);
		createQuizAttemptsTable(db);
		createSearchTable(db);
		createUserTable(db);
        createUserPrefsTable(db);
	}

    public void beginTransaction(){
        db.beginTransaction();
    }
    public void endTransaction(boolean success){
        if (success){
            db.setTransactionSuccessful();
        }
        db.endTransaction();
    }

	public void createCourseTable(SQLiteDatabase db){
		String m_sql = "create table " + COURSE_TABLE + " (" + COURSE_C_ID + " integer primary key autoincrement, "
				+ COURSE_C_VERSIONID + " int, " + COURSE_C_TITLE + " text, " + COURSE_C_LOCATION + " text, "
				+ COURSE_C_SHORTNAME + " text," + COURSE_C_SCHEDULE + " int,"
				+ COURSE_C_IMAGE + " text,"
				+ COURSE_C_DESC + " text,"
				+ COURSE_C_ORDER_PRIORITY + " integer default 0, " 
				+ COURSE_C_LANGS + " text, "
                + COURSE_C_SEQUENCING + " text default '" + Course.SEQUENCING_MODE_NONE + "' )";
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
				TRACKER_LOG_C_USERID + " integer default 0, " +
				TRACKER_LOG_C_TYPE + " text " +
				")";
		db.execSQL(l_sql);
	}

	public void createQuizAttemptsTable(SQLiteDatabase db){
		String sql = "create table " + QUIZATTEMPTS_TABLE + " (" + 
							QUIZATTEMPTS_C_ID + " integer primary key autoincrement, " + 
							QUIZATTEMPTS_C_DATETIME + " datetime default current_timestamp, " + 
							QUIZATTEMPTS_C_DATA + " text, " +  
							QUIZATTEMPTS_C_ACTIVITY_DIGEST + " text, " + 
							QUIZATTEMPTS_C_SENT + " integer default 0, "+
							QUIZATTEMPTS_C_COURSEID + " integer, " +
							QUIZATTEMPTS_C_USERID + " integer default 0, " +
							QUIZATTEMPTS_C_SCORE + " real default 0, " +
							QUIZATTEMPTS_C_MAXSCORE + " real default 0, " +
							QUIZATTEMPTS_C_PASSED + " integer default 0)";
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
                "["+USER_C_USERNAME +"]" + " TEXT, "+
                "["+USER_C_FIRSTNAME +"] TEXT, " +
                "["+USER_C_LASTNAME+"] TEXT, " +
                "["+USER_C_PASSWORDENCRYPTED +"] TEXT, " +
                "["+USER_C_APIKEY +"] TEXT, " +
                "["+USER_C_LAST_LOGIN_DATE +"] datetime null, " +
                "["+USER_C_NO_LOGINS +"] integer default 0,  " +
                "["+USER_C_POINTS +"] integer default 0,  " +
                "["+USER_C_BADGES +"] integer default 0 " +
            ");";
		db.execSQL(sql);
	}

    public void createUserPrefsTable(SQLiteDatabase db){
        String m_sql = "create table " + USER_PREFS_TABLE + " ("
                + USER_PREFS_C_USERNAME + " text not null, "
                + USER_PREFS_C_PREFKEY + " text not null, "
                + USER_PREFS_C_PREFVALUE + " text, "
                + "primary key (" + USER_PREFS_C_USERNAME + ", " + USER_PREFS_C_PREFKEY + ") "
                +  ")";
        db.execSQL(m_sql);
    }
	
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		if(oldVersion < 7){
			db.execSQL("drop table if exists " + COURSE_TABLE);
			db.execSQL("drop table if exists " + ACTIVITY_TABLE);
			db.execSQL("drop table if exists " + TRACKER_LOG_TABLE);
			db.execSQL("drop table if exists " + QUIZATTEMPTS_TABLE);
			createCourseTable(db);
			createActivityTable(db);
			createLogTable(db);
			createQuizAttemptsTable(db);
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
			String sql1 = "ALTER TABLE " + QUIZATTEMPTS_TABLE + " ADD COLUMN " + QUIZATTEMPTS_C_USERID + " integer default 0;";
			db.execSQL(sql1);
			
			// alter tracker table
			String sql2 = "ALTER TABLE " + TRACKER_LOG_TABLE + " ADD COLUMN " + TRACKER_LOG_C_USERID + " integer default 0;";
			db.execSQL(sql2);
			
			// create user table
			this.createUserTable(db);
			
		}	
	
		if(oldVersion <= 18 && newVersion >= 19){
			
			// alter quiz results table
			String sql1 = "ALTER TABLE " + QUIZATTEMPTS_TABLE + " ADD COLUMN " + QUIZATTEMPTS_C_SCORE + " real default 0;";
			db.execSQL(sql1);
			String sql2 = "ALTER TABLE " + QUIZATTEMPTS_TABLE + " ADD COLUMN " + QUIZATTEMPTS_C_PASSED + " integer default 0;";
			db.execSQL(sql2);
			
			// alter user table
			String sql3 = "ALTER TABLE " + USER_TABLE + " ADD COLUMN " + USER_C_LAST_LOGIN_DATE + " datetime null;";
			db.execSQL(sql3);
			String sql4 = "ALTER TABLE " + USER_TABLE + " ADD COLUMN " + USER_C_NO_LOGINS + " integer default 0;";
			db.execSQL(sql4);
		}

		if(oldVersion <= 19 && newVersion >= 20){
			// alter quiz results table
			String sql1 = "ALTER TABLE " + QUIZATTEMPTS_TABLE + " ADD COLUMN " + QUIZATTEMPTS_C_MAXSCORE + " real default 0;";
			db.execSQL(sql1);
		}
		
		if(oldVersion <= 20 && newVersion >= 21){
			// alter quiz results table
			String sql1 = "ALTER TABLE " + QUIZATTEMPTS_TABLE + " ADD COLUMN " + QUIZATTEMPTS_C_ACTIVITY_DIGEST + " text;";
			db.execSQL(sql1);
		}
		
		if(oldVersion <= 21 && newVersion >= 22){
			// add points and badges columns
			String sql1 = "ALTER TABLE " + USER_TABLE + " ADD COLUMN " + USER_C_POINTS + " integer default 0;";
			db.execSQL(sql1);
			String sql2 = "ALTER TABLE " + USER_TABLE + " ADD COLUMN " + USER_C_BADGES + " integer default 0;";
			db.execSQL(sql2);
		}

        if(oldVersion <= 22 && newVersion >= 23){
            // add user preferences table
            db.execSQL("drop table if exists " + USER_PREFS_TABLE);
            createUserPrefsTable(db);
        }

        if(oldVersion <= 23 && newVersion >= 24){
            // add field "sequencingMode" to Course table
            String sql1 = "ALTER TABLE " + COURSE_TABLE + " ADD COLUMN " + COURSE_C_SEQUENCING + " text default '"+Course.SEQUENCING_MODE_NONE+"';";
            db.execSQL(sql1);
        }

		if(oldVersion <= 24 && newVersion >= 25){
			// add field "type" to Tracker table
			String sql1 = "ALTER TABLE " + TRACKER_LOG_TABLE + " ADD COLUMN " + TRACKER_LOG_C_TYPE + " text ;";
			db.execSQL(sql1);
		}
	}

	public void updateV43(long userId){
		// update existing trackers
		ContentValues values = new ContentValues();
		values.put(TRACKER_LOG_C_USERID, userId);
		
		db.update(TRACKER_LOG_TABLE, values, "1=1", null);
		
		// update existing trackers
		ContentValues values2 = new ContentValues();
		values2.put(QUIZATTEMPTS_C_USERID, userId);
		
		db.update(QUIZATTEMPTS_TABLE, values2, "1=1", null);
	}
	
	
	// returns id of the row
	public long addOrUpdateCourse(Course course) {

		ContentValues values = new ContentValues();
		values.put(COURSE_C_VERSIONID, course.getVersionId());
		values.put(COURSE_C_TITLE, course.getMultiLangInfo().getTitleJSONString());
		values.put(COURSE_C_SHORTNAME, course.getShortname());
		values.put(COURSE_C_LANGS, course.getMultiLangInfo().getLangsJSONString());
		values.put(COURSE_C_IMAGE, course.getImageFile());
		values.put(COURSE_C_DESC, course.getMultiLangInfo().getDescriptionJSONString());
		values.put(COURSE_C_ORDER_PRIORITY, course.getPriority());
        values.put(COURSE_C_SEQUENCING, course.getSequencingMode());
		
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
		
		if (user.getUsername().equals("") || user.getUsername() == null){
			return 0;
		}
		
		ContentValues values = new ContentValues();
		values.put(USER_C_USERNAME, user.getUsername());
		values.put(USER_C_FIRSTNAME, user.getFirstname());
		values.put(USER_C_LASTNAME, user.getLastname());
		values.put(USER_C_PASSWORDENCRYPTED, user.getPasswordEncrypted());
		values.put(USER_C_APIKEY, user.getApiKey());
		values.put(USER_C_POINTS, user.getPoints());
		values.put(USER_C_BADGES, user.getBadges());
		
		long userId = this.isUser(user.getUsername());
		if (userId == -1) {
			Log.v(TAG, "Record added");
			return db.insertOrThrow(USER_TABLE, null, values);
		} else {
			String s = USER_C_ID + "=?";
			String[] args = new String[] { String.valueOf(userId) };
			db.update(USER_TABLE, values, s, args);
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
			int courseId = c.getInt(c.getColumnIndex(COURSE_C_ID));
			c.close();
			return courseId;
		}
	}
	
	public void updateScheduleVersion(long courseId, long scheduleVersion){
		ContentValues values = new ContentValues();
		values.put(COURSE_C_SCHEDULE, scheduleVersion);
		db.update(COURSE_TABLE, values, COURSE_C_ID + "=" + courseId, null);
	}
	
	public void insertActivities(ArrayList<Activity> acts) {

        beginTransaction();
		for (Activity a : acts) {
			ContentValues values = new ContentValues();
			values.put(ACTIVITY_C_COURSEID, a.getCourseId());
			values.put(ACTIVITY_C_SECTIONID, a.getSectionId());
			values.put(ACTIVITY_C_ACTID, a.getActId());
			values.put(ACTIVITY_C_ACTTYPE, a.getActType());
			values.put(ACTIVITY_C_ACTIVITYDIGEST, a.getDigest());
			values.put(ACTIVITY_C_TITLE, a.getMultiLangInfo().getTitleJSONString());
			db.insertOrThrow(ACTIVITY_TABLE, null, values);
		}
        endTransaction(true);
	}

	public void insertSchedule(ArrayList<ActivitySchedule> actsched) {

        beginTransaction();
		for (ActivitySchedule as : actsched) {
			ContentValues values = new ContentValues();
			values.put(ACTIVITY_C_STARTDATE, as.getStartTimeString());
			values.put(ACTIVITY_C_ENDDATE, as.getEndTimeString());
			db.update(ACTIVITY_TABLE, values, ACTIVITY_C_ACTIVITYDIGEST + "='" + as.getDigest() + "'", null);
		}
        endTransaction(true);
	}
	
	public void insertTrackers(ArrayList<TrackerLog> trackers) {
        beginTransaction();
		for (TrackerLog t : trackers) {
			ContentValues values = new ContentValues();
			values.put(TRACKER_LOG_C_DATETIME, t.getDateTimeString());
			values.put(TRACKER_LOG_C_ACTIVITYDIGEST, t.getDigest());
			values.put(TRACKER_LOG_C_SUBMITTED, t.isSubmitted());
			values.put(TRACKER_LOG_C_COURSEID, t.getCourseId());
			values.put(TRACKER_LOG_C_COMPLETED, t.isCompleted());
			values.put(TRACKER_LOG_C_USERID, t.getUserId());
			db.insertOrThrow(TRACKER_LOG_TABLE, null, values);
		}
        endTransaction(true);
	}
	
	public void resetSchedule(int courseId){
		ContentValues values = new ContentValues();
		values.put(ACTIVITY_C_STARTDATE,"");
		values.put(ACTIVITY_C_ENDDATE, "");
		db.update(ACTIVITY_TABLE, values, ACTIVITY_C_COURSEID + "=" + courseId, null);
	}
	
	public ArrayList<Course> getAllCourses() {
		ArrayList<Course> courses = new ArrayList<Course>();
		String order = COURSE_C_ORDER_PRIORITY + " DESC, " + COURSE_C_TITLE + " ASC";
		Cursor c = db.query(COURSE_TABLE, null, null, null, null, null, order);
		c.moveToFirst();
		while (c.isAfterLast() == false) {
			Course course = new Course(prefs.getString(PrefsActivity.PREF_STORAGE_LOCATION, ""));
			course.setCourseId(c.getInt(c.getColumnIndex(COURSE_C_ID)));
			course.setVersionId(c.getDouble(c.getColumnIndex(COURSE_C_VERSIONID)));
			course.getMultiLangInfo().setTitlesFromJSONString(c.getString(c.getColumnIndex(COURSE_C_TITLE)));
			course.setImageFile(c.getString(c.getColumnIndex(COURSE_C_IMAGE)));
			course.getMultiLangInfo().setLangsFromJSONString(c.getString(c.getColumnIndex(COURSE_C_LANGS)));
			course.setShortname(c.getString(c.getColumnIndex(COURSE_C_SHORTNAME)));
			course.setPriority(c.getInt(c.getColumnIndex(COURSE_C_ORDER_PRIORITY)));
            course.setSequencingMode(c.getString(c.getColumnIndex(COURSE_C_SEQUENCING)));
			courses.add(course);
			c.moveToNext();
		}
		c.close();
		return courses;
	}
	
	public ArrayList<QuizAttempt> getAllQuizAttempts() {
		ArrayList<QuizAttempt> quizAttempts = new ArrayList<QuizAttempt>();
		Cursor c = db.query(QUIZATTEMPTS_TABLE, null, null, null, null, null, null);
		c.moveToFirst();
		while (c.isAfterLast() == false) {
			QuizAttempt qa = new QuizAttempt();
			qa.setId(c.getInt(c.getColumnIndex(QUIZATTEMPTS_C_ID)));
			qa.setActivityDigest(c.getString(c.getColumnIndex(QUIZATTEMPTS_C_ACTIVITY_DIGEST)));
			qa.setData(c.getString(c.getColumnIndex(QUIZATTEMPTS_C_DATA)));
			qa.setSent(Boolean.parseBoolean(c.getString(c.getColumnIndex(QUIZATTEMPTS_C_SENT))));
			qa.setCourseId(c.getLong(c.getColumnIndex(QUIZATTEMPTS_C_COURSEID)));
			qa.setUserId(c.getLong(c.getColumnIndex(QUIZATTEMPTS_C_USERID)));
			qa.setScore(c.getFloat(c.getColumnIndex(QUIZATTEMPTS_C_SCORE)));
			qa.setMaxscore(c.getFloat(c.getColumnIndex(QUIZATTEMPTS_C_MAXSCORE)));
			qa.setPassed(Boolean.parseBoolean(c.getString(c.getColumnIndex(QUIZATTEMPTS_C_PASSED))));
			quizAttempts.add(qa);
			c.moveToNext();
		}
		c.close();
		return quizAttempts;
	}
	
	public ArrayList<Course> getCourses(long userId) {
		ArrayList<Course> courses = new ArrayList<Course>();
		String order = COURSE_C_ORDER_PRIORITY + " DESC, " + COURSE_C_TITLE + " ASC";
		Cursor c = db.query(COURSE_TABLE, null, null, null, null, null, order);
		c.moveToFirst();
		while (c.isAfterLast() == false) {
			
			Course course = new Course(prefs.getString(PrefsActivity.PREF_STORAGE_LOCATION, ""));
			course.setCourseId(c.getInt(c.getColumnIndex(COURSE_C_ID)));
			course.setVersionId(c.getDouble(c.getColumnIndex(COURSE_C_VERSIONID)));
			course.getMultiLangInfo().setTitlesFromJSONString(c.getString(c.getColumnIndex(COURSE_C_TITLE)));
			course.setImageFile(c.getString(c.getColumnIndex(COURSE_C_IMAGE)));
			course.getMultiLangInfo().setLangsFromJSONString(c.getString(c.getColumnIndex(COURSE_C_LANGS)));
			course.setShortname(c.getString(c.getColumnIndex(COURSE_C_SHORTNAME)));
			course.setPriority(c.getInt(c.getColumnIndex(COURSE_C_ORDER_PRIORITY)));
			course.getMultiLangInfo().setDescriptionsFromJSONString(c.getString(c.getColumnIndex(COURSE_C_DESC)));
            course.setSequencingMode(c.getString(c.getColumnIndex(COURSE_C_SEQUENCING)));
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
			course.setCourseId(c.getInt(c.getColumnIndex(COURSE_C_ID)));
            course.setVersionId(c.getDouble(c.getColumnIndex(COURSE_C_VERSIONID)));
            course.getMultiLangInfo().setTitlesFromJSONString(c.getString(c.getColumnIndex(COURSE_C_TITLE)));
			course.setImageFile(c.getString(c.getColumnIndex(COURSE_C_IMAGE)));
            course.getMultiLangInfo().setLangsFromJSONString(c.getString(c.getColumnIndex(COURSE_C_LANGS)));
            course.setShortname(c.getString(c.getColumnIndex(COURSE_C_SHORTNAME)));
            course.setPriority(c.getInt(c.getColumnIndex(COURSE_C_ORDER_PRIORITY)));
            course.getMultiLangInfo().setDescriptionsFromJSONString(c.getString(c.getColumnIndex(COURSE_C_DESC)));
            course.setSequencingMode(c.getString(c.getColumnIndex(COURSE_C_SEQUENCING)));
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
			activity.setDigest(c.getString(c.getColumnIndex(ACTIVITY_C_ACTIVITYDIGEST)));
			activity.getMultiLangInfo().setTitlesFromJSONString(c.getString(c.getColumnIndex(ACTIVITY_C_TITLE)));
			activities.add(activity);
			c.moveToNext();
		}
		c.close();
		return activities;
	}
	
	public ArrayList<Activity> getCourseQuizzes(long courseId){
		ArrayList<Activity> quizzes = new  ArrayList<Activity>();
		String s = ACTIVITY_C_COURSEID + "=? AND " + ACTIVITY_C_ACTTYPE +"=? AND " + ACTIVITY_C_SECTIONID+">0";
		String[] args = new String[] { String.valueOf(courseId), "quiz" };
		Cursor c = db.query(ACTIVITY_TABLE, null, s, args, null, null, null);
		c.moveToFirst();
		while (c.isAfterLast() == false) {
			Activity quiz = new Activity();
			quiz.setDbId(c.getInt(c.getColumnIndex(ACTIVITY_C_ID)));
			quiz.setDigest(c.getString(c.getColumnIndex(ACTIVITY_C_ACTIVITYDIGEST)));
			quiz.getMultiLangInfo().setTitlesFromJSONString(c.getString(c.getColumnIndex(ACTIVITY_C_TITLE)));
			quizzes.add(quiz);
			c.moveToNext();
		}
		c.close();
		return quizzes;
	}
	
	public QuizStats getQuizAttempt(String digest, long userId){
		QuizStats qs = new QuizStats();
		qs.setDigest(digest);
		qs.setAttempted(false);
		qs.setPassed(false);
		
		// find if attempted
		String s1 = QUIZATTEMPTS_C_USERID + "=? AND " + QUIZATTEMPTS_C_ACTIVITY_DIGEST +"=?";
		String[] args1 = new String[] { String.valueOf(userId), digest };
		Cursor c1 = db.query(QUIZATTEMPTS_TABLE, null, s1, args1, null, null, null);
        qs.setNumAttempts(c1.getCount());
		if (c1.getCount() == 0){ return qs; }
		c1.moveToFirst();
		while (c1.isAfterLast() == false) {
			float userScore = c1.getFloat(c1.getColumnIndex(QUIZATTEMPTS_C_SCORE));
			if (userScore > qs.getUserScore()){
				qs.setUserScore(userScore);
			}
			if (c1.getInt(c1.getColumnIndex(QUIZATTEMPTS_C_PASSED)) != 0){
				qs.setPassed(true);
			}
			qs.setMaxScore(c1.getFloat(c1.getColumnIndex(QUIZATTEMPTS_C_MAXSCORE)));
			c1.moveToNext();
		}
		c1.close();
		qs.setAttempted(true);
		
		return qs;
	}
	public void insertTracker(int courseId, String digest, String data, String type, boolean completed){
		//get current user id
		long userId = this.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));
		
		ContentValues values = new ContentValues();
		values.put(TRACKER_LOG_C_COURSEID, courseId);
		values.put(TRACKER_LOG_C_ACTIVITYDIGEST, digest);
		values.put(TRACKER_LOG_C_DATA, data);
		values.put(TRACKER_LOG_C_COMPLETED, completed);
		values.put(TRACKER_LOG_C_USERID, userId);
		values.put(TRACKER_LOG_C_TYPE, type);
		db.insertOrThrow(TRACKER_LOG_TABLE, null, values);
	}
	
	public void resetCourse(long courseId, long userId){
		// delete quiz results
		this.deleteQuizAttempts(courseId, userId);
		this.deleteTrackers(courseId, userId);
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
	
	public User getUser(long userId) throws UserNotFoundException {
		String s = USER_C_ID + "=? ";
		String[] args = new String[] { String.valueOf(userId) };
		Cursor c = db.query(USER_TABLE, null, s, args, null, null, null);
		c.moveToFirst();
		User u = null;
		while (c.isAfterLast() == false) {
			u = new User();
			u.setUserId(c.getLong(c.getColumnIndex(USER_C_ID)));
			u.setApiKey(c.getString(c.getColumnIndex(USER_C_APIKEY)));
			u.setUsername(c.getString(c.getColumnIndex(USER_C_USERNAME)));
			u.setFirstname(c.getString(c.getColumnIndex(USER_C_FIRSTNAME)));
			u.setLastname(c.getString(c.getColumnIndex(USER_C_LASTNAME)));
			u.setPoints(c.getInt(c.getColumnIndex(USER_C_POINTS)));
			u.setBadges(c.getInt(c.getColumnIndex(USER_C_BADGES)));
			u.setPasswordEncrypted(c.getString(c.getColumnIndex(USER_C_PASSWORDENCRYPTED)));
			c.moveToNext();
		}
		c.close();
		if (u == null){
			throw new UserNotFoundException();
		}
		return u;
	}
	
	public User getUser(String userName) throws UserNotFoundException {
		String s = USER_C_USERNAME + "=? ";
		String[] args = new String[] { userName };
		Cursor c = db.query(USER_TABLE, null, s, args, null, null, null);
		c.moveToFirst();
		User u = null;
		while (c.isAfterLast() == false) {
			u = new User();
			u.setUserId(c.getLong(c.getColumnIndex(USER_C_ID)));
			u.setApiKey(c.getString(c.getColumnIndex(USER_C_APIKEY)));
			u.setUsername(c.getString(c.getColumnIndex(USER_C_USERNAME)));
			u.setFirstname(c.getString(c.getColumnIndex(USER_C_FIRSTNAME)));
			u.setLastname(c.getString(c.getColumnIndex(USER_C_LASTNAME)));
			u.setPoints(c.getInt(c.getColumnIndex(USER_C_POINTS)));
			u.setBadges(c.getInt(c.getColumnIndex(USER_C_BADGES)));
			u.setPasswordEncrypted(c.getString(c.getColumnIndex(USER_C_PASSWORDENCRYPTED)));
			c.moveToNext();
		}
		c.close();
		if (u == null){
			throw new UserNotFoundException();
		}
		return u;
	}
	
	public void updateUserPoints(String userName, int points){
		ContentValues values = new ContentValues();
		values.put(USER_C_POINTS, points);
		String s = USER_C_USERNAME + "=? ";
		String[] args = new String[] { userName };
		db.update(USER_TABLE, values, s ,args);
	}
	
	public void updateUserBadges(String userName, int badges){
		ContentValues values = new ContentValues();
		values.put(USER_C_BADGES, badges);
		String s = USER_C_USERNAME + "=? ";
		String[] args = new String[] { userName };
		db.update(USER_TABLE, values, s ,args);
	}
	
	public void updateUserPoints(long userId, int points){
		ContentValues values = new ContentValues();
		values.put(USER_C_POINTS, points);
		String s = USER_C_ID + "=? ";
		String[] args = new String[] { String.valueOf(userId) };
		db.update(USER_TABLE, values, s ,args);
	}
	
	public void updateUserBadges(long userId, int badges){
		ContentValues values = new ContentValues();
		values.put(USER_C_BADGES, badges);
		String s = USER_C_ID + "=? ";
		String[] args = new String[] { String.valueOf(userId) };
		db.update(USER_TABLE, values, s ,args);
	}
	
	public ArrayList<User> getAllUsers(){
		Cursor c = db.query(USER_TABLE, null, null, null, null, null, null);
		c.moveToFirst();
		
		ArrayList<User> users = new ArrayList<User>();
		while (c.isAfterLast() == false) {
			User u = new User();
			u.setUserId(c.getInt(c.getColumnIndex(USER_C_ID)));
			u.setApiKey(c.getString(c.getColumnIndex(USER_C_APIKEY)));
			u.setUsername(c.getString(c.getColumnIndex(USER_C_USERNAME)));
			u.setFirstname(c.getString(c.getColumnIndex(USER_C_FIRSTNAME)));
			u.setLastname(c.getString(c.getColumnIndex(USER_C_LASTNAME)));
			u.setPoints(c.getInt(c.getColumnIndex(USER_C_POINTS)));
			u.setBadges(c.getInt(c.getColumnIndex(USER_C_BADGES)));
			u.setPasswordEncrypted(c.getString(c.getColumnIndex(USER_C_PASSWORDENCRYPTED)));
			users.add(u);
			c.moveToNext();
		}
		c.close();
		return users;
	}
	
	public int getSentTrackersCount(){
		String s = TRACKER_LOG_C_SUBMITTED + "=? ";
		String[] args = new String[] { "1"};
		Cursor c = db.query(TRACKER_LOG_TABLE, null, s, args, null, null, null);
		int count = c.getCount();
		c.close();
		return count;
	}
	
	public int getUnsentTrackersCount(){
		String s = TRACKER_LOG_C_SUBMITTED + "=? ";
		String[] args = new String[] { "0" };
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
		while (!c.isAfterLast()) {
			TrackerLog so = new TrackerLog();
			String digest = c.getString(c.getColumnIndex(TRACKER_LOG_C_ACTIVITYDIGEST));
			so.setId(c.getLong(c.getColumnIndex(TRACKER_LOG_C_ID)));
			so.setDigest(digest);
			String content = "";
			try {
				JSONObject json = new JSONObject();
				json.put("data", c.getString(c.getColumnIndex(TRACKER_LOG_C_DATA)));
				json.put("tracker_date", c.getString(c.getColumnIndex(TRACKER_LOG_C_DATETIME)));
				json.put("completed", c.getInt(c.getColumnIndex(TRACKER_LOG_C_COMPLETED)));
				json.put("digest", (digest!=null) ? digest : "");
				Course m = this.getCourse(c.getLong(c.getColumnIndex(TRACKER_LOG_C_COURSEID)), userId);
				if (m != null){
					json.put("course", m.getShortname());
				}
				String trackerType = c.getString(c.getColumnIndex(TRACKER_LOG_C_TYPE));
				if (trackerType != null){
					json.put("type",trackerType);
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
	
	public long insertQuizAttempt(QuizAttempt qa){
		ContentValues values = new ContentValues();
		values.put(QUIZATTEMPTS_C_DATA, qa.getData());
		values.put(QUIZATTEMPTS_C_COURSEID, qa.getCourseId());
		values.put(QUIZATTEMPTS_C_USERID, qa.getUserId());
		values.put(QUIZATTEMPTS_C_MAXSCORE, qa.getMaxscore());
		values.put(QUIZATTEMPTS_C_SCORE, qa.getScore());
		values.put(QUIZATTEMPTS_C_PASSED, qa.isPassed());
		values.put(QUIZATTEMPTS_C_ACTIVITY_DIGEST, qa.getActivityDigest());
		return db.insertOrThrow(QUIZATTEMPTS_TABLE, null, values);
	}
	
	public void updateQuizAttempt(QuizAttempt qa){
		ContentValues values = new ContentValues();
		values.put(QUIZATTEMPTS_C_DATA, qa.getData());
		values.put(QUIZATTEMPTS_C_COURSEID, qa.getCourseId());
		values.put(QUIZATTEMPTS_C_USERID, qa.getUserId());
		values.put(QUIZATTEMPTS_C_MAXSCORE, qa.getMaxscore());
		values.put(QUIZATTEMPTS_C_SCORE, qa.getScore());
		values.put(QUIZATTEMPTS_C_PASSED, qa.isPassed());
		values.put(QUIZATTEMPTS_C_ACTIVITY_DIGEST, qa.getActivityDigest());
		db.update(QUIZATTEMPTS_TABLE, values, QUIZATTEMPTS_C_ID + "=" + qa.getId(), null);
	}
	
	public void insertQuizAttempts(ArrayList<QuizAttempt> quizAttempts){
        beginTransaction();
        for (QuizAttempt qa : quizAttempts) {
            ContentValues values = new ContentValues();
            values.put(QUIZATTEMPTS_C_DATA, qa.getData());
            values.put(QUIZATTEMPTS_C_COURSEID, qa.getCourseId());
            values.put(QUIZATTEMPTS_C_USERID, qa.getUserId());
            values.put(QUIZATTEMPTS_C_MAXSCORE, qa.getMaxscore());
            values.put(QUIZATTEMPTS_C_SCORE, qa.getScore());
            values.put(QUIZATTEMPTS_C_PASSED, qa.isPassed());
            values.put(QUIZATTEMPTS_C_ACTIVITY_DIGEST, qa.getActivityDigest());
            values.put(QUIZATTEMPTS_C_SENT, qa.isSent());
            values.put(QUIZATTEMPTS_C_DATETIME, qa.getDateTimeString());
            db.insertOrThrow(QUIZATTEMPTS_TABLE, null, values);
        }
        endTransaction(true);
	}
	
	public ArrayList<QuizAttempt>  getUnsentQuizAttempts(){
		String s = QUIZATTEMPTS_C_SENT + "=? ";
		String[] args = new String[] { "0" };
		Cursor c = db.query(QUIZATTEMPTS_TABLE, null, s, args, null, null, null);
		c.moveToFirst();
		ArrayList<QuizAttempt> quizAttempts = new ArrayList<QuizAttempt>();
		while (c.isAfterLast() == false) {
			try {
				QuizAttempt qa = new QuizAttempt();
				qa.setId(c.getLong(c.getColumnIndex(QUIZATTEMPTS_C_ID)));
				qa.setData(c.getString(c.getColumnIndex(QUIZATTEMPTS_C_DATA)));
				qa.setUserId(c.getLong(c.getColumnIndex(QUIZATTEMPTS_C_USERID)));
				User u = this.getUser(qa.getUserId());
				qa.setUser(u);
				quizAttempts.add(qa);
			} catch (UserNotFoundException unfe){
				// do nothing
			}
			c.moveToNext();
		}	
		c.close();
		return quizAttempts;
	}
	
	public int markQuizSubmitted(long rowId){
		ContentValues values = new ContentValues();
		values.put(QUIZATTEMPTS_C_SENT, 1);
		
		String s = QUIZATTEMPTS_C_ID + "=? ";
		String[] args = new String[] { String.valueOf(rowId) };
		return db.update(QUIZATTEMPTS_TABLE, values, s, args);
	}
	
	public void deleteQuizAttempts(long courseId, long userId){
		// delete any quiz attempts
		String s = QUIZATTEMPTS_C_COURSEID + "=? AND " + QUIZATTEMPTS_C_USERID +"=?";
		String[] args = new String[] { String.valueOf(courseId), String.valueOf(userId) };
		db.delete(QUIZATTEMPTS_TABLE, s, args);
	}
	
	public void deleteTrackers(long courseId, long userId){
		// delete any trackers
		String s = TRACKER_LOG_C_COURSEID + "=? AND " + TRACKER_LOG_C_USERID + "=? ";
		String[] args = new String[] { String.valueOf(courseId), String.valueOf(userId) };
		db.delete(TRACKER_LOG_TABLE, s, args);
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

        String quizResultsWhereClause = QUIZATTEMPTS_C_COURSEID+" =? AND " + QUIZATTEMPTS_C_USERID + "=?";
        String[] quizResultsArgs = new String[] { String.valueOf(courseId), String.valueOf(userId) };
        String[] quizResultsColumns = new String[]{ QUIZATTEMPTS_C_ACTIVITY_DIGEST, QUIZATTEMPTS_C_PASSED, QUIZATTEMPTS_C_MAXSCORE, QUIZATTEMPTS_C_SCORE};

        //We get the attempts made by the user for this course's quizzes
        Cursor c = db.query(QUIZATTEMPTS_TABLE, quizResultsColumns, quizResultsWhereClause, quizResultsArgs, null, null, null);
        if (c.getCount() <= 0) return; //we return the empty array

        if (stats == null) stats = new ArrayList<QuizStats>();

        c.moveToFirst();
        while (!c.isAfterLast()) {
            String quizDigest = c.getString(c.getColumnIndex(QUIZATTEMPTS_C_ACTIVITY_DIGEST));
            int score = (int)(c.getFloat(c.getColumnIndex(QUIZATTEMPTS_C_SCORE)) * 100);
            int maxScore = (int)(c.getFloat(c.getColumnIndex(QUIZATTEMPTS_C_MAXSCORE)) * 100);
            boolean passed = c.getInt(c.getColumnIndex(QUIZATTEMPTS_C_PASSED))>0;

            boolean alreadyInserted = false;
            for (QuizStats quiz : stats){
                if (quiz.getDigest().equals(quizDigest)){
                    if (quiz.getUserScore() < score) quiz.setUserScore(score);
                    if (quiz.getMaxScore() < maxScore) quiz.setMaxScore(maxScore);
                    quiz.setAttempted(true);
                    quiz.setPassed(passed);
                    Log.d(TAG, "quiz score: " + quiz.getUserScore());
                    Log.d(TAG, "quiz passed: " + quiz.isPassed());
                    alreadyInserted = true;
                    break;
                }
            }
            if (!alreadyInserted){
                QuizStats quiz = new QuizStats();
                quiz.setAttempted(true);
                quiz.setDigest(quizDigest);
                quiz.setUserScore(score);
                quiz.setMaxScore(maxScore);
                quiz.setPassed(passed);
                stats.add(quiz);
            }

            c.moveToNext();
        }
        c.close();

    }

	
	public Activity getActivityByDigest(String digest){
		String sql = "SELECT * FROM  "+ ACTIVITY_TABLE + " a " +
					" WHERE " + ACTIVITY_C_ACTIVITYDIGEST + "='"+ digest + "'";
		Cursor c = db.rawQuery(sql,null);

        if (c.getCount() <= 0){
            c.close();
            return null;
        }
		c.moveToFirst();
		Activity a = new Activity();
		while (!c.isAfterLast()) {
			if(c.getString(c.getColumnIndex(ACTIVITY_C_TITLE)) != null){
                a.setCourseId(c.getLong(c.getColumnIndex(ACTIVITY_C_COURSEID)));
				a.setDigest(c.getString(c.getColumnIndex(ACTIVITY_C_ACTIVITYDIGEST)));
                a.setActType(c.getString(c.getColumnIndex(ACTIVITY_C_ACTTYPE)));
				a.setDbId(c.getInt(c.getColumnIndex(ACTIVITY_C_ID)));
				a.getMultiLangInfo().setTitlesFromJSONString(c.getString(c.getColumnIndex(ACTIVITY_C_TITLE)));
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
		while (!c.isAfterLast()) {
			
			if(c.getString(c.getColumnIndex(ACTIVITY_C_TITLE)) != null){
				a.setDigest(c.getString(c.getColumnIndex(ACTIVITY_C_ACTIVITYDIGEST)));
				a.setDbId(c.getInt(c.getColumnIndex(ACTIVITY_C_ID)));
				a.getMultiLangInfo().setTitlesFromJSONString(c.getString(c.getColumnIndex(ACTIVITY_C_TITLE)));
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
				a.getMultiLangInfo().setTitlesFromJSONString(c.getString(c.getColumnIndex(ACTIVITY_C_TITLE)));
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
					a.getMultiLangInfo().setTitlesFromJSONString(c2.getString(c2.getColumnIndex(ACTIVITY_C_TITLE)));
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
		Log.d(TAG, "deleting course from index: " + courseId);
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
            Map<Long, Course> fetchedCourses = new HashMap<>();
            Map<Long, CompleteCourse> fetchedXMLCourses = new HashMap<>();

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
	    		CompleteCourse parsed = fetchedXMLCourses.get(courseId);
                if (parsed == null){
                    try {
                        CourseXMLReader cxr = new CourseXMLReader(course.getCourseXMLLocation(), course.getCourseId(), ctx);
                        cxr.parse(CourseXMLReader.ParseMode.COMPLETE);
                        parsed = cxr.getParsedCourse();
                        fetchedXMLCourses.put(courseId, parsed);
                        result.setSection(parsed.getSection(sectionOrderId));
                        results.add(result);
                    } catch (InvalidXMLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                else{
                    result.setSection(parsed.getSection(sectionOrderId));
                    results.add(result);
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
		db.execSQL("DELETE FROM " + SEARCH_TABLE);
		Log.d(TAG, "Deleted search index...");
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
		Log.d(TAG,"this courseid = " + activity.getCourseId());
		Log.d(TAG,"this sectionid = " + activity.getSectionId());
		// get all the previous activities in this section
		String sql =  String.format("SELECT * FROM " + ACTIVITY_TABLE + 
						" WHERE " + ACTIVITY_C_ACTID + " < %d " +
						" AND " + ACTIVITY_C_COURSEID + " = %d " +
						" AND " + ACTIVITY_C_SECTIONID + " = %d", activity.getActId(), activity.getCourseId(), activity.getSectionId());
		
		Log.d(TAG, "sql: " + sql);
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
		Log.d(TAG,"this courseid = " + activity.getCourseId());
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

    public void insertUserPreferences(String username, List<Pair<String, String>> preferences){
        beginTransaction();
        for (Pair<String, String> prefence : preferences) {
            ContentValues values = new ContentValues();
            values.put(USER_PREFS_C_USERNAME, username);
            values.put(USER_PREFS_C_PREFKEY, prefence.first);
            values.put(USER_PREFS_C_PREFVALUE, prefence.second);
            db.insertWithOnConflict(USER_PREFS_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
        endTransaction(true);
    }

    public List<Pair<String, String>> getUserPreferences(String username){
        ArrayList<Pair<String, String>> prefs = new ArrayList<>();
        String whereClause = USER_PREFS_C_USERNAME + "=? ";
        String[] args = new String[] { username };

        Cursor c = db.query(USER_PREFS_TABLE, null, whereClause, args, null, null, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {

            String prefKey = c.getString(c.getColumnIndex(USER_PREFS_C_PREFKEY));
            String prefValue = c.getString(c.getColumnIndex(USER_PREFS_C_PREFVALUE));
            Pair<String, String> pref = new Pair<>(prefKey, prefValue);
            prefs.add(pref);

            c.moveToNext();
        }
        c.close();

        return prefs;
    }

    public String getUserPreference(String username, String preferenceKey){
        String whereClause = USER_PREFS_C_USERNAME + "=? AND " + USER_PREFS_C_PREFKEY + "=? ";
        String[] args = new String[] { username, preferenceKey };

        String prefValue = null;
        Cursor c = db.query(USER_PREFS_TABLE, null, whereClause, args, null, null, null);
        if (c.getCount() > 0){
            c.moveToFirst();
            prefValue = c.getString(c.getColumnIndex(USER_PREFS_C_PREFVALUE));
        }

        c.close();
        return prefValue;
    }
}
