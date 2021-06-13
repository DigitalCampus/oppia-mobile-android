package androidTestFiles.database.sampledata;

import android.content.Context;

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.User;

public class UserData {

    public final static String TEST_USER_1 = "demouser1";
    public final static String TEST_USER_2 = "demouser2";

    public static void loadData(Context ctx){

        DbHelper db = DbHelper.getInstance(ctx);

        User u1 = new User();
        u1.setUsername(TEST_USER_1);
        u1.setPasswordEncrypted("password");
        db.addOrUpdateUser(u1);

        User u2 = new User();
        u2.setUsername(TEST_USER_2);
        u2.setPasswordEncrypted("password");
        db.addOrUpdateUser(u2);
    }
}
