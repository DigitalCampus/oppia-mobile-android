package androidTestFiles;

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

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import androidTestFiles.Utils.FileUtils;
import androidTestFiles.database.BaseTestDB;
import androidTestFiles.org.digitalcampus.oppia.api.MockApiEndpoint;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;


@RunWith(AndroidJUnit4.class)
public class LoginTest extends BaseTestDB {

    // adb  shell pm grant org.digitalcampus.mobile.learning android.permission.SET_ANIMATION_SCALE
    // https://product.reverb.com/disabling-animations-in-espresso-for-android-testing-de17f7cf236f



    private CountDownLatch signal;
    private MockWebServer mockServer;
    private Context context;
    private EntityResult<User> resultUser;
    private boolean registerOK;

   /* @Rule
    public ActivityTestRule<WelcomeActivity> welcomeActivityTestRule =
            new ActivityTestRule<WelcomeActivity>(WelcomeActivity.class);*/

    @Before
    public void setUp() throws Exception {
        super.setUp();
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        signal = new CountDownLatch(1);
        mockServer = new MockWebServer();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        signal.countDown();
        mockServer.shutdown();
    }

    @Test
    public void userLogin_EmptyResponse()throws Exception {
        try {

            mockServer.enqueue(new MockResponse()
                    .setBody(""));

            mockServer.start();

        }catch(IOException ioe) {
            ioe.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }

        User user = new User();
        user.setUsername("");
        user.setPassword("");

        try {
            LoginTask task = new LoginTask(context, new MockApiEndpoint(mockServer));
            task.setLoginListener(new SubmitEntityListener<User>() {
                @Override
                public void apiKeyInvalidated() {  }

                @Override
                public void submitComplete(EntityResult<User> result) {
                    resultUser = result;
                    signal.countDown();
                }
            });
            task.execute(user);

            signal.await();

            assertFalse(resultUser.isSuccess());
            assertEquals(context.getString(R.string.error_processing_response), resultUser.getResultMessage());

        }catch (InterruptedException ie) {
            ie.printStackTrace();
        }catch(Exception e){}

    }

    @Test
    public void userLogin_OKResponse()throws Exception {
        try {

            String filename = "responses/response_201_login.json";

            mockServer.enqueue(new MockResponse()
                    .setResponseCode(201)
                    .setBody(FileUtils.getStringFromFile(InstrumentationRegistry.getInstrumentation().getContext(), filename)));

            mockServer.start();


        }catch(IOException ioe) {
            ioe.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }

        User user = new User();
        user.setUsername("");
        user.setPassword("");

        try {
            LoginTask task = new LoginTask(context, new MockApiEndpoint(mockServer));
            task.setLoginListener(new SubmitEntityListener<User>() {
                @Override
                public void apiKeyInvalidated() { }

                @Override
                public void submitComplete(EntityResult<User> result) {
                    resultUser = result;
                    signal.countDown();
                }
            });
            task.execute(user);

            signal.await();

            assertTrue(resultUser.isSuccess());
            assertEquals(context.getString(R.string.login_complete), resultUser.getResultMessage());

        }catch (InterruptedException ie) {
            ie.printStackTrace();
        }catch(Exception e){}

    }

    @Test
    public void userLogin_WrongPassword()throws Exception {
        try {

            String filename = "responses/response_400_login.json";

            mockServer.enqueue(new MockResponse()
                    .setResponseCode(400)
                    .setBody(FileUtils.getStringFromFile(InstrumentationRegistry.getInstrumentation().getContext(), filename)));

            mockServer.start();

        }catch(IOException ioe) {
            ioe.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }

        User user = new User();
        user.setUsername("");
        user.setPassword("");

        try {
            LoginTask task = new LoginTask(context, new MockApiEndpoint(mockServer));
            task.setLoginListener(new SubmitEntityListener<User>() {
                @Override
                public void apiKeyInvalidated() { }

                @Override
                public void submitComplete(EntityResult<User> result) {
                    resultUser = result;
                    signal.countDown();
                }
            });
            task.execute(user);

            signal.await();

            assertFalse(resultUser.isSuccess());
            assertEquals(context.getString(R.string.error_login), resultUser.getResultMessage());

        }catch (InterruptedException ie) {
            ie.printStackTrace();
        }catch(Exception e){}

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
}
