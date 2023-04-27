package androidTestFiles.features.authentication.login;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.listener.SubmitEntityListener;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.LoginTask;
import org.digitalcampus.oppia.task.RegisterTask;
import org.digitalcampus.oppia.task.result.EntityResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import androidTestFiles.database.TestDBHelper;
import androidTestFiles.utils.FileUtils;
import androidTestFiles.utils.MockApiEndpoint;
import androidTestFiles.utils.parent.BaseTest;
import androidTestFiles.utils.parent.MockedApiEndpointTaskTest;


@RunWith(AndroidJUnit4.class)
public class LoginTest extends MockedApiEndpointTaskTest {

    private CountDownLatch signal;
    private Context context;
    private EntityResult<User> resultUser;
    private boolean registerOK;
    private TestDBHelper testDBHelper;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        testDBHelper = new TestDBHelper(context);
        testDBHelper.setUp();
        signal = new CountDownLatch(1);
    }

    @After
    public void tearDown() throws Exception {

        testDBHelper.tearDown();
        enableConnectivity(true);

        if (mockServer != null) {
            mockServer.shutdown();
        }

    }

    @Test
    public void userLogin_EmptyResponse() throws Exception {
        startServer(200, "", 500);

        launchLoginTask();

        assertFalse(resultUser.isSuccess());
        assertEquals(context.getString(R.string.error_processing_response), resultUser.getResultMessage());

    }

    @Test
    public void userLogin_OKResponse() throws Exception {

        String filename = BaseTest.PATH_RESPONSES + "/response_201_login.json";
        String response = FileUtils.getStringFromFile(InstrumentationRegistry.getInstrumentation().getContext(), filename);
        startServer(201, response);


        launchLoginTask();

        assertTrue(resultUser.isSuccess());
        assertEquals(context.getString(R.string.login_complete), resultUser.getResultMessage());

    }

    @Test
    public void userLogin_WrongPassword() throws Exception {

        String filename = BaseTest.PATH_RESPONSES + "/response_400_login.json";
        String response = FileUtils.getStringFromFile(InstrumentationRegistry.getInstrumentation().getContext(), filename);
        startServer(400, response, 500);

        launchLoginTask();

        assertFalse(resultUser.isSuccess());
        assertEquals(context.getString(R.string.error_login), resultUser.getResultMessage());

    }

    @Test
    public void registerOfflineAvoidRegisterSameUsernameCaseInsensitive() throws Exception {

        String username1 = "test1";

        User u = new User();
        u.setUsername(username1);
        u.setPassword("testPass1");
        u.setOfflineRegister(true);

        performRegister(u);
        signal.await();
        assertTrue(registerOK);

        signal = new CountDownLatch(1);
        u.setUsername(username1.toUpperCase());
        performRegister(u);
        signal.await();
        assertFalse(registerOK);

    }

    private void performRegister(User user) {

        RegisterTask rt = new RegisterTask(context);
        rt.setRegisterListener(new RegisterTask.RegisterListener() {
            @Override
            public void onSubmitComplete(User u) {
                registerOK = true;
                signal.countDown();
            }

            @Override
            public void onSubmitError(String error) {
                registerOK = false;
                signal.countDown();

            }

            @Override
            public void onConnectionError(String error, User u) {
                registerOK = false;
                signal.countDown();

            }
        });
        rt.execute(user);
    }


    @Test
    public void offlineUserNotFound() throws Exception {

        startServer(0, "", 0, false);

        launchLoginTask();

        assertFalse(resultUser.isSuccess());
        assertEquals(context.getString(R.string.offline_user_not_found), resultUser.getResultMessage());

    }

    @Test
    public void offlineUserPasswordInvalid() throws Exception {

        testDBHelper.getTestDataManager().addUsers();

        startServer(0, "", 0, false);

        launchLoginTask();

        assertFalse(resultUser.isSuccess());
        assertEquals(context.getString(R.string.offline_user_invalid_password), resultUser.getResultMessage());

    }

    private void launchLoginTask() {

        User user = new User();
        user.setUsername("user1");
        user.setPassword("password1");

        try {
            LoginTask task = new LoginTask(context, new MockApiEndpoint(mockServer));
            task.setLoginListener(new SubmitEntityListener<User>() {
                @Override
                public void apiKeyInvalidated() {
                }

                @Override
                public void submitComplete(EntityResult<User> result) {
                    resultUser = result;
                    signal.countDown();
                }
            });
            task.execute(user);

            signal.await();

        } catch (InterruptedException ie) {
            ie.printStackTrace();
        } catch (Exception e) {
        }
    }

}
