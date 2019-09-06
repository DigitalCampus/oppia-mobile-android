package Services;

import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ServiceTestRule;
import androidx.work.Configuration;
import androidx.work.ListenableWorker;
import androidx.work.testing.TestListenableWorkerBuilder;
import androidx.work.testing.WorkManagerTestInitHelper;

import org.digitalcampus.oppia.service.TrackerWorker;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


@RunWith(AndroidJUnit4.class)
public class TrakerWorkerTest {

    private Context context;

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Configuration config = new Configuration.Builder()
                // Set log level to Log.DEBUG to
                // make it easier to see why tests failed
                .setMinimumLoggingLevel(Log.DEBUG)
                // Use a SynchronousExecutor to make it easier to write tests
//                .setExecutor(new SynchronousExecutor())
                .build();

        // Initialize WorkManager for instrumentation tests.
        WorkManagerTestInitHelper.initializeTestWorkManager(
                context, config);

    }


    @Test
    public void testTrackerWorkerFinishSuccessfuly() throws ExecutionException, InterruptedException {


        // FOR LISTENABLE WORKERS: (https://stackoverflow.com/a/56200464/1365440)
        TestListenableWorkerBuilder<TrackerWorker> testTrackerWorker = TestListenableWorkerBuilder.from(context, TrackerWorker.class);
        ListenableWorker.Result result = testTrackerWorker.build().startWork().get();
        assertThat(result, is(ListenableWorker.Result.success()));


        // FOR NORMAL WORKERS:

//        WorkManager workManager = WorkManager.getInstance(context);
        // Create request
//        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(TrackerWorker.class, 1, TimeUnit.HOURS)
//                .setInitialDelay(1, TimeUnit.HOURS)
//                .build();
        // Get the TestDriver
//        TestDriver testDriver = WorkManagerTestInitHelper.getTestDriver(context);
//        // Enqueue
//        workManager.enqueue(request).getResult().get();
//        // Tells the WorkManager test framework that initial delays are now met.
//        testDriver.setInitialDelayMet(request.getId());
//        // Get WorkInfo
//        final ListenableFuture<WorkInfo> workInfo = workManager.getWorkInfoById(request.getId());
//
//        assertThat(workInfo.get().getState(), is(WorkInfo.State.ENQUEUED));


    }
}
