package androidTestFiles.Services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.work.Configuration;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.testing.SynchronousExecutor;
import androidx.work.testing.TestListenableWorkerBuilder;
import androidx.work.testing.WorkManagerTestInitHelper;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.di.AppComponent;
import org.digitalcampus.oppia.di.AppModule;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.service.UserCohortsCheckerWorker;
import org.digitalcampus.oppia.service.UserCohortsChecksWorkerManager;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import androidTestFiles.database.sampledata.UserData;
import it.cosenonjaviste.daggermock.DaggerMockRule;

@RunWith(AndroidJUnit4.class)
public class UserCohortsCheckerWorkerTest {

    @Rule
    public DaggerMockRule<AppComponent> daggerRule =
            new DaggerMockRule<>(AppComponent.class, new AppModule((App) InstrumentationRegistry.getInstrumentation()
                    .getTargetContext()
                    .getApplicationContext())).set(
                    component -> {

                        App app =
                                (App) InstrumentationRegistry.getInstrumentation()
                                        .getTargetContext()
                                        .getApplicationContext();
                        app.setComponent(component);
                    });

    private Context context;
    @Mock
    User user;

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
    public void testCohortsCheckerWorkerFinishSuccessfully() throws ExecutionException, InterruptedException {
        when(user.getUsername()).thenReturn(UserData.TEST_USER_1);

        ListenableWorker testUserCohortsCheckerWorker = TestListenableWorkerBuilder.from(context, UserCohortsCheckerWorker.class).build();
        ListenableWorker.Result result = testUserCohortsCheckerWorker.startWork().get();
        assertThat(result, is(ListenableWorker.Result.success()));
    }

    @Test
    public void doesReturnFailureIfNoUserIsLoggedIn() throws Exception {
        when(user.getUsername()).thenReturn(null);

        ListenableWorker testUserCohortsCheckerWorker = TestListenableWorkerBuilder.from(context, UserCohortsCheckerWorker.class).build();
        ListenableWorker.Result result = testUserCohortsCheckerWorker.startWork().get();
        Data expectedData = new Data.Builder().putString(UserCohortsChecksWorkerManager.RESULT_MESSAGE, context.getString(R.string.user_not_logged_in)).build();
        assertThat(result, is(ListenableWorker.Result.failure(expectedData)));
    }

    @Test
    public void updateUserCohortsIfResultIsSuccessful() throws Exception {
        UserData.loadData(context);
        // Initial cohorts of UserData.TEST_USER_1 = [1,2]
        when(user.getUsername()).thenReturn(UserData.TEST_USER_1);
        doCallRealMethod().when(user).setCohorts(any());
        doCallRealMethod().when(user).getCohorts();

        List<Integer> updatedCohorts = Arrays.asList(2,3,4);
        UserCohortsChecksWorkerManager userCohortsChecksWorkerManager = spy(new UserCohortsChecksWorkerManager(context));
        doAnswer(invocationOnMock -> {
            // Mock API result for returning a success response and the list of updated cohorts
            BasicResult result = new BasicResult();
            result.setSuccess(true);
            result.setResultMessage(updatedCohorts.toString());
            userCohortsChecksWorkerManager.apiRequestComplete(result);
            return null;
        }).when(userCohortsChecksWorkerManager).checkUserCohortsUpdates();
        userCohortsChecksWorkerManager.setOnFinishListener(result -> assertThat(result, is(ListenableWorker.Result.success())));
        userCohortsChecksWorkerManager.startChecks();

        List<Integer> cohorts = DbHelper.getInstance(context).getUser(UserData.TEST_USER_1).getCohorts();
        assertThat(cohorts, is(updatedCohorts));
    }

    @Test
    public void dontUpdateUserCohortsIfUserIsNotLoggedIn() throws Exception {
        UserData.loadData(context);
        when(user.getUsername()).thenReturn(null); // No user logged in
        doCallRealMethod().when(user).setCohorts(any());
        doCallRealMethod().when(user).getCohorts();

        List<Integer> initialCohorts = Arrays.asList(1,2); // Cohorts of UserData.TEST_USER_1
        List<Integer> updatedCohorts = Arrays.asList(2,3,4);
        UserCohortsChecksWorkerManager userCohortsChecksWorkerManager = spy(new UserCohortsChecksWorkerManager(context));
        doAnswer(invocationOnMock -> {
            // Mock API result for returning a success response and the list of updated cohorts
            BasicResult result = new BasicResult();
            result.setSuccess(true);
            result.setResultMessage(updatedCohorts.toString());
            userCohortsChecksWorkerManager.apiRequestComplete(result);
            return null;
        }).when(userCohortsChecksWorkerManager).checkUserCohortsUpdates();

        Data expectedData = new Data.Builder().putString(UserCohortsChecksWorkerManager.RESULT_MESSAGE, context.getString(R.string.user_not_logged_in)).build();
        userCohortsChecksWorkerManager.setOnFinishListener(result -> assertThat(result, is(ListenableWorker.Result.failure(expectedData))));
        userCohortsChecksWorkerManager.startChecks();

        List<Integer> cohorts = DbHelper.getInstance(context).getUser(UserData.TEST_USER_1).getCohorts();
        assertThat(cohorts, is(initialCohorts));
    }

}
