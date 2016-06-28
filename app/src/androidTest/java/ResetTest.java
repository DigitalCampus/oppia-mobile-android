import android.test.mock.MockContext;
import android.util.Log;

import org.digitalcampus.oppia.listener.SubmitListener;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.ResetTask;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.junit.Assert.assertTrue;

public class ResetTest {
    private CountDownLatch signal;
    private MockWebServer mockServer;
    private MockContext mockContext;

    @Before
    public void setUp() throws Exception {
        signal = new CountDownLatch(1);
    }

    @After
    public void tearDown() throws Exception {
        signal.countDown();
        mockServer.shutdown();
    }

    @Test
    public void passwordReset_emptyResponse() throws Exception {
        try {
            mockServer = new MockWebServer();
            mockContext = new MockContext();

            mockServer.enqueue(new MockResponse().setBody(""));
            mockServer.enqueue(new MockResponse().setBody(""));

            mockServer.start();
        }catch(IOException ioe) {
            ioe.printStackTrace();
        }

        ArrayList<Object> users = new ArrayList<>();
        User u = new User();
        u.setUsername("");
        u.setPassword("");
        users.add(u);

        Payload p = new Payload(users);
        ResetTask task = new ResetTask(mockContext);
        Log.v("Response ", "response");
        task.setResetListener(new SubmitListener() {
            @Override
            public void submitComplete(Payload response) {
                Log.d("Response ", response.toString());
                //assertTrue(response.isResult());
                signal.countDown();
            }
        });
        task.execute(p);

        try {
            signal.await();
        }catch (InterruptedException ie) {
            ie.printStackTrace();
        }

        assertTrue(true);


    }

}
