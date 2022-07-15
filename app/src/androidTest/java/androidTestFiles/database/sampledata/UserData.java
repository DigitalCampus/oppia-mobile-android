package androidTestFiles.database.sampledata;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.User;

import java.util.Arrays;

public class UserData {

    public final static String TEST_USER_1 = "demouser1";
    public final static String TEST_USER_2 = "demouser2";
    public final static String TEST_USER_3 = "demouser3";

    public static void loadData(Context context){

        DbHelper db = DbHelper.getInstance(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        User u1 = new User();
        u1.setUsername(TEST_USER_1);
        u1.setPasswordEncrypted("password");
        u1.setCohorts(Arrays.asList(1, 2));
        db.addOrUpdateUser(u1);

        User u2 = new User();
        u2.setUsername(TEST_USER_2);
        u2.setPasswordEncrypted("password");
        u2.setCohorts(Arrays.asList(2, 3));
        db.addOrUpdateUser(u2);

        User u3 = new User();
        u3.setUsername(TEST_USER_3);
        u3.setPasswordEncrypted("password");
        u3.setCohorts(Arrays.asList(3));
        db.addOrUpdateUser(u3);

        prefs.edit().putString(PrefsActivity.PREF_USER_NAME, TEST_USER_1).commit();
    }

    public static void deleteData(Context context) {

        DbHelper db = DbHelper.getInstance(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        db.deleteUser(TEST_USER_1);
        db.deleteUser(TEST_USER_2);
        db.deleteUser(TEST_USER_3);

        prefs.edit().remove(PrefsActivity.PREF_USER_NAME).commit();

    }
}
