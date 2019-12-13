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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.gamification.Gamification;
import org.digitalcampus.oppia.gamification.PointsComparator;
import org.digitalcampus.oppia.listener.DBListener;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.ActivitySchedule;
import org.digitalcampus.oppia.model.CompleteCourse;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.GamificationEvent;
import org.digitalcampus.oppia.model.LeaderboardPosition;
import org.digitalcampus.oppia.model.Points;
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

	private static final String TAG = DbHelper.class.getSimpleName();
	private static final String DB_NAME = "mobilelearning.db";
	private static final int DB_VERSION = 30;

    private static DbHelper instance;
	private SQLiteDatabase db;
	private SharedPreferences prefs;
	private Context ctx;
	
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

    private static final String COURSE_GAME_TABLE = "CourseGamification";
    private static final String COURSE_GAME_C_ID = BaseColumns._ID;
    private static final String COURSE_GAME_C_COURSEID = "courseid"; // reference to COURSE_C_ID
    private static final String COURSE_GAME_C_EVENT = "event";
    private static final String COURSE_GAME_C_POINTS = "points";

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

    private static final String ACTIVITY_GAME_TABLE = "ActivityGamification";
    private static final String ACTIVITY_GAME_C_ID = BaseColumns._ID;
    private static final String ACTIVITY_GAME_C_ACTIVITYID = "activityid"; // reference to ACTIVITY_C_ID
    private static final String ACTIVITY_GAME_C_EVENT = "event";
    private static final String ACTIVITY_GAME_C_POINTS = "points";

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
	private static final String TRACKER_LOG_C_EXPORTED = "exported";
    private static final String TRACKER_LOG_C_EVENT = "event";
    private static final String TRACKER_LOG_C_POINTS = "points";
	
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
	private static final String QUIZATTEMPTS_C_EXPORTED = "exported";
    private static final String QUIZATTEMPTS_C_EVENT = "event";
    private static final String QUIZATTEMPTS_C_POINTS = "points";

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
	private static final String USER_C_PASSWORDENCRYPTED = "passwordencrypted"; //NOSONAR
	private static final String USER_C_PASSWORDPLAIN = "passwordplain"; //NOSONAR
	private static final String USER_C_APIKEY = "apikey";
	private static final String USER_C_LAST_LOGIN_DATE = "lastlogin";
	private static final String USER_C_NO_LOGINS = "nologins";
	private static final String USER_C_POINTS = "points";
	private static final String USER_C_BADGES = "badges";
	private static final String USER_C_OFFLINE_REGISTER = "offlineregister";
	private static final String USER_C_EMAIL = "email";
	private static final String USER_C_JOBTITLE = "jobTitle";
	private static final String USER_C_ORGANIZATION = "organisation";
	private static final String USER_C_PHONE = "phoneNo";

	private static final String USER_PREFS_TABLE = "userprefs";
    private static final String USER_PREFS_C_USERNAME = "username";
    private static final String USER_PREFS_C_PREFKEY = "preference";
    private static final String USER_PREFS_C_PREFVALUE = "value";

	private static final String LEADERBOARD_TABLE = "leaderboard";
	private static final String LEADERBOARD_C_USERNAME = "username";
	private static final String LEADERBOARD_C_FULLNAME = "fullname";
	private static final String LEADERBOARD_C_POINTS = "points";
	private static final String LEADERBOARD_C_LASTUPDATE = "lastupdate";

	// Constructor
	private DbHelper(Context ctx) { //
		super(ctx, DB_NAME, null, DB_VERSION);
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        db = this.getWritableDatabase();
        this.ctx = ctx;
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
        createCourseGamificationTable(db);
		createActivityGamificationTable(db);
		createLeaderboardTable(db);
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

	private void createCourseTable(SQLiteDatabase db){
		String mSql = "create table " + COURSE_TABLE + " (" + COURSE_C_ID + " integer primary key autoincrement, "
				+ COURSE_C_VERSIONID + " int, " + COURSE_C_TITLE + " text, " + COURSE_C_LOCATION + " text, "
				+ COURSE_C_SHORTNAME + " text," + COURSE_C_SCHEDULE + " int,"
				+ COURSE_C_IMAGE + " text,"
				+ COURSE_C_DESC + " text,"
				+ COURSE_C_ORDER_PRIORITY + " integer default 0, " 
				+ COURSE_C_LANGS + " text, "
                + COURSE_C_SEQUENCING + " text default '" + Course.SEQUENCING_MODE_NONE + "' )";
		db.execSQL(mSql);
	}
	
	private void createActivityTable(SQLiteDatabase db){
		String aSql = "create table " + ACTIVITY_TABLE + " (" +
									ACTIVITY_C_ID + " integer primary key autoincrement, " + 
									ACTIVITY_C_COURSEID + " int, " + 
									ACTIVITY_C_SECTIONID + " int, " + 
									ACTIVITY_C_ACTID + " int, " + 
									ACTIVITY_C_ACTTYPE + " text, " + 
									ACTIVITY_C_STARTDATE + " datetime null, " + 
									ACTIVITY_C_ENDDATE + " datetime null, " + 
									ACTIVITY_C_ACTIVITYDIGEST + " text, "+
									ACTIVITY_C_TITLE + " text)";
		db.execSQL(aSql);
	}
	
	private void createLogTable(SQLiteDatabase db){
		String lSql = "create table " + TRACKER_LOG_TABLE + " (" +
				TRACKER_LOG_C_ID + " integer primary key autoincrement, " + 
				TRACKER_LOG_C_COURSEID + " integer, " + 
				TRACKER_LOG_C_DATETIME + " datetime default current_timestamp, " + 
				TRACKER_LOG_C_ACTIVITYDIGEST + " text, " + 
				TRACKER_LOG_C_DATA + " text, " + 
				TRACKER_LOG_C_SUBMITTED + " integer default 0, " + 
				TRACKER_LOG_C_INPROGRESS + " integer default 0, " +
				TRACKER_LOG_C_COMPLETED + " integer default 0, " + 
				TRACKER_LOG_C_USERID + " integer default 0, " +
				TRACKER_LOG_C_TYPE + " text, " +
				TRACKER_LOG_C_EXPORTED + " integer default 0, " +
                TRACKER_LOG_C_EVENT + " text, " +
                TRACKER_LOG_C_POINTS + " integer default 0 " +
				")";
		db.execSQL(lSql);
	}

	private void createQuizAttemptsTable(SQLiteDatabase db){
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
							QUIZATTEMPTS_C_PASSED + " integer default 0, " +
							QUIZATTEMPTS_C_EXPORTED + " integer default 0," +
                            QUIZATTEMPTS_C_EVENT + " text, " +
                            QUIZATTEMPTS_C_POINTS + " integer default 0 )";
		db.execSQL(sql);
	}
	
	private void createSearchTable(SQLiteDatabase db){
		String sql = "CREATE VIRTUAL TABLE "+SEARCH_TABLE+" USING FTS3 (" +
                SEARCH_C_TEXT + " text, " +
                SEARCH_C_COURSETITLE + " text, " +
                SEARCH_C_SECTIONTITLE + " text, " +
                SEARCH_C_ACTIVITYTITLE + " text " +
            ")";
		db.execSQL(sql);
	}
	
	private void createUserTable(SQLiteDatabase db){
		String sql = "CREATE TABLE ["+USER_TABLE+"] (" +
                "["+USER_C_ID+"]" + " integer primary key autoincrement, " +
                "["+USER_C_USERNAME +"]" + " TEXT, "+
                "["+USER_C_FIRSTNAME +"] TEXT, " +
                "["+USER_C_LASTNAME+"] TEXT, " +
                "["+USER_C_PASSWORDENCRYPTED +"] TEXT, " +
				"["+USER_C_PASSWORDPLAIN +"] TEXT, " +
                "["+USER_C_APIKEY +"] TEXT, " +
				"["+USER_C_EMAIL +"] TEXT, " +
				"["+USER_C_PHONE +"] TEXT, " +
				"["+USER_C_JOBTITLE +"] TEXT, " +
				"["+USER_C_ORGANIZATION +"] TEXT, " +
                "["+USER_C_LAST_LOGIN_DATE +"] datetime null, " +
                "["+USER_C_NO_LOGINS +"] integer default 0,  " +
                "["+USER_C_POINTS +"] integer default 0,  " +
                "["+USER_C_BADGES +"] integer default 0, " +
				 "["+USER_C_OFFLINE_REGISTER+"] integer default 0 "+
            ");";
		db.execSQL(sql);
	}

    public void createUserPrefsTable(SQLiteDatabase db){
        String mSql = "create table " + USER_PREFS_TABLE + " ("
                + USER_PREFS_C_USERNAME + " text not null, "
                + USER_PREFS_C_PREFKEY + " text not null, "
                + USER_PREFS_C_PREFVALUE + " text, "
                + "primary key (" + USER_PREFS_C_USERNAME + ", " + USER_PREFS_C_PREFKEY + ") "
                +  ")";
        db.execSQL(mSql);
    }

    public void createCourseGamificationTable(SQLiteDatabase db){
        String mSql = "create table " + COURSE_GAME_TABLE + " ("
                + COURSE_GAME_C_ID + " integer primary key autoincrement, "
                + COURSE_GAME_C_COURSEID + " integer,"
                + COURSE_GAME_C_EVENT + " text,"
                + COURSE_GAME_C_POINTS + " integer default 0 )";
        db.execSQL(mSql);
    }

	private void createActivityGamificationTable(SQLiteDatabase db){
		String mSql = "create table " + ACTIVITY_GAME_TABLE + " ("
				+ ACTIVITY_GAME_C_ID + " integer primary key autoincrement, "
				+ ACTIVITY_GAME_C_ACTIVITYID + " integer,"
				+ ACTIVITY_GAME_C_EVENT + " text,"
				+ ACTIVITY_GAME_C_POINTS + " integer default 0 )";
		db.execSQL(mSql);
	}

	private void createLeaderboardTable(SQLiteDatabase db){
		String sql = "create table " + LEADERBOARD_TABLE + " (" +
				LEADERBOARD_C_USERNAME + " text, " +
				LEADERBOARD_C_FULLNAME + " text, " +
				LEADERBOARD_C_POINTS + " integer default 0, " +
				LEADERBOARD_C_LASTUPDATE + " datetime default current_timestamp )";
		db.execSQL(sql);
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

			db.execSQL(sql1);
			db.execSQL(sql2);
			db.execSQL(sql3);
			db.execSQL(sql4);
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

		if(oldVersion <= 25 && newVersion >= 26){
			// add field "exported" to Tracker table
			String sql1 = "ALTER TABLE " + TRACKER_LOG_TABLE + " ADD COLUMN " + TRACKER_LOG_C_EXPORTED + " integer default 0;";
			db.execSQL(sql1);
		}

		if(oldVersion <= 26 && newVersion >= 27){
			// add field "exported" to Tracker table
			String sql1 = "ALTER TABLE " + QUIZATTEMPTS_TABLE + " ADD COLUMN " + QUIZATTEMPTS_C_EXPORTED + " integer default 0;";
			db.execSQL(sql1);
		}

        if(oldVersion <= 27 && newVersion >= 28){
		    createCourseGamificationTable(db);
            createActivityGamificationTable(db);

            // update tracker table
            String sql1 = "ALTER TABLE " + TRACKER_LOG_TABLE + " ADD COLUMN " + TRACKER_LOG_C_POINTS + " integer default 0;";
            String sql2 = "ALTER TABLE " + TRACKER_LOG_TABLE + " ADD COLUMN " + TRACKER_LOG_C_EVENT  + " text null;";
            db.execSQL(sql1);
            db.execSQL(sql2);

            // update quizattempt table
            String sql3 = "ALTER TABLE " + QUIZATTEMPTS_TABLE + " ADD COLUMN " + QUIZATTEMPTS_C_POINTS + " integer default 0;";
            String sql4 = "ALTER TABLE " + QUIZATTEMPTS_TABLE + " ADD COLUMN " + QUIZATTEMPTS_C_EVENT  + " text null;";
            db.execSQL(sql3);
            db.execSQL(sql4);
        }

		if(oldVersion <= 28 && newVersion >= 29){
			// add leaderboard table
			db.execSQL("drop table if exists " + LEADERBOARD_TABLE);
			createLeaderboardTable(db);
		}

		if(oldVersion <= 29 && newVersion >= 30){
			// add fields for offline_register to User table
			db.execSQL("ALTER TABLE " + USER_TABLE + " ADD COLUMN " + USER_C_OFFLINE_REGISTER + " integer default 0;");
			db.execSQL("ALTER TABLE " + USER_TABLE + " ADD COLUMN " + USER_C_PASSWORDPLAIN + " text null;");
			db.execSQL("ALTER TABLE " + USER_TABLE + " ADD COLUMN " + USER_C_EMAIL + " text null;");
			db.execSQL("ALTER TABLE " + USER_TABLE + " ADD COLUMN " + USER_C_PHONE + " text null;");
			db.execSQL("ALTER TABLE " + USER_TABLE + " ADD COLUMN " + USER_C_JOBTITLE + " text null;");
			db.execSQL("ALTER TABLE " + USER_TABLE + " ADD COLUMN " + USER_C_ORGANIZATION + " text null;");
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
		values.put(COURSE_C_TITLE, course.getTitleJSONString());
		values.put(COURSE_C_SHORTNAME, course.getShortname());
		values.put(COURSE_C_LANGS, course.getLangsJSONString());
		values.put(COURSE_C_IMAGE, course.getImageFile());
		values.put(COURSE_C_DESC, course.getDescriptionJSONString());
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

				// remove coursegamification
				// remove activitygamification

				return toUpdate;
			}
		} 
		return -1;
	}

	public void insertActivityGamification(long activityId, List<GamificationEvent> gamificationEvents){

        beginTransaction();
		for (GamificationEvent event : gamificationEvents) {
            ContentValues values = new ContentValues();
            values.put(ACTIVITY_GAME_C_ACTIVITYID, activityId);
            values.put(ACTIVITY_GAME_C_EVENT, event.getEvent());
            values.put(ACTIVITY_GAME_C_POINTS, event.getPoints());
            db.insertOrThrow(ACTIVITY_GAME_TABLE, null, values);
        }
        endTransaction(true);
    }

    public void insertCourseGamification(long courseId, List<GamificationEvent> gamificationEvents){

        beginTransaction();
        for (GamificationEvent event : gamificationEvents) {
            ContentValues values = new ContentValues();
            values.put(COURSE_GAME_C_COURSEID, courseId);
            values.put(COURSE_GAME_C_EVENT, event.getEvent());
            values.put(COURSE_GAME_C_POINTS, event.getPoints());
            db.insertOrThrow(COURSE_GAME_TABLE, null, values);
        }
        endTransaction(true);
    }



	// returns id of the row
	public long addOrUpdateUser(User user) {
		
		if (user.getUsername() == null || user.getUsername().equals("")){
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
		values.put(USER_C_OFFLINE_REGISTER, user.isOfflineRegister());
		values.put(USER_C_EMAIL, user.getEmail());
		values.put(USER_C_PHONE, user.getPhoneNo());
		values.put(USER_C_JOBTITLE, user.getJobTitle());
		values.put(USER_C_ORGANIZATION, user.getOrganisation());
		values.put(USER_C_PASSWORDPLAIN, user.isOfflineRegister() ? user.getPassword() : "");
		
		long userId = this.isUser(user.getUsername());
		if (userId == -1) {
			Log.v(TAG, "Record added");
			userId = db.insertOrThrow(USER_TABLE, null, values);
			this.insertOrUpdateUserLeaderboard(user.getUsername(), user.getDisplayName(), user.getPoints(), new DateTime());

		} else {
			String s = USER_C_ID + "=?";
			String[] args = new String[] { String.valueOf(userId) };
			db.update(USER_TABLE, values, s, args);
		}
		return userId;
	}

	public void deleteUser(String username) {
		// delete activities
		String s = USER_C_USERNAME + "=?";
		String[] args = new String[]{String.valueOf(username)};
		db.delete(USER_TABLE, s, args);
	}

	public long isUser(String username){
		String s = USER_C_USERNAME + "=? COLLATE NOCASE";
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
	
	public void insertActivities(List<Activity> acts) {

        beginTransaction();
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
        endTransaction(true);
	}

	public void insertSchedule(List<ActivitySchedule> actsched) {

        beginTransaction();
		for (ActivitySchedule as : actsched) {
			ContentValues values = new ContentValues();
			values.put(ACTIVITY_C_STARTDATE, as.getStartTimeString());
			values.put(ACTIVITY_C_ENDDATE, as.getEndTimeString());
			db.update(ACTIVITY_TABLE, values, ACTIVITY_C_ACTIVITYDIGEST + "='" + as.getDigest() + "'", null);
		}
        endTransaction(true);
	}
	
	public void insertTrackers(List<TrackerLog> trackers) {
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
	
	public List<Course> getAllCourses() {
		ArrayList<Course> courses = new ArrayList<>();
		String order = COURSE_C_ORDER_PRIORITY + " DESC, " + COURSE_C_TITLE + " ASC";
		Cursor c = db.query(COURSE_TABLE, null, null, null, null, null, order);
		c.moveToFirst();
		while (!c.isAfterLast()) {
			Course course = new Course(prefs.getString(PrefsActivity.PREF_STORAGE_LOCATION, ""));
			course.setCourseId(c.getInt(c.getColumnIndex(COURSE_C_ID)));
			course.setVersionId(c.getDouble(c.getColumnIndex(COURSE_C_VERSIONID)));
			course.setTitlesFromJSONString(c.getString(c.getColumnIndex(COURSE_C_TITLE)));
			course.setImageFile(c.getString(c.getColumnIndex(COURSE_C_IMAGE)));
			course.setLangsFromJSONString(c.getString(c.getColumnIndex(COURSE_C_LANGS)));
			course.setShortname(c.getString(c.getColumnIndex(COURSE_C_SHORTNAME)));
			course.setPriority(c.getInt(c.getColumnIndex(COURSE_C_ORDER_PRIORITY)));
            course.setSequencingMode(c.getString(c.getColumnIndex(COURSE_C_SEQUENCING)));
			courses.add(course);
			c.moveToNext();
		}
		c.close();
		return courses;
	}
	
	public List<QuizAttempt> getAllQuizAttempts() {
		ArrayList<QuizAttempt> quizAttempts = new ArrayList<>();
		Cursor c = db.query(QUIZATTEMPTS_TABLE, null, null, null, null, null, null);
		c.moveToFirst();
		while (!c.isAfterLast()) {
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
	
	public List<Course> getCourses(long userId) {
		ArrayList<Course> courses = new ArrayList<>();
		String order = COURSE_C_ORDER_PRIORITY + " DESC, " + COURSE_C_TITLE + " ASC";
		Cursor c = db.query(COURSE_TABLE, null, null, null, null, null, order);
		c.moveToFirst();
		while (!c.isAfterLast()) {
			
			Course course = new Course(prefs.getString(PrefsActivity.PREF_STORAGE_LOCATION, ""));
			course.setCourseId(c.getInt(c.getColumnIndex(COURSE_C_ID)));
			course.setVersionId(c.getDouble(c.getColumnIndex(COURSE_C_VERSIONID)));
			course.setTitlesFromJSONString(c.getString(c.getColumnIndex(COURSE_C_TITLE)));
			course.setImageFile(c.getString(c.getColumnIndex(COURSE_C_IMAGE)));
			course.setLangsFromJSONString(c.getString(c.getColumnIndex(COURSE_C_LANGS)));
			course.setShortname(c.getString(c.getColumnIndex(COURSE_C_SHORTNAME)));
			course.setPriority(c.getInt(c.getColumnIndex(COURSE_C_ORDER_PRIORITY)));
			course.setDescriptionsFromJSONString(c.getString(c.getColumnIndex(COURSE_C_DESC)));
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
		while (!c.isAfterLast()) {
			course = new Course(prefs.getString(PrefsActivity.PREF_STORAGE_LOCATION, ""));
			course.setCourseId(c.getInt(c.getColumnIndex(COURSE_C_ID)));
            course.setVersionId(c.getDouble(c.getColumnIndex(COURSE_C_VERSIONID)));
            course.setTitlesFromJSONString(c.getString(c.getColumnIndex(COURSE_C_TITLE)));
			course.setImageFile(c.getString(c.getColumnIndex(COURSE_C_IMAGE)));
            course.setLangsFromJSONString(c.getString(c.getColumnIndex(COURSE_C_LANGS)));
            course.setShortname(c.getString(c.getColumnIndex(COURSE_C_SHORTNAME)));
            course.setPriority(c.getInt(c.getColumnIndex(COURSE_C_ORDER_PRIORITY)));
            course.setDescriptionsFromJSONString(c.getString(c.getColumnIndex(COURSE_C_DESC)));
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
	
	public List<Activity> getCourseActivities(long courseId){
		ArrayList<Activity> activities = new  ArrayList<>();
		String s = ACTIVITY_C_COURSEID + "=?";
		String[] args = new String[] { String.valueOf(courseId) };
		Cursor c = db.query(ACTIVITY_TABLE, null, s, args, null, null, null);
		c.moveToFirst();
		while (!c.isAfterLast()) {
			Activity activity = new Activity();
			activity.setDbId(c.getInt(c.getColumnIndex(ACTIVITY_C_ID)));
			activity.setDigest(c.getString(c.getColumnIndex(ACTIVITY_C_ACTIVITYDIGEST)));
			activity.setTitlesFromJSONString(c.getString(c.getColumnIndex(ACTIVITY_C_TITLE)));
			activities.add(activity);
			c.moveToNext();
		}
		c.close();
		return activities;
	}

    public List<GamificationEvent> getCourseGamification(long courseId){
        ArrayList<GamificationEvent> events = new  ArrayList<>();
        String s = COURSE_GAME_C_COURSEID + "=?";
        String[] args = new String[] { String.valueOf(courseId) };
        Cursor c = db.query(COURSE_GAME_TABLE, null, s, args, null, null, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            GamificationEvent event = new GamificationEvent();
            event.setEvent(c.getString(c.getColumnIndex(COURSE_GAME_C_EVENT)));
            event.setPoints(c.getInt(c.getColumnIndex(COURSE_GAME_C_POINTS)));
            events.add(event);
            c.moveToNext();
        }
        c.close();
        return events;
    }

	public ArrayList<Activity> getCourseQuizzes(long courseId){
		ArrayList<Activity> quizzes = new  ArrayList<>();
		String s = ACTIVITY_C_COURSEID + "=? AND " + ACTIVITY_C_ACTTYPE +"=? AND " + ACTIVITY_C_SECTIONID+">0";
		String[] args = new String[] { String.valueOf(courseId), "quiz" };
		Cursor c = db.query(ACTIVITY_TABLE, null, s, args, null, null, null);
		c.moveToFirst();
		while (!c.isAfterLast()) {
			Activity quiz = new Activity();
			quiz.setDbId(c.getInt(c.getColumnIndex(ACTIVITY_C_ID)));
			quiz.setDigest(c.getString(c.getColumnIndex(ACTIVITY_C_ACTIVITYDIGEST)));
			quiz.setTitlesFromJSONString(c.getString(c.getColumnIndex(ACTIVITY_C_TITLE)));
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
		while (!c1.isAfterLast()) {
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

	public void insertTracker(int courseId, String digest, String data, String type, boolean completed, String event, int points){
		long userId = this.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));
		
		ContentValues values = new ContentValues();
		values.put(TRACKER_LOG_C_COURSEID, courseId);
		values.put(TRACKER_LOG_C_ACTIVITYDIGEST, digest);
		values.put(TRACKER_LOG_C_DATA, data);
		values.put(TRACKER_LOG_C_COMPLETED, completed);
		values.put(TRACKER_LOG_C_USERID, userId);
		values.put(TRACKER_LOG_C_TYPE, type);
		values.put(TRACKER_LOG_C_EVENT,event);
		values.put(TRACKER_LOG_C_POINTS, points);
		long id = db.insertOrThrow(TRACKER_LOG_TABLE, null, values);

        this.incrementUserPoints(userId, points);

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
		boolean isInstalled = (c.getCount() > 0);
		c.close();
		return isInstalled;
	}
	
	public boolean toUpdate(String shortname, Double version){
		String s = COURSE_C_SHORTNAME + "=? AND "+ COURSE_C_VERSIONID + "< ?";
		String[] args = new String[] { shortname, String.format("%.0f", version) };
		Cursor c = db.query(COURSE_TABLE, null, s, args, null, null, null);
		boolean toUpdate = (c.getCount() > 0);
		c.close();
		return  toUpdate;
	}
	
	public boolean toUpdateSchedule(String shortname, Double scheduleVersion){
		String s = COURSE_C_SHORTNAME + "=? AND "+ COURSE_C_SCHEDULE + "< ?";
		String[] args = new String[] { shortname, String.format("%.0f", scheduleVersion) };
		Cursor c = db.query(COURSE_TABLE, null, s, args, null, null, null);
		boolean toUpdate = (c.getCount() > 0);
		c.close();
		return  toUpdate;
	}
	
	public long getUserId(String username){
		String s = USER_C_USERNAME + "=? ";
		String[] args = new String[] { username };
		Cursor c = db.query(USER_TABLE, null, s, args, null, null, null);
		c.moveToFirst();
		long userId = -1;
		while (!c.isAfterLast()) {
			userId = c.getLong(c.getColumnIndex(USER_C_ID));
			c.moveToNext();
		}
		c.close();
		return userId;
	}

	private User fetchUser(Cursor c){
		User u = new User();
		u.setUserId(c.getLong(c.getColumnIndex(USER_C_ID)));
		u.setApiKey(c.getString(c.getColumnIndex(USER_C_APIKEY)));
		u.setUsername(c.getString(c.getColumnIndex(USER_C_USERNAME)));
		u.setFirstname(c.getString(c.getColumnIndex(USER_C_FIRSTNAME)));
		u.setLastname(c.getString(c.getColumnIndex(USER_C_LASTNAME)));
		u.setPoints(c.getInt(c.getColumnIndex(USER_C_POINTS)));
		u.setBadges(c.getInt(c.getColumnIndex(USER_C_BADGES)));
		u.setPasswordEncrypted(c.getString(c.getColumnIndex(USER_C_PASSWORDENCRYPTED)));
		u.setOfflineRegister(c.getInt(c.getColumnIndex(USER_C_OFFLINE_REGISTER))>0);
		u.setPhoneNo(c.getString(c.getColumnIndex(USER_C_PHONE)));
		u.setEmail(c.getString(c.getColumnIndex(USER_C_EMAIL)));
		u.setJobTitle(c.getString(c.getColumnIndex(USER_C_JOBTITLE)));
		u.setOrganisation(c.getString(c.getColumnIndex(USER_C_ORGANIZATION)));
		if (u.isOfflineRegister()){
			u.setPassword(c.getString(c.getColumnIndex(USER_C_PASSWORDPLAIN)));
			u.setPasswordAgain(c.getString(c.getColumnIndex(USER_C_PASSWORDPLAIN)));
		}

		return u;
	}

	private User getUser(Cursor c) throws UserNotFoundException {
		User u = null;
		c.moveToFirst();
		while (!c.isAfterLast()) {
			u = fetchUser(c);
			c.moveToNext();
		}
		c.close();
		if (u == null){
			throw new UserNotFoundException();
		}

		return u;
	}

	public User getUser(long userId) throws UserNotFoundException {
		String s = USER_C_ID + "=? ";
		String[] args = new String[] { String.valueOf(userId) };
		Cursor c = db.query(USER_TABLE, null, s, args, null, null, null);
		return getUser(c);
	}
	
	public User getUser(String userName) throws UserNotFoundException {
	    Log.d(TAG,"getting username: " + userName);
		String s = USER_C_USERNAME + "=? ";
		String[] args = new String[] { userName };
		Cursor c = db.query(USER_TABLE, null, s, args, null, null, null);
		return getUser(c);
	}

	public User getOneRegisteredUser() throws UserNotFoundException {
		String s = USER_C_OFFLINE_REGISTER + "=? ";
		String[] args = new String[] { "0" };

		Cursor c = db.query(USER_TABLE, null, s, args, null, null, null);
		return getUser(c);
	}

	public List<User> getAllUsers(){
		Cursor c = db.query(USER_TABLE, null, null, null, null, null, null);
		c.moveToFirst();

		ArrayList<User> users = new ArrayList<>();
		while (!c.isAfterLast()) {
			User u = fetchUser(c);
			users.add(u);
			c.moveToNext();
		}
		c.close();
		return users;
	}

    public void incrementUserPoints(long userId, int pointsToAdd) {

        int currentPoints = 0;
        String s = USER_C_ID + "=? ";
        String[] args = new String[] { String.valueOf(userId) };
        Cursor c = db.query(USER_TABLE, null, s, args, null, null, null);
        c.moveToFirst();

        String username = "";
        String fullname = "";

        while (!c.isAfterLast()) {
            currentPoints = c.getInt(c.getColumnIndex(USER_C_POINTS));
            username = c.getString(c.getColumnIndex(USER_C_USERNAME));
            fullname = c.getString(c.getColumnIndex(USER_C_FIRSTNAME)) + " " + c.getString(c.getColumnIndex(USER_C_LASTNAME));
            c.moveToNext();
        }
        c.close();

		currentPoints += pointsToAdd;
        ContentValues values = new ContentValues();
        values.put(USER_C_POINTS, currentPoints);
        s = USER_C_ID + "=? ";
        args = new String[] { String.valueOf(userId) };
        db.update(USER_TABLE, values, s, args);

		DateTime lastUpdate = new DateTime();
        this.insertOrUpdateUserLeaderboard(username, fullname, currentPoints, lastUpdate);
    }

	public List<Points> getUserPoints(long userId, Course courseFilter, boolean onlyTrackerlogs) {
        ArrayList<Points> points = new ArrayList<>();

        // Points from Tracker
        String s = TRACKER_LOG_C_USERID + "=? AND " + TRACKER_LOG_C_POINTS + "!=0";
        String[] args = new String[] { String.valueOf(userId) };
        Cursor c = db.query(TRACKER_LOG_TABLE, null, s, args, null, null, null);
        c.moveToFirst();

        String prefLang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());

        while (!c.isAfterLast()) {

			Activity activity = this.getActivityByDigest(c.getString(c.getColumnIndex(TRACKER_LOG_C_ACTIVITYDIGEST)));
			Course course = this.getCourse(c.getInt(c.getColumnIndex(TRACKER_LOG_C_COURSEID)), userId);

			if(courseFilter != null && course != null){
				if (courseFilter.getCourseId() != course.getCourseId()) {
					c.moveToNext();
					continue;
				}
			}

            Points p = new Points();
            p.setDateTime(c.getString(c.getColumnIndex(TRACKER_LOG_C_DATETIME)));
            p.setPoints(c.getInt(c.getColumnIndex(TRACKER_LOG_C_POINTS)));
            p.setEvent(c.getString(c.getColumnIndex(TRACKER_LOG_C_EVENT)));

            // get course and activity title
			String event = c.getString(c.getColumnIndex(TRACKER_LOG_C_EVENT));
            String description = event;
            Log.d(TAG, event);


			switch (event) {
				case Gamification.EVENT_NAME_ACTIVITY_COMPLETED:
					if (activity != null) {
						Course courseActivity = this.getCourse(activity.getCourseId(), userId);
						if (courseActivity != null) {
							description = this.ctx.getString(R.string.points_event_activity_completed,
									activity.getTitle(prefLang),
									courseActivity.getTitle(prefLang));
						}
					}
					break;

				case Gamification.EVENT_NAME_MEDIA_PLAYED:
					String data = c.getString(c.getColumnIndex(TRACKER_LOG_C_DATA));
					try {
						JSONObject jsonObj = new JSONObject(data);
						String mediaFileName = jsonObj.getString("mediafile");
						if (course != null) {
							description = this.ctx.getString(R.string.points_event_media_played,
									mediaFileName);
						}
					} catch (JSONException jsone) {
						Log.d(TAG, jsone.getMessage(), jsone);
					}

					break;

				case Gamification.EVENT_NAME_QUIZ_ATTEMPT:
					Log.d(TAG, "quizid " + c.getString(c.getColumnIndex(TRACKER_LOG_C_ACTIVITYDIGEST)));
					if ((course != null) && (activity != null)) {
						description = this.ctx.getString(R.string.points_event_quiz_attempt,
								activity.getTitle(prefLang),
								course.getTitle(prefLang));
					}

					break;

				case Gamification.EVENT_NAME_COURSE_DOWNLOADED:
					Log.d(TAG, "id: " + c.getInt(c.getColumnIndex(TRACKER_LOG_C_COURSEID)));
					description = this.ctx.getString(R.string.points_event_course_downloaded,
							course == null ? "" : course.getTitle(prefLang));
					break;
			}

            p.setDescription(description);

            points.add(p);
            c.moveToNext();
        }
        c.close();

		if (onlyTrackerlogs) {
			// Re-order by date time with latest date first
			Collections.sort(points, new PointsComparator());
			return points;
		}

        // Points from QuizAttempts
        String qa = QUIZATTEMPTS_C_USERID + "=? AND " + QUIZATTEMPTS_C_POINTS + "!=0";
        String[] qaargs = new String[] { String.valueOf(userId) };
        Cursor qac = db.query(QUIZATTEMPTS_TABLE, null, qa, qaargs, null, null, null);
        qac.moveToFirst();
        while (!qac.isAfterLast()) {
            Points p = new Points();
            p.setDateTime(qac.getString(qac.getColumnIndex(QUIZATTEMPTS_C_DATETIME)));
            p.setPoints(qac.getInt(qac.getColumnIndex(QUIZATTEMPTS_C_POINTS)));
            p.setEvent(qac.getString(qac.getColumnIndex(QUIZATTEMPTS_C_EVENT)));

            // get course and activity title
            String description = qac.getString(qac.getColumnIndex(QUIZATTEMPTS_C_EVENT));
            Activity activity = this.getActivityByDigest(qac.getString(qac.getColumnIndex(QUIZATTEMPTS_C_ACTIVITY_DIGEST)));

            if (activity != null) {

            	if(courseFilter != null){
					if (courseFilter.getCourseId() != activity.getCourseId()) {
						qac.moveToNext();
						continue;
					}
				}

                Course course = this.getCourse(activity.getCourseId(), userId);
                if (course != null) {
                    description = this.ctx.getString(R.string.points_event_quiz_attempt,
                            activity.getTitle(prefs),
                            course.getTitle(prefs));
                }
            }
            p.setDescription(description);

            points.add(p);
            qac.moveToNext();
        }
        qac.close();

        // Re-order by date time with latest date first
        Collections.sort(points, new PointsComparator());

        return points;
	}

	public void updateUserBadges(String userName, int badges){
		ContentValues values = new ContentValues();
		values.put(USER_C_BADGES, badges);
		String s = USER_C_USERNAME + "=? ";
		String[] args = new String[] { userName };
		db.update(USER_TABLE, values, s ,args);
	}

	public void updateUserBadges(long userId, int badges){
		ContentValues values = new ContentValues();
		values.put(USER_C_BADGES, badges);
		String s = USER_C_ID + "=? ";
		String[] args = new String[] { String.valueOf(userId) };
		db.update(USER_TABLE, values, s ,args);
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

	public int getUnexportedTrackersCount(){
		String s = TRACKER_LOG_C_SUBMITTED + "=? AND " + TRACKER_LOG_C_EXPORTED + "=? ";
		String[] args = new String[] { "0",  "0" };
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

		ArrayList<Object> sl = new ArrayList<>();
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
                json.put("event", c.getString(c.getColumnIndex(TRACKER_LOG_C_EVENT)));
                json.put("points", c.getInt(c.getColumnIndex(TRACKER_LOG_C_POINTS)));
				Course m = this.getCourse(c.getLong(c.getColumnIndex(TRACKER_LOG_C_COURSEID)), userId);
				if (m != null){
					json.put("course", m.getShortname());
				}
				String trackerType = c.getString(c.getColumnIndex(TRACKER_LOG_C_TYPE));
				if (trackerType != null){
					json.put("type",trackerType);
				}
				content = json.toString();
			} catch (JSONException jsone) {
				Mint.logException(jsone);
				Log.d(TAG,"error creating unsent trackers", jsone);
			}
			
			so.setContent(content);
			sl.add(so);
			c.moveToNext();
		}
		Payload p = new Payload(sl);
		c.close();
		
		return p;
	}

	public List<TrackerLog> getUnexportedTrackers(long userId){
		String s = TRACKER_LOG_C_SUBMITTED + "=? AND " + TRACKER_LOG_C_USERID + "=? AND " + TRACKER_LOG_C_EXPORTED + "=? ";
		String[] args = new String[] { "0", String.valueOf(userId), "0" };
		Cursor c = db.query(TRACKER_LOG_TABLE, null, s, args, null, null, null);
		c.moveToFirst();

		ArrayList<TrackerLog> trackers = new ArrayList<>();
		while (!c.isAfterLast()) {
			TrackerLog tracker = new TrackerLog();
			String digest = c.getString(c.getColumnIndex(TRACKER_LOG_C_ACTIVITYDIGEST));
			tracker.setId(c.getLong(c.getColumnIndex(TRACKER_LOG_C_ID)));
			tracker.setDigest(digest);
			String content = "";
			try {
				JSONObject json = new JSONObject();
				json.put("data", c.getString(c.getColumnIndex(TRACKER_LOG_C_DATA)));
				json.put("tracker_date", c.getString(c.getColumnIndex(TRACKER_LOG_C_DATETIME)));
				json.put("completed", c.getInt(c.getColumnIndex(TRACKER_LOG_C_COMPLETED)));
				json.put("digest", (digest!=null) ? digest : "");
                json.put("event", c.getString(c.getColumnIndex(TRACKER_LOG_C_EVENT)));
                json.put("points", c.getInt(c.getColumnIndex(TRACKER_LOG_C_POINTS)));
				Course m = this.getCourse(c.getLong(c.getColumnIndex(TRACKER_LOG_C_COURSEID)), userId);
				if (m != null){
					json.put("course", m.getShortname());
				}
				String trackerType = c.getString(c.getColumnIndex(TRACKER_LOG_C_TYPE));
				if (trackerType != null){
					json.put("type",trackerType);
				}
				content = json.toString();
			} catch (JSONException jsone) {
				Mint.logException(jsone);
                Log.d(TAG,"error creating unexported trackers", jsone);
			}

			tracker.setContent(content);
			trackers.add(tracker);
			c.moveToNext();
		}
		c.close();
		return trackers;
	}

	public void markLogsAndQuizzesExported(){
		ContentValues trackerValues = new ContentValues();
		trackerValues.put(TRACKER_LOG_C_EXPORTED, 1);
		db.update(TRACKER_LOG_TABLE, trackerValues, null, null);

		ContentValues quizValues = new ContentValues();
		quizValues.put(QUIZATTEMPTS_C_EXPORTED, 1);
		db.update(QUIZATTEMPTS_TABLE, quizValues, null, null);
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
        values.put(QUIZATTEMPTS_C_EVENT, qa.getEvent());
        values.put(QUIZATTEMPTS_C_POINTS, qa.getPoints());
		long result = db.insertOrThrow(QUIZATTEMPTS_TABLE, null, values);

        // increment the users points
        this.incrementUserPoints(qa.getUserId(), qa.getPoints());
        return result;
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
        values.put(QUIZATTEMPTS_C_EVENT, qa.getEvent());
        values.put(QUIZATTEMPTS_C_POINTS, qa.getPoints());
		db.update(QUIZATTEMPTS_TABLE, values, QUIZATTEMPTS_C_ID + "=" + qa.getId(), null);
	}

	public void insertQuizAttempts(List<QuizAttempt> quizAttempts){
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
            values.put(QUIZATTEMPTS_C_EVENT, qa.getEvent());
            values.put(QUIZATTEMPTS_C_POINTS, qa.getPoints());
            db.insertOrThrow(QUIZATTEMPTS_TABLE, null, values);
        }
        endTransaction(true);
	}

	public List<QuizAttempt>  getUnsentQuizAttempts(){
		String s = QUIZATTEMPTS_C_SENT + "=? ";
		String[] args = new String[] { "0" };
		Cursor c = db.query(QUIZATTEMPTS_TABLE, null, s, args, null, null, null);
		c.moveToFirst();
		ArrayList<QuizAttempt> quizAttempts = new ArrayList<>();
		while (!c.isAfterLast()) {
			try {
				QuizAttempt qa = new QuizAttempt();
				qa.setId(c.getLong(c.getColumnIndex(QUIZATTEMPTS_C_ID)));
				qa.setData(c.getString(c.getColumnIndex(QUIZATTEMPTS_C_DATA)));
				qa.setUserId(c.getLong(c.getColumnIndex(QUIZATTEMPTS_C_USERID)));
                qa.setEvent(c.getString(c.getColumnIndex(QUIZATTEMPTS_C_EVENT)));
                qa.setPoints(c.getInt(c.getColumnIndex(QUIZATTEMPTS_C_POINTS)));
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

	public List<QuizAttempt> getUnexportedQuizAttempts(long userId) {
		String s = QUIZATTEMPTS_C_SENT + "=? AND " + QUIZATTEMPTS_C_EXPORTED + "=? AND " + QUIZATTEMPTS_C_USERID + "=? ";
		String[] args = new String[] { "0", "0", String.valueOf(userId) };
		Cursor c = db.query(QUIZATTEMPTS_TABLE, null, s, args, null, null, null);
		c.moveToFirst();
		ArrayList<QuizAttempt> quizAttempts = new ArrayList<>();
		while (!c.isAfterLast()) {
			try {
				QuizAttempt qa = new QuizAttempt();
				qa.setId(c.getLong(c.getColumnIndex(QUIZATTEMPTS_C_ID)));
				qa.setData(c.getString(c.getColumnIndex(QUIZATTEMPTS_C_DATA)));
				qa.setUserId(c.getLong(c.getColumnIndex(QUIZATTEMPTS_C_USERID)));
                qa.setEvent(c.getString(c.getColumnIndex(QUIZATTEMPTS_C_EVENT)));
                qa.setPoints(c.getInt(c.getColumnIndex(QUIZATTEMPTS_C_POINTS)));
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

	public boolean isQuizFirstAttempt(String digest){
        //get current user id
        long userId = this.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));

        String s = QUIZATTEMPTS_C_ACTIVITY_DIGEST + "=? AND " + QUIZATTEMPTS_C_USERID + "=? ";
        String[] args = new String[] { digest, String.valueOf(userId) };
        Cursor c = db.query(QUIZATTEMPTS_TABLE, null, s, args, null, null, null);
        int count = c.getCount();
        Log.d(this.TAG, "isQuizFirstAttempt returned " + count + " rows");
        c.close();
		return (count == 0);

    }

    public boolean isQuizFirstAttemptToday(String digest){
        //get current user id
        long userId = this.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);

        String todayString = ( new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ) ).format( today.getTime() );

        String s = QUIZATTEMPTS_C_ACTIVITY_DIGEST + "=? AND " + QUIZATTEMPTS_C_USERID + "=? AND " + QUIZATTEMPTS_C_DATETIME + ">=?";
        String[] args = new String[] { digest, String.valueOf(userId), todayString };
        Cursor c = db.query(QUIZATTEMPTS_TABLE, null, s, args, null, null, null);
        int count = c.getCount();
		Log.d(this.TAG, "isQuizFirstAttemptToday returned " + count + " rows");
        c.close();
        return (count == 0);
    }

	public boolean isActivityFirstAttemptToday(String digest){
		//get current user id
		long userId = this.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));

		Calendar today = Calendar.getInstance();
		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);

		String todayString = ( new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ) ).format( today.getTime() );

		String s = TRACKER_LOG_C_ACTIVITYDIGEST + "=? AND " + TRACKER_LOG_C_USERID + "=? AND " + TRACKER_LOG_C_DATETIME + ">=?";
		String[] args = new String[] { digest, String.valueOf(userId), todayString };
		Cursor c = db.query(TRACKER_LOG_TABLE, null, s, args, null, null, null);
		int count = c.getCount();
		c.close();
		return (count == 0);
	}

	public boolean isMediaPlayed(String digest){
		//get current user id
		long userId = this.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));

		String s = TRACKER_LOG_C_ACTIVITYDIGEST + "=? AND " + TRACKER_LOG_C_USERID + "=? AND " + TRACKER_LOG_C_COMPLETED + "=1";
		String[] args = new String[] { digest, String.valueOf(userId) };
		Cursor c = db.query(TRACKER_LOG_TABLE, null, s, args, null, null, null);
		int count = c.getCount();
		c.close();
		return (count > 0);
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
		boolean isAttempted = (c.getCount() > 0);
		c.close();
		return isAttempted;
	}
	
	public boolean activityCompleted(int courseId, String digest, long userId){
		String s = TRACKER_LOG_C_ACTIVITYDIGEST + "=? AND " + 
					TRACKER_LOG_C_COURSEID + "=? AND " + 
					TRACKER_LOG_C_USERID + "=? AND " +
					TRACKER_LOG_C_COMPLETED + "=1";
		String[] args = new String[] { digest, String.valueOf(courseId), String.valueOf(userId) };
		Cursor c = db.query(TRACKER_LOG_TABLE, null, s, args, null, null, null);
		boolean isCompleted = (c.getCount() > 0);
		c.close();
		return isCompleted;
	}

    public void getCourseQuizResults(List<QuizStats> stats, int courseId, long userId){

        String quizResultsWhereClause = QUIZATTEMPTS_C_COURSEID+" =? AND " + QUIZATTEMPTS_C_USERID + "=?";
        String[] quizResultsArgs = new String[] { String.valueOf(courseId), String.valueOf(userId) };
        String[] quizResultsColumns = new String[]{ QUIZATTEMPTS_C_ACTIVITY_DIGEST, QUIZATTEMPTS_C_PASSED, QUIZATTEMPTS_C_MAXSCORE, QUIZATTEMPTS_C_SCORE};

        //We get the attempts made by the user for this course's quizzes
        Cursor c = db.query(QUIZATTEMPTS_TABLE, quizResultsColumns, quizResultsWhereClause, quizResultsArgs, null, null, null);
        if (c.getCount() <= 0) return; //we return the empty array

        if (stats == null) stats = new ArrayList<>();

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
				a.setTitlesFromJSONString(c.getString(c.getColumnIndex(ACTIVITY_C_TITLE)));
				a.setSectionId(c.getInt(c.getColumnIndex(ACTIVITY_C_SECTIONID)));
			}
			c.moveToNext();
		}
		c.close();
		return a;
	}
	
	public List<Activity> getActivitiesDue(int max, long userId){
		
		ArrayList<Activity> activities = new ArrayList<>();
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
		while (!c.isAfterLast()) {
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
			while (!c2.isAfterLast()) {
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
		List<Activity> activities = this.getCourseActivities(courseId);
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
	public List<SearchResult> search(String searchText, int limit, long userId, Context ctx, DBListener listener){
		ArrayList<SearchResult> results = new ArrayList<>();
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
                        result.setActivity(parsed.getSection(sectionOrderId).getActivity(activity.getDigest()));
                        results.add(result);
                    } catch (InvalidXMLException ixmle) {
                        Log.d(TAG,"Invalid course xml file", ixmle);
                        Mint.logException(ixmle);
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
	public boolean isPreviousSectionActivitiesCompleted(Activity activity, long userId){
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
        boolean completed = true;
	    if(c.getCount()>0){
	    	c.moveToFirst();
	    	// check if each activity has been completed or not
	    	while (!c.isAfterLast()) {
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
	    }
        c.close();
        return completed;
	}
	
	/*
	 * 
	 */
	public boolean isPreviousCourseActivitiesCompleted(Activity activity, long userId){
		
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
        boolean completed = true;
	    if(c.getCount()>0){
	    	c.moveToFirst();
	    	// check if each activity has been completed or not
	    	while (!c.isAfterLast()) {
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
	    }
	    c.close();
	    return completed;
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
        ArrayList<Pair<String, String>> localPrefs = new ArrayList<>();
        String whereClause = USER_PREFS_C_USERNAME + "=? ";
        String[] args = new String[] { username };

        Cursor c = db.query(USER_PREFS_TABLE, null, whereClause, args, null, null, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {

            String prefKey = c.getString(c.getColumnIndex(USER_PREFS_C_PREFKEY));
            String prefValue = c.getString(c.getColumnIndex(USER_PREFS_C_PREFVALUE));
            Pair<String, String> pref = new Pair<>(prefKey, prefValue);
            localPrefs.add(pref);

            c.moveToNext();
        }
        c.close();

        return localPrefs;
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

    public boolean insertOrUpdateUserLeaderboard(String username, String fullname, int points, DateTime lastUpdate){

		if ((username == null) || ("".equals(username)))
			return false;

		String whereClause = LEADERBOARD_C_USERNAME + "=? ";
		String[] args = new String[] { username };
		boolean updated;
		String lastUpdateStr = MobileLearning.DATETIME_FORMAT.print(lastUpdate);

		ContentValues values = new ContentValues();
		values.put(LEADERBOARD_C_FULLNAME, fullname);
		values.put(LEADERBOARD_C_USERNAME, username);
		values.put(LEADERBOARD_C_POINTS, points);
		values.put(LEADERBOARD_C_LASTUPDATE, lastUpdateStr);

		Cursor c = db.query(LEADERBOARD_TABLE, null, whereClause, args, null, null, null);
		if (c.getCount() > 0){
			c.close();
			//Update table only if the last update value is smaller than the current one
			String s = LEADERBOARD_C_USERNAME + "=? AND " + LEADERBOARD_C_LASTUPDATE + "<=? ";
			args = new String[] { username, lastUpdateStr };
			int affectedRows = db.update(LEADERBOARD_TABLE, values, s, args);

			updated = (affectedRows > 0);
		}
		else{
			db.insertOrThrow(LEADERBOARD_TABLE, null, values);
			updated = true;
			c.close();
		}

		return updated;
	}

	public List<LeaderboardPosition> getLeaderboard(){
        ArrayList<LeaderboardPosition> leaderboard = new ArrayList<>();
        String order = LEADERBOARD_C_POINTS + " DESC ";
        Cursor c = db.query(LEADERBOARD_TABLE, null, null, null, null, null, order);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            LeaderboardPosition pos = new LeaderboardPosition();
            pos.setUsername(c.getString(c.getColumnIndex(LEADERBOARD_C_USERNAME)));
            pos.setFullname(c.getString(c.getColumnIndex(LEADERBOARD_C_FULLNAME)));
            pos.setPoints(c.getInt(c.getColumnIndex(LEADERBOARD_C_POINTS)));
            leaderboard.add(pos);
            c.moveToNext();
        }

        c.close();
        return leaderboard;
    }


}
