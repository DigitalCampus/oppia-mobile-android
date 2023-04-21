package androidTestFiles.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.ChangePasswordTask;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import androidTestFiles.database.TestDataManager;
import androidTestFiles.utils.MockApiEndpoint;
import androidTestFiles.utils.parent.MockedApiEndpointTaskTest;

@RunWith(AndroidJUnit4.class)
public class ChangePasswordTaskTest extends MockedApiEndpointTaskTest {
    public static final String ERROR_CHANGEPASSWORD_DIFFERENT = "responses/response_400_change_password.json";
    public static final String ORIGINAL_USER_PASSWORD = "password";

    private Context context;
    private DbHelper db;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        db = DbHelper.getInstance(context);
        TestDataManager testDataManager = new TestDataManager(db);
        testDataManager.addUsers();
        SessionManager.loginUser(context, db.getUser("user1"));
    }

    @After
    public void tearDown() throws Exception {
        if (mockServer!=null)
            mockServer.shutdown();
    }

    private void changePassword(String newPassword, String repeatPassword) {
        final CountDownLatch signal = new CountDownLatch(1);

        ChangePasswordTask task = new ChangePasswordTask(context, new MockApiEndpoint(mockServer));
        task.setResponseListener(new ChangePasswordTask.ResponseListener() {
            @Override
            public void onSuccess() {
                signal.countDown();
            }

            @Override
            public void onError(String error) {
                signal.countDown();
            }
        });

        task.execute(newPassword, repeatPassword);

        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String getPasswordEncrypted(String password) {
        User user = new User();
        user.setPassword(password);
        return user.getPasswordEncrypted();
    }

    @Test
    public void changePasswordIfSameRepeatedPassword() throws Exception {
        startServer(201, "");

        String newPassword = "newPassword";
        String repeatPassword = "newPassword";

        changePassword(newPassword, repeatPassword);

        User u = db.getUser(SessionManager.getUsername(context));
        assertEquals(getPasswordEncrypted(newPassword), u.getPasswordEncrypted());
        assertNotEquals(getPasswordEncrypted(ORIGINAL_USER_PASSWORD), u.getPasswordEncrypted());
    }

    @Test
    public void dontChangePasswordIfDifferentRepeatedPassword() throws Exception {
        startServer(400, ERROR_CHANGEPASSWORD_DIFFERENT);

        String newPassword = "newPassword";
        String repeatPassword = "differentPassword";

        changePassword(newPassword, repeatPassword);

        User u = db.getUser("user1");
        assertNotEquals(getPasswordEncrypted(newPassword), u.getPasswordEncrypted());
        assertEquals(getPasswordEncrypted(ORIGINAL_USER_PASSWORD), u.getPasswordEncrypted());
    }

}
