package androidTestFiles.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import static androidTestFiles.activities.EditProfileActivityTest.VALID_PROFILE_RESPONSE;

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
import org.digitalcampus.oppia.service.UpdateUserProfileWorker;
import org.digitalcampus.oppia.service.UpdateUserProfileWorkerManager;
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
import androidTestFiles.utils.FileUtils;
import it.cosenonjaviste.daggermock.DaggerMockRule;

@RunWith(AndroidJUnit4.class)
public class UpdateUserProfileWorkerTest {

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
    public void testUpdateUserProfileWorkerFinishSuccessfully() throws ExecutionException, InterruptedException {
        when(user.getUsername()).thenReturn(UserData.TEST_USER_1);

        ListenableWorker testUpdateUserProfileWorker = TestListenableWorkerBuilder.from(context, UpdateUserProfileWorker.class).build();
        ListenableWorker.Result result = testUpdateUserProfileWorker.startWork().get();
        assertThat(result, is(ListenableWorker.Result.success()));
    }

    @Test
    public void doesReturnFailureIfNoUserIsLoggedIn() throws Exception {
        when(user.getUsername()).thenReturn(null);

        ListenableWorker testUpdateUserProfileWorker = TestListenableWorkerBuilder.from(context, UpdateUserProfileWorker.class).build();
        ListenableWorker.Result result = testUpdateUserProfileWorker.startWork().get();
        Data expectedData = new Data.Builder().putString(UpdateUserProfileWorkerManager.RESULT_MESSAGE, context.getString(R.string.user_not_logged_in)).build();
        assertThat(result, is(ListenableWorker.Result.failure(expectedData)));
    }

    @Test
    public void updateUserProfileIfResultIsSuccessful() throws Exception {
        UserData.loadData(context);
        // Initial cohorts of UserData.TEST_USER_1 = [1,2]
        when(user.getUsername()).thenReturn(UserData.TEST_USER_1);
        doCallRealMethod().when(user).setCohorts(any());
        doCallRealMethod().when(user).updateFromJSON(any(), any());
        doCallRealMethod().when(user).setCohortsFromJSONArray(any());
        doCallRealMethod().when(user).getCohorts();

        String profileResponse = FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), VALID_PROFILE_RESPONSE);
        List<Integer> updatedCohorts = Arrays.asList(4,5,6);
        UpdateUserProfileWorkerManager updateUserProfileWorkerManager = spy(new UpdateUserProfileWorkerManager(context));
        doAnswer(invocationOnMock -> {
            // Mock API result for returning a success response and the list of updated cohorts
            BasicResult result = new BasicResult();
            result.setSuccess(true);
            result.setResultMessage(profileResponse);
            updateUserProfileWorkerManager.apiRequestComplete(result);
            return null;
        }).when(updateUserProfileWorkerManager).fetchUserProfile();
        updateUserProfileWorkerManager.setOnFinishListener(result -> assertThat(result, is(ListenableWorker.Result.success())));
        updateUserProfileWorkerManager.startChecks();

        List<Integer> cohorts = DbHelper.getInstance(context).getUser(UserData.TEST_USER_1).getCohorts();
        assertThat(cohorts, is(updatedCohorts));
    }

    @Test
    public void dontUpdateUserProfileIfUserIsNotLoggedIn() throws Exception {
        UserData.loadData(context);
        when(user.getUsername()).thenReturn(null); // No user logged in
        doCallRealMethod().when(user).setCohorts(any());
        doCallRealMethod().when(user).getCohorts();

        List<Integer> initialCohorts = Arrays.asList(1,2); // Cohorts of UserData.TEST_USER_1
        List<Integer> updatedCohorts = Arrays.asList(2,3,4);
        UpdateUserProfileWorkerManager updateUserProfileWorkerManager = spy(new UpdateUserProfileWorkerManager(context));
        doAnswer(invocationOnMock -> {
            // Mock API result for returning a success response and the list of updated cohorts
            BasicResult result = new BasicResult();
            result.setSuccess(true);
            result.setResultMessage(updatedCohorts.toString());
            updateUserProfileWorkerManager.apiRequestComplete(result);
            return null;
        }).when(updateUserProfileWorkerManager).fetchUserProfile();

        Data expectedData = new Data.Builder().putString(UpdateUserProfileWorkerManager.RESULT_MESSAGE, context.getString(R.string.user_not_logged_in)).build();
        updateUserProfileWorkerManager.setOnFinishListener(result -> assertThat(result, is(ListenableWorker.Result.failure(expectedData))));
        updateUserProfileWorkerManager.startChecks();

        List<Integer> cohorts = DbHelper.getInstance(context).getUser(UserData.TEST_USER_1).getCohorts();
        assertThat(cohorts, is(initialCohorts));
    }

}
