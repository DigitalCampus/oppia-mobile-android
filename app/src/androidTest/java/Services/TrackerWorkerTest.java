package Services;

import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.work.Configuration;
import androidx.work.ListenableWorker;
import androidx.work.testing.SynchronousExecutor;
import androidx.work.testing.TestListenableWorkerBuilder;
import androidx.work.testing.WorkManagerTestInitHelper;

import org.digitalcampus.oppia.service.TrackerWorker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


@RunWith(AndroidJUnit4.class)
public class TrackerWorkerTest {

    private Context context;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Configuration config = new Configuration.Builder()
                // Set log level to Log.DEBUG to
                // make it easier to see why tests failed
                .setMinimumLoggingLevel(Log.DEBUG)
                // Use a SynchronousExecutor to make it easier to write tests
                .setExecutor(new SynchronousExecutor())
                .build();

        // Initialize WorkManager for instrumentation tests.
        WorkManagerTestInitHelper.initializeTestWorkManager(
                context, config);

    }


    @Test
    public void testTrackerWorkerFinishSuccessfuly() throws ExecutionException, InterruptedException {


        // FOR LISTENABLE WORKERS: (https://stackoverflow.com/a/56200464/1365440)
        ListenableWorker testTrackerWorker = TestListenableWorkerBuilder.from(context, TrackerWorker.class).build();
        ListenableWorker.Result result = testTrackerWorker.startWork().get();
        assertThat(result, is(ListenableWorker.Result.success()));

    }
}
