import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import junit.framework.TestCase;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.digitalcampus.oppia.api.MockApiEndpoint;
import org.digitalcampus.oppia.listener.SubmitListener;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.LoginTask;
import org.digitalcampus.oppia.task.Payload;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import TestRules.DisableAnimationsRule;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class LoginTest{

    @Rule
    public DisableAnimationsRule disableAnimationsRule = new DisableAnimationsRule();


    private CountDownLatch signal;
    private MockWebServer mockServer;
    private Context context;
    private Payload response;

   /* @Rule
    public ActivityTestRule<WelcomeActivity> welcomeActivityTestRule =
            new ActivityTestRule<WelcomeActivity>(WelcomeActivity.class);*/

    @Before
    public void setUp() throws Exception { 
        context = InstrumentationRegistry.getTargetContext();
        //DbHelper.getInstance(context).resetDatabase();
        signal = new CountDownLatch(1);
    }

    @After
    public void tearDown() throws Exception {
        signal.countDown();
        mockServer.shutdown();
    }

    @Test
    public void userLogin_EmptyResponse()throws Exception {
        try {
            mockServer = new MockWebServer();

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
            mockServer = new MockWebServer();

            String filename = "responses/response_201_login.json";

            mockServer.enqueue(new MockResponse()
                    .setResponseCode(201)
                    .setBody(Utils.FileUtils.getStringFromFile(InstrumentationRegistry.getContext(), filename)));

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
            mockServer = new MockWebServer();

            String filename = "responses/response_400_login.json";

            mockServer.enqueue(new MockResponse()
                    .setResponseCode(400)
                    .setBody(Utils.FileUtils.getStringFromFile(InstrumentationRegistry.getContext(), filename)));

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

}
