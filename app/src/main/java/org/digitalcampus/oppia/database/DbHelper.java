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

package org.digitalcampus.oppia.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.gamification.Gamification;
import org.digitalcampus.oppia.gamification.PointsComparator;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.CompleteCourse;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CustomField;
import org.digitalcampus.oppia.model.CustomValue;
import org.digitalcampus.oppia.model.GamificationEvent;
import org.digitalcampus.oppia.model.Points;
import org.digitalcampus.oppia.model.QuizAttempt;
import org.digitalcampus.oppia.model.QuizStats;
import org.digitalcampus.oppia.model.SearchResult;
import org.digitalcampus.oppia.model.TrackerLog;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.model.db_model.Leaderboard;
import org.digitalcampus.oppia.model.db_model.UserCustomField;
import org.digitalcampus.oppia.model.db_model.UserPreference;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.xmlreaders.CourseXMLReader;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DbHelper extends SQLiteOpenHelper {

    private static final String TAG = DbHelper.class.getSimpleName();
    public static final String DB_NAME = "mobilelearning.db";
    public static final int DB_VERSION = 46;

    private static DbHelper instance;
    private SQLiteDatabase db;
    private SharedPreferences prefs;
    private Context ctx;

    private static final String STR_CREATE_TABLE = "create table ";
    private static final String STR_ALTER_TABLE = "ALTER TABLE ";
    private static final String STR_INT_PRIMARY_KEY_AUTO = " integer primary key autoincrement, ";
    private static final String STR_INT_COMMA = " int, ";
    private static final String STR_TEXT_COMMA = " text, ";
    private static final String STR_INT_DEFAULT_O = " integer default 0";
    private static final String STR_DROP_IF_EXISTS = "drop table if exists ";
    private static final String STR_ADD_COLUMN = " ADD COLUMN ";
    private static final String STR_TEXT_NULL = " text null";
    private static final String STR_DATETIME_NULL = " datetime null";
    private static final String STR_WHERE = " WHERE ";
    private static final String STR_EQUALS_NUMBER = " = %d ";
    private static final String STR_TEXT_DEFAULT = " text default '";
    private static final String STR_EQUALS_AND = "=? AND ";
    private static final String STR_INNERJOIN_FULLTEXT = " INNER JOIN %s a ON a.%s = ft.docid";
    private static final String STR_INNERJOIN_COURSE = " INNER JOIN %s c ON a.%s = c.%s ";
    private static final String STR_WHERE_MATCH = " WHERE %s MATCH '%s' ";

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
    private static final String QUIZATTEMPTS_C_TIMETAKEN = "timetaken";
    private static final String QUIZATTEMPTS_C_TYPE = "quiztype";

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

    private static final String CUSTOM_FIELD_TABLE = "customfield";
    private static final String CUSTOM_FIELD_C_ID = BaseColumns._ID;
    private static final String CUSTOM_FIELD_C_KEY = "field_key";
    private static final String CUSTOM_FIELD_C_REQUIRED = "required";
    private static final String CUSTOM_FIELD_C_TYPE = "fieldtype";
    private static final String CUSTOM_FIELD_C_LABEL = "label";
    private static final String CUSTOM_FIELD_C_HELPTEXT = "helptext";
    private static final String CUSTOM_FIELD_C_ORDER = "field_order";
    private static final String CUSTOM_FIELD_C_COLLECTION = "collection";
    private static final String CUSTOM_FIELD_C_VISIBLE_BY = "visible_by";
    private static final String CUSTOM_FIELD_C_VISIBLE_VALUE = "visible_value";
    private static final String CUSTOM_FIELD_C_COLLECTION_BY = "collection_by";

    private static final String CUSTOM_FIELDS_COLLECTION_TABLE = "customfield_collection";
    private static final String CUSTOM_FIELDS_COLLECTION_C_ID = BaseColumns._ID;
    private static final String CUSTOM_FIELDS_COLLECTION_C_COLLECTION_ID = "collection_id";
    private static final String CUSTOM_FIELDS_COLLECTION_C_ITEM_KEY = "item_key";
    private static final String CUSTOM_FIELDS_COLLECTION_C_ITEM_VALUE = "item_value";

    // User Custom Fields
    static final String USER_CF_TABLE = "user_cf";
    private static final String USER_CF_ID = BaseColumns._ID;
    private static final String USER_CF_USERNAME = "username";
    private static final String CF_FIELD_KEY = "field_key";
    private static final String CF_VALUE_STR = "value_str";
    private static final String CF_VALUE_INT = "value_int";
    private static final String CF_VALUE_BOOL = "value_bool";
    private static final String CF_VALUE_FLOAT = "value_float";


    // Constructor
    private DbHelper(Context ctx, String dbName) { //
        super(ctx, dbName, null, DB_VERSION);
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        db = this.getWritableDatabase();
        this.ctx = ctx;
    }

    public static synchronized DbHelper getInstance(Context ctx) {
        if (instance == null) {
            instance = new DbHelper(ctx, DB_NAME);
        }
        return instance;
    }

    public static void clearInstance() {
        instance = null;
    }

    public static synchronized DbHelper getInMemoryInstance(Context ctx) {
        if (instance == null) {
            instance = new DbHelper(ctx, null); // null dbName creates a "in memory" database
        }
        return instance;
    }

    public synchronized void resetDatabase() {
        //Remove the data from all the tables

        List<String> tables = Arrays.asList(COURSE_TABLE, COURSE_GAME_TABLE, ACTIVITY_TABLE,
                ACTIVITY_GAME_TABLE, TRACKER_LOG_TABLE, QUIZATTEMPTS_TABLE, SEARCH_TABLE,
                USER_TABLE, CUSTOM_FIELD_TABLE, CUSTOM_FIELDS_COLLECTION_TABLE, USER_CF_TABLE);

        for (String tablename : tables) {
            db.delete(tablename, null, null);
        }
    }

    public SQLiteDatabase getDB(){
        return db;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        createCourseTable(db);
        createActivityTable(db);
        createLogTable(db);
        createQuizAttemptsTable(db);
        createSearchTable(db);
        createUserTable(db);
        createCourseGamificationTable(db);
        createActivityGamificationTable(db);
        createUserCustomFieldsTable(db);
        createCustomFieldTable(db);
        createCustomFieldsCollectionTable(db);
    }

    public void beginTransaction() {
        db.beginTransaction();
    }

    public void endTransaction(boolean success) {
        if (success) {
            db.setTransactionSuccessful();
        }
        db.endTransaction();
    }

    private void createCourseTable(SQLiteDatabase db) {
        String mSql = STR_CREATE_TABLE + COURSE_TABLE + " (" + COURSE_C_ID + STR_INT_PRIMARY_KEY_AUTO
                + COURSE_C_VERSIONID + STR_INT_COMMA + COURSE_C_TITLE + STR_TEXT_COMMA + COURSE_C_LOCATION + STR_TEXT_COMMA
                + COURSE_C_SHORTNAME + STR_TEXT_COMMA + COURSE_C_SCHEDULE + STR_INT_COMMA
                + COURSE_C_IMAGE + STR_TEXT_COMMA
                + COURSE_C_DESC + STR_TEXT_COMMA
                + COURSE_C_ORDER_PRIORITY + STR_INT_DEFAULT_O + ", "
                + COURSE_C_LANGS + STR_TEXT_COMMA
                + COURSE_C_SEQUENCING + STR_TEXT_DEFAULT + Course.SEQUENCING_MODE_NONE + "' )";
        db.execSQL(mSql);
    }

    private void createActivityTable(SQLiteDatabase db) {
        String aSql = STR_CREATE_TABLE + ACTIVITY_TABLE + " (" +
                ACTIVITY_C_ID + STR_INT_PRIMARY_KEY_AUTO +
                ACTIVITY_C_COURSEID + STR_INT_COMMA +
                ACTIVITY_C_SECTIONID + STR_INT_COMMA +
                ACTIVITY_C_ACTID + STR_INT_COMMA +
                ACTIVITY_C_ACTTYPE + STR_TEXT_COMMA +
                ACTIVITY_C_STARTDATE + " datetime null, " +
                ACTIVITY_C_ENDDATE + " datetime null, " +
                ACTIVITY_C_ACTIVITYDIGEST + STR_TEXT_COMMA +
                ACTIVITY_C_TITLE + " text)";
        db.execSQL(aSql);
    }

    private void createLogTable(SQLiteDatabase db) {
        String lSql = STR_CREATE_TABLE + TRACKER_LOG_TABLE + " (" +
                TRACKER_LOG_C_ID + STR_INT_PRIMARY_KEY_AUTO +
                TRACKER_LOG_C_COURSEID + " integer, " +
                TRACKER_LOG_C_DATETIME + " datetime default current_timestamp, " +
                TRACKER_LOG_C_ACTIVITYDIGEST + STR_TEXT_COMMA +
                TRACKER_LOG_C_DATA + STR_TEXT_COMMA +
                TRACKER_LOG_C_SUBMITTED + STR_INT_DEFAULT_O + ", " +
                TRACKER_LOG_C_INPROGRESS + STR_INT_DEFAULT_O + ", " +
                TRACKER_LOG_C_COMPLETED + STR_INT_DEFAULT_O + ", " +
                TRACKER_LOG_C_USERID + STR_INT_DEFAULT_O + ", " +
                TRACKER_LOG_C_TYPE + STR_TEXT_COMMA +
                TRACKER_LOG_C_EXPORTED + STR_INT_DEFAULT_O + ", " +
                TRACKER_LOG_C_EVENT + STR_TEXT_COMMA +
                TRACKER_LOG_C_POINTS + STR_INT_DEFAULT_O +
                ")";
        db.execSQL(lSql);
    }

    private void createQuizAttemptsTable(SQLiteDatabase db) {
        String sql = STR_CREATE_TABLE + QUIZATTEMPTS_TABLE + " (" +
                QUIZATTEMPTS_C_ID + STR_INT_PRIMARY_KEY_AUTO +
                QUIZATTEMPTS_C_DATETIME + " datetime default current_timestamp, " +
                QUIZATTEMPTS_C_DATA + STR_TEXT_COMMA +
                QUIZATTEMPTS_C_ACTIVITY_DIGEST + STR_TEXT_COMMA +
                QUIZATTEMPTS_C_SENT + STR_INT_DEFAULT_O + ", " +
                QUIZATTEMPTS_C_COURSEID + " integer, " +
                QUIZATTEMPTS_C_USERID + STR_INT_DEFAULT_O + ", " +
                QUIZATTEMPTS_C_SCORE + " real default 0, " +
                QUIZATTEMPTS_C_MAXSCORE + " real default 0, " +
                QUIZATTEMPTS_C_PASSED + STR_INT_DEFAULT_O + ", " +
                QUIZATTEMPTS_C_EXPORTED + STR_INT_DEFAULT_O + ", " +
                QUIZATTEMPTS_C_EVENT + STR_TEXT_COMMA +
                QUIZATTEMPTS_C_TIMETAKEN + STR_INT_DEFAULT_O + ", " +
                QUIZATTEMPTS_C_TYPE + STR_TEXT_DEFAULT + QuizAttempt.TYPE_QUIZ + "', " +
                QUIZATTEMPTS_C_POINTS + STR_INT_DEFAULT_O + ")";
        db.execSQL(sql);
    }

    private void createSearchTable(SQLiteDatabase db) {
        String sql = "CREATE VIRTUAL TABLE " + SEARCH_TABLE + " USING FTS3 (" +
                SEARCH_C_TEXT + STR_TEXT_COMMA +
                SEARCH_C_COURSETITLE + STR_TEXT_COMMA +
                SEARCH_C_SECTIONTITLE + STR_TEXT_COMMA +
                SEARCH_C_ACTIVITYTITLE + " text " +
                ")";
        db.execSQL(sql);
    }

    private void createUserTable(SQLiteDatabase db) {

        String text = "] TEXT, ";
        String sql = "CREATE TABLE [" + USER_TABLE + "] (" +
                "[" + USER_C_ID + "]" + STR_INT_PRIMARY_KEY_AUTO +
                "[" + USER_C_USERNAME + "]" + STR_TEXT_COMMA +
                "[" + USER_C_FIRSTNAME + text +
                "[" + USER_C_LASTNAME + text +
                "[" + USER_C_PASSWORDENCRYPTED + text +
                "[" + USER_C_PASSWORDPLAIN + text +
                "[" + USER_C_APIKEY + text +
                "[" + USER_C_EMAIL + text +
                "[" + USER_C_PHONE + text +
                "[" + USER_C_JOBTITLE + text +
                "[" + USER_C_ORGANIZATION + text +
                "[" + USER_C_LAST_LOGIN_DATE + "] datetime null, " +
                "[" + USER_C_NO_LOGINS + "] integer default 0,  " +
                "[" + USER_C_POINTS + "] integer default 0,  " +
                "[" + USER_C_BADGES + "] integer default 0, " +
                "[" + USER_C_OFFLINE_REGISTER + "] integer default 0 " +
                ");";
        db.execSQL(sql);
    }


    public void createCourseGamificationTable(SQLiteDatabase db) {
        String mSql = STR_CREATE_TABLE + COURSE_GAME_TABLE + " ("
                + COURSE_GAME_C_ID + STR_INT_PRIMARY_KEY_AUTO
                + COURSE_GAME_C_COURSEID + " integer,"
                + COURSE_GAME_C_EVENT + STR_TEXT_COMMA
                + COURSE_GAME_C_POINTS + " integer default 0 )";
        db.execSQL(mSql);
    }

    private void createActivityGamificationTable(SQLiteDatabase db) {
        String mSql = STR_CREATE_TABLE + ACTIVITY_GAME_TABLE + " ("
                + ACTIVITY_GAME_C_ID + STR_INT_PRIMARY_KEY_AUTO
                + ACTIVITY_GAME_C_ACTIVITYID + " integer,"
                + ACTIVITY_GAME_C_EVENT + STR_TEXT_COMMA
                + ACTIVITY_GAME_C_POINTS + " integer default 0 )";
        db.execSQL(mSql);
    }

    private void createUserCustomFieldsTable(SQLiteDatabase db) {

        String sql = "CREATE TABLE IF NOT EXISTS ["+USER_CF_TABLE+"] (" +
                "["+USER_CF_ID+"]" + STR_INT_PRIMARY_KEY_AUTO +
                "["+ USER_CF_USERNAME +"]" + STR_TEXT_COMMA+
                "["+ CF_FIELD_KEY +"]" + STR_TEXT_COMMA +
                "["+ CF_VALUE_STR +"]" + STR_TEXT_COMMA +
                "["+ CF_VALUE_INT +"]" + STR_INT_COMMA +
                "["+ CF_VALUE_BOOL +"] BOOLEAN, " +
                "["+ CF_VALUE_FLOAT +"] FLOAT, " +
                "CONSTRAINT unq UNIQUE (" + USER_CF_USERNAME + ", "+ CF_FIELD_KEY +")" +
                ");";
        db.execSQL(sql);
    }

    private void createCustomFieldTable(SQLiteDatabase db) {
        String mSql = STR_CREATE_TABLE + CUSTOM_FIELD_TABLE + " ("
                + CUSTOM_FIELD_C_ID + STR_INT_PRIMARY_KEY_AUTO
                + CUSTOM_FIELD_C_KEY + STR_TEXT_COMMA
                + CUSTOM_FIELD_C_LABEL + STR_TEXT_COMMA
                + CUSTOM_FIELD_C_HELPTEXT + STR_TEXT_COMMA
                + CUSTOM_FIELD_C_TYPE + STR_TEXT_COMMA
                + CUSTOM_FIELD_C_COLLECTION + STR_TEXT_COMMA
                + CUSTOM_FIELD_C_VISIBLE_BY + STR_TEXT_COMMA
                + CUSTOM_FIELD_C_VISIBLE_VALUE + STR_TEXT_COMMA
                + CUSTOM_FIELD_C_ORDER + STR_INT_COMMA
                + CUSTOM_FIELD_C_REQUIRED + STR_INT_DEFAULT_O + ", "
                + CUSTOM_FIELD_C_COLLECTION_BY + " text)";
        db.execSQL(mSql);
    }

    private void createCustomFieldsCollectionTable(SQLiteDatabase db) {
        String mSql = STR_CREATE_TABLE + CUSTOM_FIELDS_COLLECTION_TABLE + " ("
                + CUSTOM_FIELDS_COLLECTION_C_ID + STR_INT_PRIMARY_KEY_AUTO
                + CUSTOM_FIELDS_COLLECTION_C_COLLECTION_ID + STR_TEXT_COMMA
                + CUSTOM_FIELDS_COLLECTION_C_ITEM_KEY + STR_TEXT_COMMA
                + CUSTOM_FIELDS_COLLECTION_C_ITEM_VALUE + " text)";
        db.execSQL(mSql);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < 7) {
            db.execSQL(STR_DROP_IF_EXISTS + COURSE_TABLE);
            db.execSQL(STR_DROP_IF_EXISTS + ACTIVITY_TABLE);
            db.execSQL(STR_DROP_IF_EXISTS + TRACKER_LOG_TABLE);
            db.execSQL(STR_DROP_IF_EXISTS + QUIZATTEMPTS_TABLE);
            createCourseTable(db);
            createActivityTable(db);
            createLogTable(db);
            createQuizAttemptsTable(db);
            return;
        }

        if (oldVersion <= 7 && newVersion >= 8) {
            String sql = STR_ALTER_TABLE + ACTIVITY_TABLE + STR_ADD_COLUMN + ACTIVITY_C_STARTDATE + STR_DATETIME_NULL +";";
            db.execSQL(sql);
            sql = STR_ALTER_TABLE + ACTIVITY_TABLE + STR_ADD_COLUMN + ACTIVITY_C_ENDDATE + STR_DATETIME_NULL +";";
            db.execSQL(sql);
        }

        if (oldVersion <= 8 && newVersion >= 9) {
            String sql = STR_ALTER_TABLE + COURSE_TABLE + STR_ADD_COLUMN + COURSE_C_SCHEDULE + " int null;";
            db.execSQL(sql);
        }

        if (oldVersion <= 9 && newVersion >= 10) {
            String sql = STR_ALTER_TABLE + ACTIVITY_TABLE + STR_ADD_COLUMN + ACTIVITY_C_TITLE + STR_TEXT_NULL + ";";
            db.execSQL(sql);
        }

        // This is a fix as previous versions may not have upgraded db tables correctly
        if (oldVersion <= 10 && newVersion >= 11) {
            String sql1 = STR_ALTER_TABLE + ACTIVITY_TABLE + STR_ADD_COLUMN + ACTIVITY_C_STARTDATE + STR_DATETIME_NULL +";";
            String sql2 = STR_ALTER_TABLE + ACTIVITY_TABLE + STR_ADD_COLUMN + ACTIVITY_C_ENDDATE + STR_DATETIME_NULL +";";
            String sql3 = STR_ALTER_TABLE + COURSE_TABLE + STR_ADD_COLUMN + COURSE_C_SCHEDULE + " int null;";
            String sql4 = STR_ALTER_TABLE + ACTIVITY_TABLE + STR_ADD_COLUMN + ACTIVITY_C_TITLE + STR_TEXT_NULL + ";";

            db.execSQL(sql1);
            db.execSQL(sql2);
            db.execSQL(sql3);
            db.execSQL(sql4);
        }

        if (oldVersion <= 11 && newVersion >= 12) {
            String sql = STR_ALTER_TABLE + COURSE_TABLE + STR_ADD_COLUMN + COURSE_C_LANGS + STR_TEXT_NULL + ";";
            db.execSQL(sql);
            sql = STR_ALTER_TABLE + COURSE_TABLE + STR_ADD_COLUMN + COURSE_C_IMAGE + STR_TEXT_NULL + ";";
            db.execSQL(sql);
        }

        if (oldVersion <= 12 && newVersion >= 13) {
            String sql = STR_ALTER_TABLE + TRACKER_LOG_TABLE + STR_ADD_COLUMN + TRACKER_LOG_C_COMPLETED + STR_INT_DEFAULT_O + ";";
            db.execSQL(sql);
        }
        // skip jump from 13 to 14
        if (oldVersion <= 14 && newVersion >= 15) {
            ContentValues values = new ContentValues();
            values.put(TRACKER_LOG_C_COMPLETED, true);
            db.update(TRACKER_LOG_TABLE, values, null, null);
        }

        if (oldVersion <= 15 && newVersion >= 16) {
            String sql = STR_ALTER_TABLE + COURSE_TABLE + STR_ADD_COLUMN + COURSE_C_DESC + STR_TEXT_NULL + ";";
            db.execSQL(sql);
        }

        if (oldVersion <= 16 && newVersion >= 17) {
            String sql = STR_ALTER_TABLE + COURSE_TABLE + STR_ADD_COLUMN + COURSE_C_ORDER_PRIORITY  + STR_INT_DEFAULT_O + ";";
            db.execSQL(sql);
        }

        if (oldVersion <= 17 && newVersion >= 18) {
            //create search table
            this.createSearchTable(db);

            // alter quiz results table
            String sql1 = STR_ALTER_TABLE + QUIZATTEMPTS_TABLE + STR_ADD_COLUMN + QUIZATTEMPTS_C_USERID  + STR_INT_DEFAULT_O + ";";
            db.execSQL(sql1);

            // alter tracker table
            String sql2 = STR_ALTER_TABLE + TRACKER_LOG_TABLE + STR_ADD_COLUMN + TRACKER_LOG_C_USERID  + STR_INT_DEFAULT_O + ";";
            db.execSQL(sql2);

            // create user table
            this.createUserTable(db);

        }

        if (oldVersion <= 18 && newVersion >= 19) {

            // alter quiz results table
            String sql1 = STR_ALTER_TABLE + QUIZATTEMPTS_TABLE + STR_ADD_COLUMN + QUIZATTEMPTS_C_SCORE + " real default 0;";
            db.execSQL(sql1);
            String sql2 = STR_ALTER_TABLE + QUIZATTEMPTS_TABLE + STR_ADD_COLUMN + QUIZATTEMPTS_C_PASSED  + STR_INT_DEFAULT_O + ";";
            db.execSQL(sql2);

            // alter user table
            String sql3 = STR_ALTER_TABLE + USER_TABLE + STR_ADD_COLUMN + USER_C_LAST_LOGIN_DATE + STR_DATETIME_NULL +";";
            db.execSQL(sql3);
            String sql4 = STR_ALTER_TABLE + USER_TABLE + STR_ADD_COLUMN + USER_C_NO_LOGINS  + STR_INT_DEFAULT_O + ";";
            db.execSQL(sql4);
        }

        if (oldVersion <= 19 && newVersion >= 20) {
            // alter quiz results table
            String sql1 = STR_ALTER_TABLE + QUIZATTEMPTS_TABLE + STR_ADD_COLUMN + QUIZATTEMPTS_C_MAXSCORE + " real default 0;";
            db.execSQL(sql1);
        }

        if (oldVersion <= 20 && newVersion >= 21) {
            // alter quiz results table
            String sql1 = STR_ALTER_TABLE + QUIZATTEMPTS_TABLE + STR_ADD_COLUMN + QUIZATTEMPTS_C_ACTIVITY_DIGEST + " text;";
            db.execSQL(sql1);
        }

        if (oldVersion <= 21 && newVersion >= 22) {
            // add points and badges columns
            String sql1 = STR_ALTER_TABLE + USER_TABLE + STR_ADD_COLUMN + USER_C_POINTS  + STR_INT_DEFAULT_O + ";";
            db.execSQL(sql1);
            String sql2 = STR_ALTER_TABLE + USER_TABLE + STR_ADD_COLUMN + USER_C_BADGES  + STR_INT_DEFAULT_O + ";";
            db.execSQL(sql2);
        }

        if (oldVersion <= 23 && newVersion >= 24) {
            // add field "sequencingMode" to Course table
            String sql1 = STR_ALTER_TABLE + COURSE_TABLE + STR_ADD_COLUMN + COURSE_C_SEQUENCING + STR_TEXT_DEFAULT + Course.SEQUENCING_MODE_NONE + "';";
            db.execSQL(sql1);
        }

        if (oldVersion <= 24 && newVersion >= 25) {
            // add field "type" to Tracker table
            String sql1 = STR_ALTER_TABLE + TRACKER_LOG_TABLE + STR_ADD_COLUMN + TRACKER_LOG_C_TYPE + " text ;";
            db.execSQL(sql1);
        }

        if (oldVersion <= 25 && newVersion >= 26) {
            // add field "exported" to Tracker table
            String sql1 = STR_ALTER_TABLE + TRACKER_LOG_TABLE + STR_ADD_COLUMN + TRACKER_LOG_C_EXPORTED  + STR_INT_DEFAULT_O + ";";
            db.execSQL(sql1);
        }

        if (oldVersion <= 26 && newVersion >= 27) {
            // add field "exported" to Tracker table
            String sql1 = STR_ALTER_TABLE + QUIZATTEMPTS_TABLE + STR_ADD_COLUMN + QUIZATTEMPTS_C_EXPORTED  + STR_INT_DEFAULT_O + ";";
            db.execSQL(sql1);
        }

        if (oldVersion <= 27 && newVersion >= 28) {
            createCourseGamificationTable(db);
            createActivityGamificationTable(db);

            // update tracker table
            String sql1 = STR_ALTER_TABLE + TRACKER_LOG_TABLE + STR_ADD_COLUMN + TRACKER_LOG_C_POINTS  + STR_INT_DEFAULT_O + ";";
            String sql2 = STR_ALTER_TABLE + TRACKER_LOG_TABLE + STR_ADD_COLUMN + TRACKER_LOG_C_EVENT + STR_TEXT_NULL + ";";
            db.execSQL(sql1);
            db.execSQL(sql2);

            // update quizattempt table
            String sql3 = STR_ALTER_TABLE + QUIZATTEMPTS_TABLE + STR_ADD_COLUMN + QUIZATTEMPTS_C_POINTS  + STR_INT_DEFAULT_O + ";";
            String sql4 = STR_ALTER_TABLE + QUIZATTEMPTS_TABLE + STR_ADD_COLUMN + QUIZATTEMPTS_C_EVENT + STR_TEXT_NULL + ";";
            db.execSQL(sql3);
            db.execSQL(sql4);
        }

        if (oldVersion <= 29 && newVersion >= 30) {
            // add fields for offline_register to User table
            db.execSQL(STR_ALTER_TABLE + USER_TABLE + STR_ADD_COLUMN + USER_C_OFFLINE_REGISTER + STR_INT_DEFAULT_O + ";");
            db.execSQL(STR_ALTER_TABLE + USER_TABLE + STR_ADD_COLUMN + USER_C_PASSWORDPLAIN + STR_TEXT_NULL + ";");
            db.execSQL(STR_ALTER_TABLE + USER_TABLE + STR_ADD_COLUMN + USER_C_EMAIL + STR_TEXT_NULL + ";");
            db.execSQL(STR_ALTER_TABLE + USER_TABLE + STR_ADD_COLUMN + USER_C_PHONE + STR_TEXT_NULL + ";");
            db.execSQL(STR_ALTER_TABLE + USER_TABLE + STR_ADD_COLUMN + USER_C_JOBTITLE + STR_TEXT_NULL + ";");
            db.execSQL(STR_ALTER_TABLE + USER_TABLE + STR_ADD_COLUMN + USER_C_ORGANIZATION + STR_TEXT_NULL + ";");
        }

        if (oldVersion < 40) {
            createUserCustomFieldsTable(db);
            createCustomFieldTable(db);
        }

        if (oldVersion < 41){
            // add the timetaken field
            db.execSQL(STR_ALTER_TABLE + QUIZATTEMPTS_TABLE + STR_ADD_COLUMN + QUIZATTEMPTS_C_TIMETAKEN + STR_INT_DEFAULT_O + ";");
            extractQuizAttemptsTimetaken(db);
        }

        if (oldVersion < 43){
            db.execSQL(STR_ALTER_TABLE + QUIZATTEMPTS_TABLE + STR_ADD_COLUMN + QUIZATTEMPTS_C_TYPE + STR_TEXT_DEFAULT + QuizAttempt.TYPE_QUIZ + "';");
        }

        if (oldVersion < 44){
            db.execSQL(STR_ALTER_TABLE + CUSTOM_FIELD_TABLE + STR_ADD_COLUMN + CUSTOM_FIELD_C_COLLECTION + STR_TEXT_NULL + ";");
            createCustomFieldsCollectionTable(db);
        }

        if (oldVersion < 45){
            db.execSQL(STR_ALTER_TABLE + CUSTOM_FIELD_TABLE + STR_ADD_COLUMN + CUSTOM_FIELD_C_VISIBLE_BY + STR_TEXT_NULL + ";");
            db.execSQL(STR_ALTER_TABLE + CUSTOM_FIELD_TABLE + STR_ADD_COLUMN + CUSTOM_FIELD_C_VISIBLE_VALUE + STR_TEXT_NULL + ";");
        }

        if (oldVersion < 46){
            db.execSQL(STR_ALTER_TABLE + CUSTOM_FIELD_TABLE + STR_ADD_COLUMN + CUSTOM_FIELD_C_COLLECTION_BY + STR_TEXT_NULL + ";");
        }

    }



    public void updateV43(long userId) {
        // update existing trackers
        ContentValues values = new ContentValues();
        values.put(TRACKER_LOG_C_USERID, userId);

        db.update(TRACKER_LOG_TABLE, values, "1=1", null);

        // update existing trackers
        ContentValues values2 = new ContentValues();
        values2.put(QUIZATTEMPTS_C_USERID, userId);

        db.update(QUIZATTEMPTS_TABLE, values2, "1=1", null);
    }

    public void extractQuizAttemptsTimetaken(SQLiteDatabase database){
        Cursor c1 = database.query(QUIZATTEMPTS_TABLE, null, null, null, null, null, null);
        c1.moveToFirst();
        while (!c1.isAfterLast()) {

            String data = c1.getString(c1.getColumnIndex(QUIZATTEMPTS_C_DATA));
            if (TextUtils.isEmpty(data)){
                c1.moveToNext();
                continue;
            }

            try {
                JSONObject logData = new JSONObject(data);
                String instanceID = logData.getString("instance_id");
                int attemptID = c1.getInt(c1.getColumnIndex(QUIZATTEMPTS_C_ID));
                long time = -1;

                String s = TRACKER_LOG_C_DATA + " LIKE ?";
                String[] args = new String[]{ "%" + instanceID + "%" };
                Cursor c = database.query(TRACKER_LOG_TABLE, null, s, args, null, null, null);
                if (c.getCount() > 0) {
                    c.moveToFirst();
                    String trackerData = c.getString(c.getColumnIndex(TRACKER_LOG_C_DATA));
                    JSONObject trackerJSON = new JSONObject(trackerData);
                    time = trackerJSON.getLong(QUIZATTEMPTS_C_TIMETAKEN);
                }
                c.close();

                if (time > 0){
                    ContentValues values = new ContentValues();
                    values.put(QUIZATTEMPTS_C_TIMETAKEN, time);
                    database.update(QUIZATTEMPTS_TABLE, values, QUIZATTEMPTS_C_ID + "=" + attemptID, null);
                }

            } catch (JSONException e) {
                // Pass
            }
            c1.moveToNext();
        }
        c1.close();
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
        } else if (this.toUpdate(course.getShortname(), course.getVersionId())) {
            long toUpdate = this.getCourseID(course.getShortname());

            // remove existing course info from search index
            this.searchIndexRemoveCourse(toUpdate);

            if (toUpdate != 0) {
                db.update(COURSE_TABLE, values, COURSE_C_ID + "=" + toUpdate, null);
                // remove all the old activities
                String s = ACTIVITY_C_COURSEID + "=?";
                String[] args = new String[]{String.valueOf(toUpdate)};
                db.delete(ACTIVITY_TABLE, s, args);

                // remove coursegamification
                // remove activitygamification

                return toUpdate;
            }
        }
        return -1;
    }

    public void insertActivityGamification(long activityId, List<GamificationEvent> gamificationEvents) {

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

    public void insertCourseGamification(long courseId, List<GamificationEvent> gamificationEvents) {

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

        if (user.getUsername() == null || user.getUsername().equals("")) {
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
            String[] args = new String[]{String.valueOf(userId)};
            db.update(USER_TABLE, values, s, args);
        }
        insertOrUpdateCustomFields(user);

        return userId;
    }

    private void insertOrUpdateCustomFields(User user) {
        List<ContentValues> contenValuesList = convertCFtoCV(user.getUserCustomFields());
        for (ContentValues contentValues : contenValuesList) {
            contentValues.put(USER_CF_USERNAME, user.getUsername());
            try {
                db.insertOrThrow(USER_CF_TABLE, null, contentValues);
            } catch (SQLiteException e) {
                String s = USER_CF_USERNAME + STR_EQUALS_AND + CF_FIELD_KEY + "=?";
                String[] args = new String[] { String.valueOf(user.getUsername()), contentValues.getAsString(CF_FIELD_KEY) };
                db.update(USER_CF_TABLE, contentValues, s, args);
            }
        }

    }

    private List<ContentValues> convertCFtoCV(Map<String, CustomValue> customFields) {

        List<ContentValues> contentValuesList = new ArrayList<>();
        for (Map.Entry<String, CustomValue> entry : customFields.entrySet()) {
            String key = entry.getKey();
            CustomValue customValue = entry.getValue();
            Object value = customValue.getValue();

            ContentValues contentValues = new ContentValues();
            contentValues.put(CF_FIELD_KEY, key);
            if (value instanceof String) {
                contentValues.put(CF_VALUE_STR, (String) value);
            } else if (value instanceof Integer) {
                contentValues.put(CF_VALUE_INT, (Integer) value);
            } else if (value instanceof Float) {
                contentValues.put(CF_VALUE_FLOAT, (Float) value);
            } else if (value instanceof Boolean) {
                contentValues.put(CF_VALUE_BOOL, (Boolean) value);
            }
            contentValuesList.add(contentValues);
        }

        return contentValuesList;
    }

    public void deleteUser(String username) {
        // delete activities
        String s = USER_C_USERNAME + "=?";
        String[] args = new String[]{String.valueOf(username)};
        db.delete(USER_TABLE, s, args);
    }

    public long isUser(String username) {
        String s = USER_C_USERNAME + "=? COLLATE NOCASE";
        String[] args = new String[]{username};
        Cursor c = db.query(USER_TABLE, null, s, args, null, null, null);
        if (c.getCount() == 0) {
            c.close();
            return -1;
        } else {
            c.moveToFirst();
            int userId = c.getInt(c.getColumnIndex(USER_C_ID));
            c.close();
            return userId;
        }
    }


    public int getCourseID(String shortname) {
        String s = COURSE_C_SHORTNAME + "=?";
        String[] args = new String[]{shortname};
        Cursor c = db.query(COURSE_TABLE, null, s, args, null, null, null);
        if (c.getCount() == 0) {
            c.close();
            return 0;
        } else {
            c.moveToFirst();
            int courseId = c.getInt(c.getColumnIndex(COURSE_C_ID));
            c.close();
            return courseId;
        }
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


    public List<Course> getAllCourses() {
        ArrayList<Course> courses = new ArrayList<>();
        String order = COURSE_C_ORDER_PRIORITY + " DESC, " + COURSE_C_TITLE + " ASC";
        Cursor c = db.query(COURSE_TABLE, null, null, null, null, null, order);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            Course course = setupCourseObject(c);
            courses.add(course);
            c.moveToNext();
        }
        c.close();
        return courses;
    }

    public List<QuizAttempt> getAllQuizAttempts() {
        return getQuizAttempts(db);
    }

    public List<QuizAttempt> getQuizAttempts(SQLiteDatabase database){
        ArrayList<QuizAttempt> quizAttempts = new ArrayList<>();
        Cursor c = database.query(QUIZATTEMPTS_TABLE, null, null, null, null, null, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            QuizAttempt qa = fetchQuizAttempt(c);
            quizAttempts.add(qa);
            c.moveToNext();
        }
        c.close();
        return quizAttempts;
    }

    private QuizAttempt fetchQuizAttempt(Cursor c){
        QuizAttempt qa = new QuizAttempt();
        qa.setId(c.getInt(c.getColumnIndex(QUIZATTEMPTS_C_ID)));
        qa.setActivityDigest(c.getString(c.getColumnIndex(QUIZATTEMPTS_C_ACTIVITY_DIGEST)));
        qa.setData(c.getString(c.getColumnIndex(QUIZATTEMPTS_C_DATA)));
        qa.setSent(Boolean.parseBoolean(c.getString(c.getColumnIndex(QUIZATTEMPTS_C_SENT))));
        qa.setDateTimeFromString(c.getString(c.getColumnIndex(QUIZATTEMPTS_C_DATETIME)));
        qa.setCourseId(c.getLong(c.getColumnIndex(QUIZATTEMPTS_C_COURSEID)));
        qa.setUserId(c.getLong(c.getColumnIndex(QUIZATTEMPTS_C_USERID)));
        qa.setScore(c.getFloat(c.getColumnIndex(QUIZATTEMPTS_C_SCORE)));
        qa.setMaxscore(c.getFloat(c.getColumnIndex(QUIZATTEMPTS_C_MAXSCORE)));
        qa.setPassed(Boolean.parseBoolean(c.getString(c.getColumnIndex(QUIZATTEMPTS_C_PASSED))));
        qa.setType(c.getString(c.getColumnIndex(QUIZATTEMPTS_C_TYPE)));
        qa.setTimetaken(c.getInt(c.getColumnIndex(QUIZATTEMPTS_C_TIMETAKEN)));

        return qa;
    }

    public List<Course> getCoursesForUser(long userId) {
        ArrayList<Course> courses = new ArrayList<>();
        String order = COURSE_C_ORDER_PRIORITY + " DESC, " + COURSE_C_TITLE + " ASC";
        Cursor c = db.query(COURSE_TABLE, null, null, null, null, null, order);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            Course course = setupCourseObject(c);
            this.courseSetProgress(course, userId);
            courses.add(course);
            c.moveToNext();
        }
        c.close();
        return courses;
    }

    public Course getCourse(long courseId, long userId) {
        Course course = null;
        String s = COURSE_C_ID + "=?";
        String[] args = new String[]{String.valueOf(courseId)};
        Cursor c = db.query(COURSE_TABLE, null, s, args, null, null, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            course = setupCourseObject(c);
            this.courseSetProgress(course, userId);
            c.moveToNext();
        }
        c.close();
        return course;
    }

    public long getCourseIdByShortname(String shortname) {
        String s = COURSE_C_SHORTNAME + "=?";
        String[] args = new String[]{shortname};
        Cursor c = db.query(COURSE_TABLE, null, s, args, null, null, null);

        if (c.getCount() > 0) {
            c.moveToFirst();
            return c.getLong(c.getColumnIndex(COURSE_C_ID));
        }
        c.close();
        return -1;
    }

    private Course setupCourseObject(Cursor c){
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
        return course;
    }

    private Course courseSetProgress(Course course, long userId) {
        // get no activities
        String s = ACTIVITY_C_COURSEID + "=?";
        String[] args = new String[]{String.valueOf(course.getCourseId())};
        Cursor c = db.query(ACTIVITY_TABLE, null, s, args, null, null, null);
        course.setNoActivities(c.getCount());
        c.close();

        // get no completed
        String sqlCompleted = "SELECT DISTINCT " + TRACKER_LOG_C_ACTIVITYDIGEST + " FROM " + TRACKER_LOG_TABLE +
                STR_WHERE + TRACKER_LOG_C_COURSEID + "=" + course.getCourseId() +
                " AND " + TRACKER_LOG_C_USERID + "=" + userId +
                " AND " + TRACKER_LOG_C_COMPLETED + "=1" +
                " AND " + TRACKER_LOG_C_ACTIVITYDIGEST + " IN ( SELECT " + ACTIVITY_C_ACTIVITYDIGEST + " FROM " + ACTIVITY_TABLE + STR_WHERE + ACTIVITY_C_COURSEID + "=" + course.getCourseId() + ")";
        c = db.rawQuery(sqlCompleted, null);
        course.setNoActivitiesCompleted(c.getCount());
        c.close();

        return course;
    }

    public List<Activity> getCourseActivities(long courseId) {
        ArrayList<Activity> activities = new ArrayList<>();
        String s = ACTIVITY_C_COURSEID + "=?";
        String[] args = new String[]{String.valueOf(courseId)};
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

    public List<Activity> getCourseQuizzes(long courseId) {
        ArrayList<Activity> quizzes = new ArrayList<>();
        String s = ACTIVITY_C_COURSEID + STR_EQUALS_AND + ACTIVITY_C_ACTTYPE + STR_EQUALS_AND + ACTIVITY_C_SECTIONID + ">0";
        String[] args = new String[]{String.valueOf(courseId), "quiz"};
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

    public QuizStats getQuizAttempt(String digest, long userId) {
        QuizStats qs = new QuizStats();
        qs.setDigest(digest);
        qs.setAttempted(false);
        qs.setPassed(false);

        // find if attempted
        String s1 = QUIZATTEMPTS_C_USERID + STR_EQUALS_AND + QUIZATTEMPTS_C_ACTIVITY_DIGEST + "=?";
        String[] args1 = new String[]{String.valueOf(userId), digest};
        Cursor query = db.query(QUIZATTEMPTS_TABLE, null, s1, args1, null, null, null);
        qs.setNumAttempts(query.getCount());
        if (query.getCount() == 0) {
            return qs;
        }

        qs.setNumAttempts(query.getCount());
        float scoreSum = 0;
        query.moveToFirst();
        while (!query.isAfterLast()) {
            float userScore = query.getFloat(query.getColumnIndex(QUIZATTEMPTS_C_SCORE));
            if (userScore > qs.getUserScore()) {
                qs.setUserScore(userScore);
            }
            if (query.getInt(query.getColumnIndex(QUIZATTEMPTS_C_PASSED)) != 0) {
                qs.setPassed(true);
            }
            scoreSum += userScore;
            qs.setMaxScore(query.getFloat(query.getColumnIndex(QUIZATTEMPTS_C_MAXSCORE)));
            query.moveToNext();
        }
        query.close();

        qs.setAverageScore(scoreSum / qs.getNumAttempts());
        qs.setAttempted(true);

        return qs;
    }


    public List<QuizAttempt> getGlobalQuizAttempts(long userId, String prefLang) {

        // find if attempted
        String s1 = QUIZATTEMPTS_C_USERID + STR_EQUALS_AND + QUIZATTEMPTS_C_TYPE + "=?";
        String order = QUIZATTEMPTS_C_DATETIME + " DESC";
        String[] args1 = new String[]{String.valueOf(userId), QuizAttempt.TYPE_QUIZ};
        Cursor c = db.query(QUIZATTEMPTS_TABLE, null, s1, args1, null, null, order);

        ArrayList<QuizAttempt> attempts = new ArrayList<>();
        Map<Long, Course> fetchedCourses = new HashMap<>();
        Map<Long, CompleteCourse> fetchedXMLCourses = new HashMap<>();

        c.moveToFirst();
        while (!c.isAfterLast()) {
            QuizAttempt qa = fetchQuizAttempt(c);
            c.moveToNext();

            long courseId = qa.getCourseId();
            Course course = fetchedCourses.get(courseId);
            if (course == null) {
                course = this.getCourse(courseId, userId);
                fetchedCourses.put(courseId, course);
            }

            Activity activity = this.getActivityByDigest(qa.getActivityDigest());
            if (activity == null || course == null) {
                continue;
            }
            qa.setCourseTitle(course.getTitle(prefLang));
            qa.setQuizTitle(activity.getTitle(prefLang));
            int sectionOrderId = activity.getSectionId();
            CompleteCourse parsed = fetchedXMLCourses.get(courseId);
            if (parsed == null) {
                try {
                    CourseXMLReader cxr = new CourseXMLReader(course.getCourseXMLLocation(), course.getCourseId(), ctx);
                    cxr.parse(CourseXMLReader.ParseMode.COMPLETE);
                    parsed = cxr.getParsedCourse();
                    fetchedXMLCourses.put(courseId, parsed);
                    qa.setSectionTitle(parsed.getSection(sectionOrderId).getTitle(prefLang));
                } catch (InvalidXMLException ixmle) {
                    Log.d(TAG, "Invalid course xml file", ixmle);
                    Mint.logException(ixmle);
                }
            } else {
                qa.setSectionTitle(parsed.getSection(sectionOrderId).getTitle(prefLang));
            }

            attempts.add(qa);


        }
        c.close();

        return attempts;
    }

    public List<QuizAttempt> getQuizAttempts(String digest, long userId) {

        // find if attempted
        String s1 = QUIZATTEMPTS_C_USERID + STR_EQUALS_AND + QUIZATTEMPTS_C_ACTIVITY_DIGEST + "=?";
        String[] args1 = new String[]{String.valueOf(userId), digest};
        Cursor c = db.query(QUIZATTEMPTS_TABLE, null, s1, args1, null, null, null);

        ArrayList<QuizAttempt> attempts = new ArrayList<>();

        c.moveToFirst();
        while (!c.isAfterLast()) {
            QuizAttempt qa = new QuizAttempt();
            qa.setId(c.getInt(c.getColumnIndex(QUIZATTEMPTS_C_ID)));
            qa.setActivityDigest(c.getString(c.getColumnIndex(QUIZATTEMPTS_C_ACTIVITY_DIGEST)));
            qa.setData(c.getString(c.getColumnIndex(QUIZATTEMPTS_C_DATA)));
            qa.setDateTimeFromString(c.getString(c.getColumnIndex(QUIZATTEMPTS_C_DATETIME)));
            qa.setCourseId(c.getLong(c.getColumnIndex(QUIZATTEMPTS_C_COURSEID)));
            qa.setScore(c.getFloat(c.getColumnIndex(QUIZATTEMPTS_C_SCORE)));
            qa.setMaxscore(c.getFloat(c.getColumnIndex(QUIZATTEMPTS_C_MAXSCORE)));
            qa.setPassed(c.getInt(c.getColumnIndex(QUIZATTEMPTS_C_PASSED)) != 0);
            qa.setTimetaken(c.getInt(c.getColumnIndex(QUIZATTEMPTS_C_TIMETAKEN)));
            attempts.add(qa);
            c.moveToNext();
        }
        c.close();

        return attempts;
    }

    public void insertTracker(int courseId, String digest, String data, String type, boolean completed, String event, int points) {
        long userId = this.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));

        ContentValues values = new ContentValues();
        values.put(TRACKER_LOG_C_COURSEID, courseId);
        values.put(TRACKER_LOG_C_ACTIVITYDIGEST, digest);
        values.put(TRACKER_LOG_C_DATA, data);
        values.put(TRACKER_LOG_C_COMPLETED, completed);
        values.put(TRACKER_LOG_C_USERID, userId);
        values.put(TRACKER_LOG_C_TYPE, type);
        values.put(TRACKER_LOG_C_EVENT, event);
        values.put(TRACKER_LOG_C_POINTS, points);
        db.insertOrThrow(TRACKER_LOG_TABLE, null, values);

        this.incrementUserPoints(userId, points);

    }

    public void resetCourse(long courseId, long userId) {
        // delete quiz results
        this.deleteQuizAttempts(courseId, userId);
        this.deleteTrackers(courseId, userId);
    }

    public void deleteCourse(int courseId) {
        // remove from search index
        this.searchIndexRemoveCourse(courseId);

        // delete activities
        String s = ACTIVITY_C_COURSEID + "=?";
        String[] args = new String[]{String.valueOf(courseId)};
        db.delete(ACTIVITY_TABLE, s, args);

        // delete course
        s = COURSE_C_ID + "=?";
        args = new String[]{String.valueOf(courseId)};
        db.delete(COURSE_TABLE, s, args);

    }


    public boolean isInstalled(String shortname) {
        String s = COURSE_C_SHORTNAME + "=?";
        String[] args = new String[]{shortname};
        Cursor c = db.query(COURSE_TABLE, null, s, args, null, null, null);
        boolean isInstalled = (c.getCount() > 0);
        c.close();
        return isInstalled;
    }

    public boolean toUpdate(String shortname, Double version) {
        String s = COURSE_C_SHORTNAME + STR_EQUALS_AND + COURSE_C_VERSIONID + "< ?";
        String[] args = new String[]{shortname, String.format("%.0f", version)};
        Cursor c = db.query(COURSE_TABLE, null, s, args, null, null, null);
        boolean toUpdate = (c.getCount() > 0);
        c.close();
        return toUpdate;
    }

    public long getUserId(String username) {
        String s = USER_C_USERNAME + "=? ";
        String[] args = new String[]{username};
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

    private User fetchUser(Cursor c) {
        User u = new User();
        u.setUserId(c.getLong(c.getColumnIndex(USER_C_ID)));
        u.setApiKey(c.getString(c.getColumnIndex(USER_C_APIKEY)));
        u.setUsername(c.getString(c.getColumnIndex(USER_C_USERNAME)));
        u.setFirstname(c.getString(c.getColumnIndex(USER_C_FIRSTNAME)));
        u.setLastname(c.getString(c.getColumnIndex(USER_C_LASTNAME)));
        u.setPoints(c.getInt(c.getColumnIndex(USER_C_POINTS)));
        u.setBadges(c.getInt(c.getColumnIndex(USER_C_BADGES)));
        u.setPasswordEncrypted(c.getString(c.getColumnIndex(USER_C_PASSWORDENCRYPTED)));
        u.setOfflineRegister(c.getInt(c.getColumnIndex(USER_C_OFFLINE_REGISTER)) > 0);
        u.setPhoneNo(c.getString(c.getColumnIndex(USER_C_PHONE)));
        u.setEmail(c.getString(c.getColumnIndex(USER_C_EMAIL)));
        u.setJobTitle(c.getString(c.getColumnIndex(USER_C_JOBTITLE)));
        u.setOrganisation(c.getString(c.getColumnIndex(USER_C_ORGANIZATION)));
        if (u.isOfflineRegister()) {
            u.setPassword(c.getString(c.getColumnIndex(USER_C_PASSWORDPLAIN)));
            u.setPasswordAgain(c.getString(c.getColumnIndex(USER_C_PASSWORDPLAIN)));
        }
        fetchUserCustomFields(u);

        return u;
    }

    private void fetchUserCustomFields(User u) {

        String s = USER_C_USERNAME + "=? ";
        String[] args = new String[]{ u.getUsername() };
        Cursor c = db.query(USER_CF_TABLE, null, s, args, null, null, null);

        List<CustomField> cFields = this.getCustomFields();

        c.moveToFirst();
        while (!c.isAfterLast()) {
            String key = c.getString(c.getColumnIndex(CF_FIELD_KEY));
            for (CustomField field : cFields){
                if (TextUtils.equals(key, field.getKey())){
                    if (field.isString() || field.isChoices()){
                        // Internally, we just save the choices key value as a str
                        String value = c.getString(c.getColumnIndex(CF_VALUE_STR));
                        u.putCustomField(key, new CustomValue<>(value));
                    }
                    else if (field.isBoolean()){
                        boolean value = c.getInt(c.getColumnIndex(CF_VALUE_BOOL)) == 1;
                        u.putCustomField(key, new CustomValue<>(value));
                    }
                    else if (field.isInteger()){
                        int value = c.getInt(c.getColumnIndex(CF_VALUE_INT));
                        u.putCustomField(key, new CustomValue<>(value));
                    }
                    else if (field.isFloat()){
                        float value = c.getFloat(c.getColumnIndex(CF_VALUE_FLOAT));
                        u.putCustomField(key, new CustomValue<>(value));
                    }
                }
            }
            c.moveToNext();
        }
        c.close();
    }

    private User getUser(Cursor c) throws UserNotFoundException {
        User u = null;
        c.moveToFirst();
        while (!c.isAfterLast()) {
            u = fetchUser(c);
            c.moveToNext();
        }
        c.close();
        if (u == null) {
            throw new UserNotFoundException();
        }

        return u;
    }

    public User getUser(long userId) throws UserNotFoundException {
        String s = USER_C_ID + "=? ";
        String[] args = new String[]{String.valueOf(userId)};
        Cursor c = db.query(USER_TABLE, null, s, args, null, null, null);
        return getUser(c);
    }

    public User getUser(String userName) throws UserNotFoundException {
        Log.d(TAG, "getting username: " + userName);
        String s = USER_C_USERNAME + "=? ";
        String[] args = new String[]{userName};
        Cursor c = db.query(USER_TABLE, null, s, args, null, null, null);
        return getUser(c);
    }

    public User getOneRegisteredUser() throws UserNotFoundException {
        String s = USER_C_OFFLINE_REGISTER + "=? ";
        String[] args = new String[]{"0"};

        Cursor c = db.query(USER_TABLE, null, s, args, null, null, null);
        return getUser(c);
    }

    public List<User> getAllUsers() {
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
        String[] args = new String[]{String.valueOf(userId)};
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
        args = new String[]{String.valueOf(userId)};
        db.update(USER_TABLE, values, s, args);

        DateTime lastUpdate = new DateTime();
        this.insertOrUpdateUserLeaderboard(username, fullname, currentPoints, lastUpdate);
    }

    public List<Points> getUserPoints(long userId, Course courseFilter, boolean onlyTrackerlogs) {
        ArrayList<Points> points = new ArrayList<>();

        // Points from Tracker
        String s = TRACKER_LOG_C_USERID + STR_EQUALS_AND + TRACKER_LOG_C_POINTS + "!=0";
        String[] args = new String[]{String.valueOf(userId)};
        Cursor c = db.query(TRACKER_LOG_TABLE, null, s, args, null, null, null);
        c.moveToFirst();

        String prefLang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());

        while (!c.isAfterLast()) {

            Activity activity = this.getActivityByDigest(c.getString(c.getColumnIndex(TRACKER_LOG_C_ACTIVITYDIGEST)));
            Course course = this.getCourse(c.getInt(c.getColumnIndex(TRACKER_LOG_C_COURSEID)), userId);

            if (courseFilter != null && course != null && courseFilter.getCourseId() != course.getCourseId()) {
                c.moveToNext();
                continue;
            }

            Points p = new Points();
            p.setDateTime(c.getString(c.getColumnIndex(TRACKER_LOG_C_DATETIME)));
            p.setPointsAwarded(c.getInt(c.getColumnIndex(TRACKER_LOG_C_POINTS)));
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
                default:
                    // do nothing
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
        String qa = QUIZATTEMPTS_C_USERID + STR_EQUALS_AND + QUIZATTEMPTS_C_POINTS + "!=0";
        String[] qaargs = new String[]{String.valueOf(userId)};
        Cursor qac = db.query(QUIZATTEMPTS_TABLE, null, qa, qaargs, null, null, null);
        qac.moveToFirst();
        while (!qac.isAfterLast()) {
            Points p = new Points();
            p.setDateTime(qac.getString(qac.getColumnIndex(QUIZATTEMPTS_C_DATETIME)));
            p.setPointsAwarded(qac.getInt(qac.getColumnIndex(QUIZATTEMPTS_C_POINTS)));
            p.setEvent(qac.getString(qac.getColumnIndex(QUIZATTEMPTS_C_EVENT)));

            // get course and activity title
            String description = qac.getString(qac.getColumnIndex(QUIZATTEMPTS_C_EVENT));
            Activity activity = this.getActivityByDigest(qac.getString(qac.getColumnIndex(QUIZATTEMPTS_C_ACTIVITY_DIGEST)));

            if (activity != null) {

                if (courseFilter != null && courseFilter.getCourseId() != activity.getCourseId()) {
                    qac.moveToNext();
                    continue;
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

    public void updateUserBadges(String userName, int badges) {
        ContentValues values = new ContentValues();
        values.put(USER_C_BADGES, badges);
        String s = USER_C_USERNAME + "=? ";
        String[] args = new String[]{userName};
        db.update(USER_TABLE, values, s, args);
    }

    public void updateUserBadges(long userId, int badges) {
        ContentValues values = new ContentValues();
        values.put(USER_C_BADGES, badges);
        String s = USER_C_ID + "=? ";
        String[] args = new String[]{String.valueOf(userId)};
        db.update(USER_TABLE, values, s, args);
    }

    public int getSentTrackersCount() {
        String s = TRACKER_LOG_C_SUBMITTED + "=? ";
        String[] args = new String[]{"1"};
        Cursor c = db.query(TRACKER_LOG_TABLE, null, s, args, null, null, null);
        int count = c.getCount();
        c.close();
        return count;
    }

    public int getUnsentTrackersCount() {
        String s = TRACKER_LOG_C_SUBMITTED + "=? ";
        String[] args = new String[]{"0"};
        Cursor c = db.query(TRACKER_LOG_TABLE, null, s, args, null, null, null);
        int count = c.getCount();
        c.close();
        return count;
    }

    public int getUnexportedTrackersCount() {
        String s = TRACKER_LOG_C_SUBMITTED + STR_EQUALS_AND + TRACKER_LOG_C_EXPORTED + "=? ";
        String[] args = new String[]{"0", "0"};
        Cursor c = db.query(TRACKER_LOG_TABLE, null, s, args, null, null, null);
        int count = c.getCount();
        c.close();
        return count;
    }


    public Payload getUnsentTrackers(long userId) {
        String s = TRACKER_LOG_C_SUBMITTED + STR_EQUALS_AND + TRACKER_LOG_C_USERID + "=? ";
        String[] args = new String[]{"0", String.valueOf(userId)};
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
                json.put(TRACKER_LOG_C_COMPLETED, c.getInt(c.getColumnIndex(TRACKER_LOG_C_COMPLETED)));
                json.put(TRACKER_LOG_C_ACTIVITYDIGEST, (digest != null) ? digest : "");
                json.put(TRACKER_LOG_C_EVENT, c.getString(c.getColumnIndex(TRACKER_LOG_C_EVENT)));
                json.put(TRACKER_LOG_C_POINTS, c.getInt(c.getColumnIndex(TRACKER_LOG_C_POINTS)));
                Course m = this.getCourse(c.getLong(c.getColumnIndex(TRACKER_LOG_C_COURSEID)), userId);
                if (m != null) {
                    json.put("course", m.getShortname());
                }
                String trackerType = c.getString(c.getColumnIndex(TRACKER_LOG_C_TYPE));
                if (trackerType != null) {
                    json.put("type", trackerType);
                }
                content = json.toString();
            } catch (JSONException jsone) {
                Mint.logException(jsone);
                Log.d(TAG, "error creating unsent trackers", jsone);
            }

            so.setContent(content);
            sl.add(so);
            c.moveToNext();
        }
        Payload p = new Payload(sl);
        c.close();

        return p;
    }

    public List<TrackerLog> getUnexportedTrackers(long userId) {
        String s = TRACKER_LOG_C_SUBMITTED + STR_EQUALS_AND + TRACKER_LOG_C_USERID + STR_EQUALS_AND + TRACKER_LOG_C_EXPORTED + "=? ";
        String[] args = new String[]{"0", String.valueOf(userId), "0"};
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
                json.put(TRACKER_LOG_C_COMPLETED, c.getInt(c.getColumnIndex(TRACKER_LOG_C_COMPLETED)));
                json.put(TRACKER_LOG_C_ACTIVITYDIGEST, (digest != null) ? digest : "");
                json.put(TRACKER_LOG_C_EVENT, c.getString(c.getColumnIndex(TRACKER_LOG_C_EVENT)));
                json.put(TRACKER_LOG_C_POINTS, c.getInt(c.getColumnIndex(TRACKER_LOG_C_POINTS)));
                Course m = this.getCourse(c.getLong(c.getColumnIndex(TRACKER_LOG_C_COURSEID)), userId);
                if (m != null) {
                    json.put("course", m.getShortname());
                }
                String trackerType = c.getString(c.getColumnIndex(TRACKER_LOG_C_TYPE));
                if (trackerType != null) {
                    json.put("type", trackerType);
                }
                content = json.toString();
            } catch (JSONException jsone) {
                Mint.logException(jsone);
                Log.d(TAG, "error creating unexported trackers", jsone);
            }

            tracker.setContent(content);
            trackers.add(tracker);
            c.moveToNext();
        }
        c.close();
        return trackers;
    }

    public void markLogsAndQuizzesExported() {
        ContentValues trackerValues = new ContentValues();
        trackerValues.put(TRACKER_LOG_C_EXPORTED, 1);
        db.update(TRACKER_LOG_TABLE, trackerValues, null, null);

        ContentValues quizValues = new ContentValues();
        quizValues.put(QUIZATTEMPTS_C_EXPORTED, 1);
        db.update(QUIZATTEMPTS_TABLE, quizValues, null, null);
    }

    public int markLogSubmitted(long rowId) {
        ContentValues values = new ContentValues();
        values.put(TRACKER_LOG_C_SUBMITTED, 1);

        return db.update(TRACKER_LOG_TABLE, values, TRACKER_LOG_C_ID + "=" + rowId, null);
    }

    public long insertQuizAttempt(QuizAttempt qa) {
        ContentValues values = createContentValuesFromQuizAttempt(qa);
        long result = db.insertOrThrow(QUIZATTEMPTS_TABLE, null, values);

        // increment the users points
        this.incrementUserPoints(qa.getUserId(), qa.getPoints());
        return result;
    }

    public void updateQuizAttempt(QuizAttempt qa) {
        ContentValues values = createContentValuesFromQuizAttempt(qa);
        db.update(QUIZATTEMPTS_TABLE, values, QUIZATTEMPTS_C_ID + "=" + qa.getId(), null);
    }

    public void insertQuizAttempts(List<QuizAttempt> quizAttempts) {
        beginTransaction();
        for (QuizAttempt qa : quizAttempts) {
            ContentValues values = createContentValuesFromQuizAttempt(qa);
            values.put(QUIZATTEMPTS_C_SENT, qa.isSent());
            values.put(QUIZATTEMPTS_C_DATETIME, qa.getDateTimeString());
            db.insertOrThrow(QUIZATTEMPTS_TABLE, null, values);
        }
        endTransaction(true);
    }

    private ContentValues createContentValuesFromQuizAttempt(QuizAttempt qa){
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
        values.put(QUIZATTEMPTS_C_TIMETAKEN, qa.getTimetaken());
        values.put(QUIZATTEMPTS_C_TYPE, qa.getType());
        return values;
    }

    public List<QuizAttempt> getUnsentQuizAttempts() {
        String s = QUIZATTEMPTS_C_SENT + "=? ";
        String[] args = new String[]{"0"};
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
                qa.setTimetaken(c.getInt(c.getColumnIndex(QUIZATTEMPTS_C_TIMETAKEN)));
                User u = this.getUser(qa.getUserId());
                qa.setUser(u);
                quizAttempts.add(qa);
            } catch (UserNotFoundException unfe) {
                // do nothing
            }
            c.moveToNext();
        }
        c.close();
        return quizAttempts;
    }

    public List<QuizAttempt> getUnexportedQuizAttempts(long userId) {
        String s = QUIZATTEMPTS_C_SENT + STR_EQUALS_AND + QUIZATTEMPTS_C_EXPORTED + STR_EQUALS_AND + QUIZATTEMPTS_C_USERID + "=? ";
        String[] args = new String[]{"0", "0", String.valueOf(userId)};
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
                qa.setTimetaken(c.getInt(c.getColumnIndex(QUIZATTEMPTS_C_TIMETAKEN)));
                qa.setType(c.getString(c.getColumnIndex(QUIZATTEMPTS_C_TYPE)));
                User u = this.getUser(qa.getUserId());
                qa.setUser(u);
                quizAttempts.add(qa);
            } catch (UserNotFoundException unfe) {
                // do nothing
            }
            c.moveToNext();
        }
        c.close();
        return quizAttempts;
    }

    public int markQuizSubmitted(long rowId) {
        ContentValues values = new ContentValues();
        values.put(QUIZATTEMPTS_C_SENT, 1);

        String s = QUIZATTEMPTS_C_ID + "=? ";
        String[] args = new String[]{String.valueOf(rowId)};
        return db.update(QUIZATTEMPTS_TABLE, values, s, args);
    }

    public void deleteQuizAttempts(long courseId, long userId) {
        // delete any quiz attempts
        String s = QUIZATTEMPTS_C_COURSEID + STR_EQUALS_AND + QUIZATTEMPTS_C_USERID + "=?";
        String[] args = new String[]{String.valueOf(courseId), String.valueOf(userId)};
        db.delete(QUIZATTEMPTS_TABLE, s, args);
    }

    public boolean isQuizFirstAttempt(String digest) {
        // digest could be null (quiz wrongly configured)
        if (digest == null){
            return false;
        }
        //get current user id
        long userId = this.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));

        String s = QUIZATTEMPTS_C_ACTIVITY_DIGEST + STR_EQUALS_AND + QUIZATTEMPTS_C_USERID + "=? ";
        String[] args = new String[]{digest, String.valueOf(userId)};
        Cursor c = db.query(QUIZATTEMPTS_TABLE, null, s, args, null, null, null);
        int count = c.getCount();
        Log.d(TAG, "isQuizFirstAttempt returned " + count + " rows");
        c.close();
        return (count == 0);

    }

    public boolean isQuizFirstAttemptToday(String digest) {
        // digest could be null (quiz wrongly configured)
        if (digest == null){
            return false;
        }
        //get current user id
        long userId = this.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);

        String todayString = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(today.getTime());

        String s = QUIZATTEMPTS_C_ACTIVITY_DIGEST + STR_EQUALS_AND + QUIZATTEMPTS_C_USERID + STR_EQUALS_AND + QUIZATTEMPTS_C_DATETIME + ">=?";
        String[] args = new String[]{digest, String.valueOf(userId), todayString};
        Cursor c = db.query(QUIZATTEMPTS_TABLE, null, s, args, null, null, null);
        int count = c.getCount();
        Log.d(TAG, "isQuizFirstAttemptToday returned " + count + " rows");
        c.close();
        return (count == 0);
    }

    public boolean isActivityFirstAttemptToday(String digest) {
        // digest could be null (activity wrongly configured)
        if (digest == null){
            return false;
        }
        //get current user id
        long userId = this.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);

        String todayString = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(today.getTime());

        String s = TRACKER_LOG_C_ACTIVITYDIGEST + STR_EQUALS_AND + TRACKER_LOG_C_USERID + STR_EQUALS_AND + TRACKER_LOG_C_DATETIME + ">=?";
        String[] args = new String[]{digest, String.valueOf(userId), todayString};
        Cursor c = db.query(TRACKER_LOG_TABLE, null, s, args, null, null, null);
        int count = c.getCount();
        c.close();
        return (count == 0);
    }

    public boolean isMediaPlayed(String digest) {
        //get current user id
        long userId = this.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));

        String s = TRACKER_LOG_C_ACTIVITYDIGEST + STR_EQUALS_AND + TRACKER_LOG_C_USERID + STR_EQUALS_AND + TRACKER_LOG_C_COMPLETED + "=1";
        String[] args = new String[]{digest, String.valueOf(userId)};
        Cursor c = db.query(TRACKER_LOG_TABLE, null, s, args, null, null, null);
        int count = c.getCount();
        c.close();
        return (count > 0);
    }

    public void deleteTrackers(long courseId, long userId) {
        // delete any trackers
        String s = TRACKER_LOG_C_COURSEID + STR_EQUALS_AND + TRACKER_LOG_C_USERID + "=? ";
        String[] args = new String[]{String.valueOf(courseId), String.valueOf(userId)};
        db.delete(TRACKER_LOG_TABLE, s, args);
    }

    public boolean activityAttempted(long courseId, String digest, long userId) {
        String s = TRACKER_LOG_C_ACTIVITYDIGEST + STR_EQUALS_AND +
                TRACKER_LOG_C_USERID + STR_EQUALS_AND +
                TRACKER_LOG_C_COURSEID + "=?";
        String[] args = new String[]{digest, String.valueOf(userId), String.valueOf(courseId)};
        Cursor c = db.query(TRACKER_LOG_TABLE, null, s, args, null, null, null);
        boolean isAttempted = (c.getCount() > 0);
        c.close();
        return isAttempted;
    }

    public boolean activityCompleted(int courseId, String digest, long userId) {
        String s = TRACKER_LOG_C_ACTIVITYDIGEST + STR_EQUALS_AND +
                TRACKER_LOG_C_COURSEID + STR_EQUALS_AND +
                TRACKER_LOG_C_USERID + STR_EQUALS_AND +
                TRACKER_LOG_C_COMPLETED + "=1";
        String[] args = new String[]{digest, String.valueOf(courseId), String.valueOf(userId)};
        Cursor c = db.query(TRACKER_LOG_TABLE, null, s, args, null, null, null);
        boolean isCompleted = (c.getCount() > 0);
        c.close();
        return isCompleted;
    }


    public Activity getActivityByDigest(String digest) {
        String sql = "SELECT * FROM  " + ACTIVITY_TABLE + " a " +
                STR_WHERE + ACTIVITY_C_ACTIVITYDIGEST + "='" + digest + "'";
        Cursor c = db.rawQuery(sql, null);

        if (c.getCount() <= 0) {
            c.close();
            return null;
        }
        c.moveToFirst();
        Activity a = new Activity();
        while (!c.isAfterLast()) {
            if (c.getString(c.getColumnIndex(ACTIVITY_C_TITLE)) != null) {
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


    /*
     * SEARCH Functions
     *
     */

    public void searchIndexRemoveCourse(long courseId) {
        List<Activity> activities = this.getCourseActivities(courseId);
        Log.d(TAG, "deleting course from index: " + courseId);
        for (Activity a : activities) {
            this.deleteSearchRow(a.getDbId());
        }
    }

    public void insertActivityIntoSearchTable(String courseTitle, String sectionTitle, String activityTitle, int activityDbId, String fullText) {
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
    public List<SearchResult> search(String searchText, int limit, long userId, Context ctx) {
        ArrayList<SearchResult> results = new ArrayList<>();
        String sqlSeachFullText = String.format("SELECT c.%s AS courseid, a.%s as activitydigest, a.%s as sectionid, 1 AS ranking FROM %s ft " +
                        STR_INNERJOIN_FULLTEXT +
                        STR_INNERJOIN_COURSE +
                        STR_WHERE_MATCH,
                COURSE_C_ID, ACTIVITY_C_ACTIVITYDIGEST, ACTIVITY_C_SECTIONID, SEARCH_TABLE,
                ACTIVITY_TABLE, ACTIVITY_C_ID,
                COURSE_TABLE, ACTIVITY_C_COURSEID, COURSE_C_ID,
                SEARCH_C_TEXT, searchText);
        String sqlActivityTitle = String.format("SELECT c.%s AS courseid, a.%s as activitydigest, a.%s as sectionid, 5 AS ranking FROM %s ft " +
                        STR_INNERJOIN_FULLTEXT +
                        STR_INNERJOIN_COURSE +
                        STR_WHERE_MATCH,
                COURSE_C_ID, ACTIVITY_C_ACTIVITYDIGEST, ACTIVITY_C_SECTIONID, SEARCH_TABLE,
                ACTIVITY_TABLE, ACTIVITY_C_ID,
                COURSE_TABLE, ACTIVITY_C_COURSEID, COURSE_C_ID,
                SEARCH_C_ACTIVITYTITLE, searchText);

        String sqlSectionTitle = String.format("SELECT c.%s AS courseid, a.%s as activitydigest, a.%s as sectionid, 10 AS ranking FROM %s ft " +
                        STR_INNERJOIN_FULLTEXT +
                        STR_INNERJOIN_COURSE +
                        STR_WHERE_MATCH,
                COURSE_C_ID, ACTIVITY_C_ACTIVITYDIGEST, ACTIVITY_C_SECTIONID, SEARCH_TABLE,
                ACTIVITY_TABLE, ACTIVITY_C_ID,
                COURSE_TABLE, ACTIVITY_C_COURSEID, COURSE_C_ID,
                SEARCH_C_SECTIONTITLE, searchText);
        String sqlCourseTitle = String.format("SELECT c.%s AS courseid, a.%s as activitydigest, a.%s as sectionid, 15 AS ranking FROM %s ft " +
                        STR_INNERJOIN_FULLTEXT +
                        STR_INNERJOIN_COURSE +
                        STR_WHERE_MATCH,
                COURSE_C_ID, ACTIVITY_C_ACTIVITYDIGEST, ACTIVITY_C_SECTIONID, SEARCH_TABLE,
                ACTIVITY_TABLE, ACTIVITY_C_ID,
                COURSE_TABLE, ACTIVITY_C_COURSEID, COURSE_C_ID,
                SEARCH_C_COURSETITLE, searchText);

        String sql = String.format("SELECT DISTINCT courseid, activitydigest FROM ( SELECT * FROM (" +
                        "%s UNION %s UNION %s UNION %s) ORDER BY ranking DESC LIMIT 0,%d)",
                sqlSeachFullText, sqlActivityTitle, sqlSectionTitle, sqlCourseTitle, limit);

        Cursor c = db.rawQuery(sql, null);
        if (c != null && c.getCount() > 0) {

            long startTime = System.currentTimeMillis();
            Map<Long, Course> fetchedCourses = new HashMap<>();
            Map<Long, CompleteCourse> fetchedXMLCourses = new HashMap<>();

            c.moveToFirst();
            while (!c.isAfterLast()) {
                SearchResult result = new SearchResult();

                long courseId = c.getLong(c.getColumnIndex("courseid"));
                Course course = fetchedCourses.get(courseId);
                if (course == null) {
                    course = this.getCourse(courseId, userId);
                    fetchedCourses.put(courseId, course);
                }
                result.setCourse(course);

                int digest = c.getColumnIndex("activitydigest");
                Activity activity = this.getActivityByDigest(c.getString(digest));
                result.setActivity(activity);

                int sectionOrderId = activity.getSectionId();
                CompleteCourse parsed = fetchedXMLCourses.get(courseId);
                if (parsed == null) {
                    try {
                        CourseXMLReader cxr = new CourseXMLReader(course.getCourseXMLLocation(), course.getCourseId(), ctx);
                        cxr.parse(CourseXMLReader.ParseMode.COMPLETE);
                        parsed = cxr.getParsedCourse();
                        fetchedXMLCourses.put(courseId, parsed);
                        result.setSection(parsed.getSection(sectionOrderId));
                        result.setActivity(parsed.getSection(sectionOrderId).getActivity(activity.getDigest()));
                        results.add(result);
                    } catch (InvalidXMLException ixmle) {
                        Log.d(TAG, "Invalid course xml file", ixmle);
                        Mint.logException(ixmle);
                    }
                } else {
                    result.setSection(parsed.getSection(sectionOrderId));
                    results.add(result);
                }

                c.moveToNext();
            }
            long ellapsedTime = System.currentTimeMillis() - startTime;
            Log.d(TAG, "Performing search query and fetching. " + ellapsedTime + " ms ellapsed");
        }
        if (c != null) {
            c.close();
        }
        return results;

    }

    /*
     * Delete the entire search index
     */
    public void deleteSearchIndex() {
        db.execSQL("DELETE FROM " + SEARCH_TABLE);
        Log.d(TAG, "Deleted search index...");
    }

    /*
     * Delete a particular activity from the search index
     */
    public void deleteSearchRow(int activityDbId) {
        String s = "docid=?";
        String[] args = new String[]{String.valueOf(activityDbId)};
        db.delete(SEARCH_TABLE, s, args);
    }

    /*
     *
     */
    public boolean isPreviousSectionActivitiesCompleted(Activity activity, long userId) {
        // get this activity

        Log.d(TAG, "this digest = " + activity.getDigest());
        Log.d(TAG, "this actid = " + activity.getActId());
        Log.d(TAG, "this courseid = " + activity.getCourseId());
        Log.d(TAG, "this sectionid = " + activity.getSectionId());
        // get all the previous activities in this section
        String sql = String.format("SELECT * FROM " + ACTIVITY_TABLE +
                STR_WHERE + ACTIVITY_C_ACTID + " < %d " +
                " AND " + ACTIVITY_C_COURSEID + STR_EQUALS_NUMBER +
                " AND " + ACTIVITY_C_SECTIONID + STR_EQUALS_NUMBER, activity.getActId(), activity.getCourseId(), activity.getSectionId());

        Log.d(TAG, "sql: " + sql);
        Cursor c = db.rawQuery(sql, null);
        boolean completed = true;
        if (c.getCount() > 0) {
            c.moveToFirst();
            // check if each activity has been completed or not
            while (!c.isAfterLast()) {
                String sqlCheck = String.format("SELECT * FROM " + TRACKER_LOG_TABLE +
                        STR_WHERE + TRACKER_LOG_C_ACTIVITYDIGEST + " = '%s'" +
                        " AND " + TRACKER_LOG_C_COMPLETED + " =1" +
                        " AND " + TRACKER_LOG_C_USERID + STR_EQUALS_NUMBER, c.getString(c.getColumnIndex(ACTIVITY_C_ACTIVITYDIGEST)), userId);
                Cursor c2 = db.rawQuery(sqlCheck, null);
                if (c2 == null || c2.getCount() == 0) {
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
    public boolean isPreviousCourseActivitiesCompleted(Activity activity, long userId) {

        Log.d(TAG, "this digest = " + activity.getDigest());
        Log.d(TAG, "this actid = " + activity.getActId());
        Log.d(TAG, "this courseid = " + activity.getCourseId());
        Log.d(TAG, "this sectionid = " + activity.getSectionId());
        // get all the previous activities in this section
        String sql = String.format("SELECT * FROM " + ACTIVITY_TABLE +
                " WHERE (" + ACTIVITY_C_COURSEID + STR_EQUALS_NUMBER +
                " AND " + ACTIVITY_C_SECTIONID + " < %d )" +
                " OR (" + ACTIVITY_C_ACTID + " < %d " +
                " AND " + ACTIVITY_C_COURSEID + STR_EQUALS_NUMBER +
                " AND " + ACTIVITY_C_SECTIONID + " = %d)", activity.getCourseId(), activity.getSectionId(), activity.getActId(), activity.getCourseId(), activity.getSectionId());

        Log.d(TAG, "sql: " + sql);
        Cursor c = db.rawQuery(sql, null);
        boolean completed = true;
        if (c.getCount() > 0) {
            c.moveToFirst();
            // check if each activity has been completed or not
            while (!c.isAfterLast()) {
                String sqlCheck = String.format("SELECT * FROM " + TRACKER_LOG_TABLE +
                        STR_WHERE + TRACKER_LOG_C_ACTIVITYDIGEST + " = '%s'" +
                        " AND " + TRACKER_LOG_C_COMPLETED + " =1" +
                        " AND " + TRACKER_LOG_C_USERID + STR_EQUALS_NUMBER, c.getString(c.getColumnIndex(ACTIVITY_C_ACTIVITYDIGEST)), userId);
                Cursor c2 = db.rawQuery(sqlCheck, null);
                if (c2 == null || c2.getCount() == 0) {
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


    public void clearCustomFieldsAndCollections(){
        db.delete(CUSTOM_FIELD_TABLE, null, null);
        db.delete(CUSTOM_FIELDS_COLLECTION_TABLE, null, null);
    }

    public void insertOrUpdateCustomField(CustomField field) {

        if (TextUtils.isEmpty(field.getKey()))
            return;

        ContentValues values = new ContentValues();
        values.put(CUSTOM_FIELD_C_KEY, field.getKey());
        values.put(CUSTOM_FIELD_C_TYPE, field.getType());
        values.put(CUSTOM_FIELD_C_HELPTEXT, field.getHelperText());
        values.put(CUSTOM_FIELD_C_LABEL, field.getLabel());
        values.put(CUSTOM_FIELD_C_REQUIRED, field.isRequired()?1:0);
        values.put(CUSTOM_FIELD_C_ORDER, field.getOrder());
        values.put(CUSTOM_FIELD_C_COLLECTION, field.getCollectionName());
        values.put(CUSTOM_FIELD_C_VISIBLE_BY, field.getFieldVisibleBy());
        values.put(CUSTOM_FIELD_C_VISIBLE_VALUE, field.getValueVisibleBy());
        values.put(CUSTOM_FIELD_C_COLLECTION_BY, field.getCollectionNameBy());

        String s = CUSTOM_FIELD_C_KEY + "=?";
        String[] args = new String[]{ field.getKey() };
        Cursor c = db.query(CUSTOM_FIELD_TABLE, null, s, args, null, null, null);
        boolean toUpdate = c.getCount() > 0;
        c.close();

        if (toUpdate){
            db.update(CUSTOM_FIELD_TABLE, values, s, args);
        }
        else{
            db.insertOrThrow(CUSTOM_FIELD_TABLE, null, values);
        }

    }

    public void insertOrUpdateCustomFieldCollection(String collectionName, List<CustomField.CollectionItem> items) {
        for (CustomField.CollectionItem item : items){
            ContentValues values = new ContentValues();
            values.put(CUSTOM_FIELDS_COLLECTION_C_COLLECTION_ID, collectionName);
            values.put(CUSTOM_FIELDS_COLLECTION_C_ITEM_KEY, item.getKey());
            values.put(CUSTOM_FIELDS_COLLECTION_C_ITEM_VALUE, item.getLabel());

            String s = CUSTOM_FIELDS_COLLECTION_C_COLLECTION_ID + STR_EQUALS_AND + CUSTOM_FIELDS_COLLECTION_C_ITEM_KEY + "=?";
            String[] args = new String[]{ collectionName, item.getKey() };
            Cursor c = db.query(CUSTOM_FIELDS_COLLECTION_TABLE, null, s, args, null, null, null);
            boolean toUpdate = c.getCount() > 0;
            c.close();

            if (toUpdate){
                db.update(CUSTOM_FIELDS_COLLECTION_TABLE, values, s, args);
            }
            else{
                db.insertOrThrow(CUSTOM_FIELDS_COLLECTION_TABLE, null, values);
            }
        }

    }

    public List<CustomField.CollectionItem> getCollection(String collectionName){
        String s = CUSTOM_FIELDS_COLLECTION_C_COLLECTION_ID + "=?";
        String[] args = new String[]{ collectionName };
        Cursor c = db.query(CUSTOM_FIELDS_COLLECTION_TABLE, null, s, args, null, null, null);
        c.moveToFirst();
        List<CustomField.CollectionItem> items = new ArrayList<>();
        while (!c.isAfterLast()) {
            String key = c.getString(c.getColumnIndex(CUSTOM_FIELDS_COLLECTION_C_ITEM_KEY));
            String value = c.getString(c.getColumnIndex(CUSTOM_FIELDS_COLLECTION_C_ITEM_VALUE));
            items.add( new CustomField.CollectionItem(key, value));
            c.moveToNext();
        }
        c.close();

        return items;
    }

    public List<CustomField> getCustomFields(){
        Cursor c = db.query(CUSTOM_FIELD_TABLE, null, null, null, null, null, CUSTOM_FIELD_C_ORDER);
        c.moveToFirst();

        ArrayList<CustomField> fields = new ArrayList<>();
        while (!c.isAfterLast()) {
            CustomField field = new CustomField();
            field.setKey(c.getString(c.getColumnIndex(CUSTOM_FIELD_C_KEY)));
            field.setType(c.getString(c.getColumnIndex(CUSTOM_FIELD_C_TYPE)));
            field.setRequired(c.getInt(c.getColumnIndex(CUSTOM_FIELD_C_REQUIRED))==1);
            field.setOrder(c.getInt(c.getColumnIndex(CUSTOM_FIELD_C_ORDER)));
            field.setLabel(c.getString(c.getColumnIndex(CUSTOM_FIELD_C_LABEL)));
            field.setHelperText(c.getString(c.getColumnIndex(CUSTOM_FIELD_C_HELPTEXT)));
            field.setCollectionName(c.getString(c.getColumnIndex(CUSTOM_FIELD_C_COLLECTION)));
            field.setFieldVisibleBy(c.getString(c.getColumnIndex(CUSTOM_FIELD_C_VISIBLE_BY)));
            field.setValueVisibleBy(c.getString(c.getColumnIndex(CUSTOM_FIELD_C_VISIBLE_VALUE)));
            field.setCollectionNameBy(c.getString(c.getColumnIndex(CUSTOM_FIELD_C_COLLECTION_BY)));

            fields.add(field);
            c.moveToNext();
        }
        c.close();

        for (CustomField field : fields){
            if (field.isChoices() && !TextUtils.isEmpty(field.getCollectionName())){
                field.setCollection(getCollection(field.getCollectionName()));
            }
        }

        return fields;
    }

    /* Methods SQLiteDatabase free. When all previous ones are migrated, DbHelper class can be used only for
        common methods to access Room Database and remove the inheritance of SQLiteOpenHelper and all db logic
     */

    public boolean insertOrUpdateUserLeaderboard(String username, String fullname, int points, DateTime lastUpdate) {

        if ((username == null) || ("".equals(username)))
            return false;

        boolean updated = false;
        Leaderboard leaderboard = App.getDb().leaderboardDao().getLeaderboard(username);
        if (leaderboard == null) {
            // Insert
            Leaderboard leaderboardItem = new Leaderboard(username, fullname, points, lastUpdate);
            App.getDb().leaderboardDao().insert(leaderboardItem);
        } else {

            if (leaderboard.getLastupdate().isBefore(lastUpdate)) {
                leaderboard.setFullname(fullname);
                leaderboard.setPoints(points);
                leaderboard.setLastupdate(lastUpdate);
                int rowsUpdated = App.getDb().leaderboardDao().update(leaderboard);
                updated = rowsUpdated == 1;
            }

        }

        return updated;

    }


    // METHODS FOR DB ROOM DATA MIGRATION

    public static final String LEADERBOARD_TABLE = "leaderboard";
    public static final String USER_PREFS_TABLE = "userprefs";

    public void dropTable(String table) {
        db.execSQL(STR_DROP_IF_EXISTS + table);
    }


    public List<UserPreference> getAllUserPreferences() {

        String userPrefsCPrefkey = "preference";
        String userPrefsCPrefvalue = "value";

        List<UserPreference> userPreferences = new ArrayList<>();
        Cursor c = db.query(USER_PREFS_TABLE, null, null, null, null, null, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {

            String username = c.getString(c.getColumnIndex(USER_C_USERNAME));
            String prefKey = c.getString(c.getColumnIndex(userPrefsCPrefkey));
            String prefValue = c.getString(c.getColumnIndex(userPrefsCPrefvalue));
            userPreferences.add(new UserPreference(username, prefKey, prefValue));

            c.moveToNext();
        }
        c.close();

        return userPreferences;
    }

    public List<Leaderboard> getLeaderboardList() {

        String leaderboardCFullname = "fullname";
        String leaderboardCPoints = "points";
        String leaderboardCLastupdate = "lastupdate";

        ArrayList<Leaderboard> leaderboard = new ArrayList<>();
        Cursor c = db.query(LEADERBOARD_TABLE, null, null, null, null, null, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            Leaderboard leaderboardItem = new Leaderboard();
            leaderboardItem.setUsername(c.getString(c.getColumnIndex(USER_C_USERNAME)));
            leaderboardItem.setFullname(c.getString(c.getColumnIndex(leaderboardCFullname)));
            leaderboardItem.setPoints(c.getInt(c.getColumnIndex(leaderboardCPoints)));
            leaderboardItem.setLastupdateStr(c.getString(c.getColumnIndex(leaderboardCLastupdate)));
            leaderboard.add(leaderboardItem);
            c.moveToNext();
        }

        c.close();
        return leaderboard;
    }

    public List<UserCustomField> getUserCustomFields() {

        ArrayList<UserCustomField> userCustomFields = new ArrayList<>();
        Cursor c = db.query(USER_CF_TABLE, null, null, null, null, null, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            UserCustomField userCustomField = new UserCustomField();
            userCustomField.setUsername(c.getString(c.getColumnIndex(USER_C_USERNAME)));
            userCustomField.setFieldKey(c.getString(c.getColumnIndex(CUSTOM_FIELD_C_KEY)));
            userCustomField.setValueStr(c.getString(c.getColumnIndex(CF_VALUE_STR)));

            String valueIntStr = c.getString(c.getColumnIndex(CF_VALUE_INT));
            if (valueIntStr != null) {
                userCustomField.setValueInt(Integer.parseInt(valueIntStr));
            }

            String valueBoolStr = c.getString(c.getColumnIndex(CF_VALUE_BOOL));
            if (valueBoolStr != null) {
                userCustomField.setValueBool(Integer.parseInt(valueBoolStr) == 1);
            }

            String valueFloatStr = c.getString(c.getColumnIndex(CF_VALUE_FLOAT));
            if (valueFloatStr != null) {
                userCustomField.setValueFloat(Float.parseFloat(valueFloatStr));
            }

            userCustomFields.add(userCustomField);
            c.moveToNext();
        }

        c.close();
        return userCustomFields;

    }

}
