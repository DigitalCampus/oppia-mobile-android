package database;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;


@RunWith(AndroidJUnit4.class)
public class UserTest extends BaseTestDB {


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

        ArrayList<UserPreference> userPrefs = (ArrayList<UserPreference>)
        getDbHelper().getAllUserPreferences();

        assertEquals(0, userPrefs.size());
    }*/

    // TODO getOneRegisteredUser
    // TODO getAllUsers
    // TODO updateUserBadges - username
    // TODO updateUserBadges - userid
    // TODO getAllUserPreferences
    // TODO getUserCustomFields



}
