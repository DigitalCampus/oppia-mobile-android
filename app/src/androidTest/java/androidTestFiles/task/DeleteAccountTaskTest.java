package androidTestFiles.task;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.task.DeleteAccountTask;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import androidTestFiles.database.TestDataManager;
import androidTestFiles.utils.MockApiEndpoint;
import androidTestFiles.utils.parent.MockedApiEndpointTaskTest;

@RunWith(AndroidJUnit4.class)
public class DeleteAccountTaskTest extends MockedApiEndpointTaskTest {

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

    private void deleteAccount(String userPassword) {
        final CountDownLatch signal = new CountDownLatch(1);

        DeleteAccountTask task = new DeleteAccountTask(context, new MockApiEndpoint(mockServer));
        task.setResponseListener(new DeleteAccountTask.ResponseListener() {
            @Override
            public void onSuccess() {
                signal.countDown();
            }

            @Override
            public void onError(String error) {
                signal.countDown();
            }
        });

        task.execute(userPassword);

        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void deleteAccount_correctPassword_accountIsDeleted() throws Exception {
        startServer(201, "");

        assertNotNull(db.getUser(SessionManager.getUsername(context)));

        String correctPassword = "password";
        deleteAccount(correctPassword);

        assertThrows(UserNotFoundException.class, () -> db.getUser(SessionManager.getUsername(context)));
    }

    @Test
    public void deleteAccount_wrongPassword_accountIsNotDeleted() throws Exception {
        startServer(400, "");

        assertNotNull(db.getUser(SessionManager.getUsername(context)));

        String wrongPassword = "wrongPassword";
        deleteAccount(wrongPassword);

        assertNotNull(db.getUser(SessionManager.getUsername(context)));
    }

    @Test
    public void deleteAccount_serverConnectionError_accountIsNotDeleted() throws Exception {
        startServer(500, "");

        assertNotNull(db.getUser(SessionManager.getUsername(context)));

        String correctPassword = "password";
        deleteAccount(correctPassword);

        assertNotNull(db.getUser(SessionManager.getUsername(context)));
    }
}
