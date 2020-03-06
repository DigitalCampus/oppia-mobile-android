import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.model.CustomValue;
import org.digitalcampus.oppia.model.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;


@RunWith(AndroidJUnit4.class)
public class DBTest {

//    @Inject
//    private MockContext mockContext;
    private Context context;

    @Before
    public void setUp() throws Exception { 
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    }

    @Test
    public void storeAndRetrieveUserCustomFields() {

        DbHelper dbHelper = DbHelper.getInstance(context);

        dbHelper.getReadableDatabase(); // To force migration if needed

        User user = new User();
        user.setUsername("esprueba2");
        user.setPassword("aaabbb");
        user.setEmail("xxx2@yyy.zzz");
        user.setCounty("spain");
        user.setDistrict("alcala");

        dbHelper.addOrUpdateUser(user);

        try {
            User user1 = dbHelper.getUser(user.getUsername());
            assertEquals(user1.getCounty(), "spain");
            assertEquals(user1.getDistrict(), "alcala");
        } catch (UserNotFoundException e) {
            e.printStackTrace();
        }

    }
}
