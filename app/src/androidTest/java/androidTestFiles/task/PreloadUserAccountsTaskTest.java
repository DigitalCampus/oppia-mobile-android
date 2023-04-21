package androidTestFiles.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.task.PreloadAccountsTask;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import androidTestFiles.utils.FileUtils;


public class PreloadUserAccountsTaskTest {

    private Context context;
    private DbHelper db;

    @Before
    public void setUp() throws IOException {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        db = DbHelper.getInstance(context);
        File location = new File(Storage.getStorageLocationRoot(context));
        String filename = "oppia_accounts.csv";
        FileUtils.copyFileFromAssets(context, "tests/accounts", filename, location, filename);
    }

    private void preloadUserAccounts(boolean expectedResult) {
        final CountDownLatch signal = new CountDownLatch(1);

        PreloadAccountsTask task = new PreloadAccountsTask(context);
        task.setPreloadAccountsListener(result -> {
            assertEquals(expectedResult, result.isSuccess());
            signal.countDown();
        });

        task.execute();

        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void preloadUserAccountsTest_success() throws UserNotFoundException {
        // Users don't exists
        assertThrows(UserNotFoundException.class, () -> db.getUser("testuser1"));
        assertThrows(UserNotFoundException.class, () -> db.getUser("testuser2"));

        // Preload user accounts
        preloadUserAccounts(true);

        // Users exist
        assertNotNull(db.getUser("testuser1"));
        assertNotNull(db.getUser("testuser2"));
    }

}
