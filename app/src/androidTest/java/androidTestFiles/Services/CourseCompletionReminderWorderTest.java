package androidTestFiles.Services;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;
import androidx.work.ListenableWorker;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.di.AppComponent;
import org.digitalcampus.oppia.di.AppModule;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.TrackerLogRepository;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.service.CoursesCompletionReminderWorkerManager;
import org.digitalcampus.oppia.utils.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.Calendar;
import java.util.Collections;

import androidTestFiles.database.sampledata.CourseData;
import androidTestFiles.database.sampledata.UserData;
import it.cosenonjaviste.daggermock.DaggerMockRule;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class CourseCompletionReminderWorderTest {

    private Context context;

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

    @Mock
    CoursesRepository coursesRepository;
    @Mock
    TrackerLogRepository trackerLogRepository;
    @Mock
    User user;
    @Mock
    SharedPreferences prefs;

    private CoursesCompletionReminderWorkerManager coursesCompletionReminderWorkerManager;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        CourseData.loadData(context);
        UserData.loadData(context);
        coursesCompletionReminderWorkerManager = new CoursesCompletionReminderWorkerManager(context);

        when(prefs.getString(PrefsActivity.PREF_BADGE_AWARD_CRITERIA, null)).thenReturn("any_criteria");
    }

    @Test
    public void doesNotshowNotificationIfNoUserIsLoggedIn() throws Exception {

        when(user.getUsername()).thenReturn(null);

        ListenableWorker.Result result = coursesCompletionReminderWorkerManager.checkCompletionReminder();

        assertEquals(result, ListenableWorker.Result.success());
        checkNotificatonVisibility(false);

    }

    @Test
    public void doesNotShowNotificationIfCoursesCompleted() throws Exception {

        when(user.getUsername()).thenReturn("test_user");

        setHasLastWeekActivty(false);
        setCompletedCourses(true);

        ListenableWorker.Result result = coursesCompletionReminderWorkerManager.checkCompletionReminder();

        assertEquals(result, ListenableWorker.Result.success());
        checkNotificatonVisibility(false);
    }


    @Test
    public void doesNotShowNotificationIfActivityLastWeek() throws Exception {

        when(user.getUsername()).thenReturn("test_user");

        setHasLastWeekActivty(true);
        setCompletedCourses(false);

        ListenableWorker.Result result = coursesCompletionReminderWorkerManager.checkCompletionReminder();

        assertEquals(result, ListenableWorker.Result.success());
        checkNotificatonVisibility(false);
    }

    @Test
    public void doesShowNotificationIfNoActivityLastWeekAndAnyCourseNotCompleted() throws Exception {

        when(user.getUsername()).thenReturn("test_user");

        setHasLastWeekActivty(false);
        setCompletedCourses(false);

        ListenableWorker.Result result = coursesCompletionReminderWorkerManager.checkCompletionReminder();

        assertEquals(result, ListenableWorker.Result.success());
        checkNotificatonVisibility(true);
    }


    // ------

    private void checkNotificatonVisibility(boolean visible) {

        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        device.openNotification();
        device.wait(Until.hasObject(By.text(context.getString(R.string.courses_reminder_notif_title))), 1000);

        if (visible) {
            Assert.assertNotNull(device.findObject(By.text(context.getString(R.string.courses_reminder_notif_title))));
        } else {
            Assert.assertNull(device.findObject(By.text(context.getString(R.string.courses_reminder_notif_title))));
        }
        device.pressBack(); // To close notification panel.
    }

    private void setCompletedCourses(boolean completed) {

        Course course = mock(Course.class);
        when(course.isComplete(any(), any(), anyString(), anyInt())).thenReturn(completed);
        when(coursesRepository.getCourses(any())).thenReturn(Collections.singletonList(course));
        when(coursesRepository.getCourse(any(), anyLong(), anyLong())).thenReturn(course);
    }


    private void setHasLastWeekActivty(boolean lastWeekActivity) throws Exception {

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, lastWeekActivity
                ? -(CoursesCompletionReminderWorkerManager.PERIOD_DAYS_REMINDER - 1)
                : -(CoursesCompletionReminderWorkerManager.PERIOD_DAYS_REMINDER + 1));
        when(trackerLogRepository.getLastTrackerDatetime(any()))
                .thenReturn(DateUtils.DATETIME_FORMAT.print(calendar.getTimeInMillis()));
    }


}
