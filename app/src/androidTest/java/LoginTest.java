import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.api.MockApiEndpoint;
import org.digitalcampus.oppia.listener.SubmitListener;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.LoginTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.RegisterTask;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import database.BaseTestDB;
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
    private Payload response;
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

        ArrayList<Object> users = new ArrayList<>();
        User u = new User();
        u.setUsername("");
        u.setPassword("");
        users.add(u);

        Payload p = new Payload(users);
        try {
            LoginTask task = new LoginTask(context, new MockApiEndpoint(mockServer));
            task.setLoginListener(new SubmitListener() {
                @Override
                public void apiKeyInvalidated() {  }

                @Override
                public void submitComplete(Payload r) {
                    response = r;
                    signal.countDown();
                }
            });
            task.execute(p);

            signal.await();

            assertFalse(response.isResult());
            assertEquals(context.getString(R.string.error_processing_response), response.getResultResponse());

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
                    .setBody(Utils.FileUtils.getStringFromFile(InstrumentationRegistry.getInstrumentation().getContext(), filename)));

            mockServer.start();


        }catch(IOException ioe) {
            ioe.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }

        ArrayList<Object> users = new ArrayList<>();
        User u = new User();
        u.setUsername("");
        u.setPassword("");
        users.add(u);

        Payload p = new Payload(users);
        try {
            LoginTask task = new LoginTask(context, new MockApiEndpoint(mockServer));
            task.setLoginListener(new SubmitListener() {
                @Override
                public void apiKeyInvalidated() { }

                @Override
                public void submitComplete(Payload r) {
                    response = r;
                    signal.countDown();
                }
            });
            task.execute(p);

            signal.await();

            assertTrue(response.isResult());
            assertEquals(context.getString(R.string.login_complete), response.getResultResponse());

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
                    .setBody(Utils.FileUtils.getStringFromFile(InstrumentationRegistry.getInstrumentation().getContext(), filename)));

            mockServer.start();

        }catch(IOException ioe) {
            ioe.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }

        ArrayList<Object> users = new ArrayList<>();
        User u = new User();
        u.setUsername("");
        u.setPassword("");
        users.add(u);

        Payload p = new Payload(users);
        try {
            LoginTask task = new LoginTask(context, new MockApiEndpoint(mockServer));
            task.setLoginListener(new SubmitListener() {
                @Override
                public void apiKeyInvalidated() { }

                @Override
                public void submitComplete(Payload r) {
                    response = r;
                    signal.countDown();
                }
            });
            task.execute(p);

            signal.await();

            assertFalse(response.isResult());
            assertEquals(context.getString(R.string.error_login), response.getResultResponse());

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

    private void performRegister(User u) {

        Payload p = new Payload(Arrays.asList(u));
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
        rt.execute(p);
    }
}
