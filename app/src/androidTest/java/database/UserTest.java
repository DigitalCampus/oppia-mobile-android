package database;

import android.content.Context;
import android.test.mock.MockContext;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.model.db_model.UserPreference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;


@RunWith(AndroidJUnit4.class)
public class UserTest extends BaseDBTests {


    @Test
    public void storeAndRetrieveUser() {

        User user = new User();
        user.setUsername("esprueba2");
        user.setPassword("aaabbb");
        user.setEmail("xxx2@yyy.zzz");
        user.setFirstname("myfirstname");
        user.setLastname("mylastname");

        getDbHelper().addOrUpdateUser(user);

        try {
            User user1 = getDbHelper().getUser(user.getUsername());
            assertEquals("esprueba2",user1.getUsername());
            assertEquals("myfirstname", user1.getFirstname());
            assertEquals("mylastname", user1.getLastname());
            assertEquals("myfirstname mylastname", user1.getDisplayName());
        } catch (UserNotFoundException e) {
            e.printStackTrace();
        }

    }


    // TODO getAllUserPreferences
    // currently fails with table not found exception
    /*@Test
    public void getAllUserPreferences() {

        DbHelper dbHelper = DbHelper.getInstance(context);
        dbHelper.getReadableDatabase();

        ArrayList<UserPreference> userPrefs = (ArrayList<UserPreference>) dbHelper.getAllUserPreferences();

        assertEquals(0, userPrefs.size());
    }*/

    // TODO getOneRegisteredUser
    // TODO getAllUsers
    // TODO updateUserBadges - username
    // TODO updateUserBadges - userid
    // TODO getAllUserPreferences
    // TODO getUserCustomFields



}
