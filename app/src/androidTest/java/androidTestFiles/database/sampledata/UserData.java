package androidTestFiles.database.sampledata;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.User;

public class UserData {

    public final static String TEST_USER_1 = "demouser1";
//    public final static String TEST_USER_2 = "demouser2";

    public static void loadData(Context context){

        DbHelper db = DbHelper.getInstance(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        User u1 = new User();
        u1.setUsername(TEST_USER_1);
        u1.setPasswordEncrypted("password");
        db.addOrUpdateUser(u1);

        prefs.edit().putString(PrefsActivity.PREF_USER_NAME, TEST_USER_1).commit();

//        User u2 = new User();
//        u2.setUsername(TEST_USER_2);
//        u2.setPasswordEncrypted("password");
//        db.addOrUpdateUser(u2);
    }

    public static void deleteData(Context context) {

        DbHelper db = DbHelper.getInstance(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        db.deleteUser(TEST_USER_1);

        prefs.edit().remove(PrefsActivity.PREF_USER_NAME).commit();

    }
}
