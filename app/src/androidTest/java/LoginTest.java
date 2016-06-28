import android.app.Application;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.AndroidTestCase;
import android.test.InstrumentationTestCase;
import android.test.InstrumentationTestRunner;
import android.test.mock.MockContext;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.api.MockApiEndpoint;
import org.digitalcampus.oppia.listener.SubmitListener;
import org.digitalcampus.oppia.listener.TaskCompleteListener;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.LoginTask;
import org.digitalcampus.oppia.task.Payload;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

/**
 * Created by Alberto on 23/06/2016.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class LoginTest extends InstrumentationTestCase {
    private CountDownLatch signal;
    private MockWebServer mockServer;
    private Context context;
    Payload response;


    @Before
    public void setUp() throws Exception {
        super.setUp();
        context = InstrumentationRegistry.getTargetContext();
        signal = new CountDownLatch(1);
    }

    @After
    public void tearDown() throws Exception {
        if (signal != null){
            signal.countDown();
        }
        //mockServer.shutdown();
    }

    @Test
    public void userLogin_emptyResponseTest() {
        try {
            mockServer = new MockWebServer();

            mockServer.enqueue(new MockResponse().setResponseCode(200));

            mockServer.start();

        }catch(IOException ioe) {
            ioe.printStackTrace();
        }

        ArrayList<Object> users = new ArrayList<>();
        User u = new User();
        u.setUsername("asdas");
        u.setPassword("asdasd");
        users.add(u);

        Payload p = new Payload(users);
        try {
            LoginTask task = new LoginTask(context, new MockApiEndpoint(mockServer));
            task.setLoginListener(new SubmitListener() {
                @Override
                public void submitComplete(Payload r) {
                    response = r;
                    signal.countDown();

                }
            });
            task.execute(p);

            try {
                signal.await();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }


            assertTrue(response.isResult());
        }catch(Exception e){}

    }

}
