package androidTestFiles.UI;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.activity.CourseIndexActivity;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.CompleteCourse;
import org.digitalcampus.oppia.model.CompleteCourseProvider;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.task.ParseCourseXMLTask;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;

import androidTestFiles.TestRules.DaggerInjectMockUITest;
import androidTestFiles.Utils.CourseUtils;
import androidTestFiles.Utils.TestUtils;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import static androidTestFiles.Utils.CourseUtils.mockCourse;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;

@RunWith(AndroidJUnit4.class)
public class BaselineUITest extends DaggerInjectMockUITest {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Mock
    CompleteCourseProvider completeCourseProvider;

    private Intent getIntentWithPretest(CompleteCourse course, boolean attempted, String jumpToDigest){
        ArrayList<Activity> baseline = new ArrayList<>();
        Activity pretest = new Activity();
        pretest.setDigest("aaaaa");
        pretest.setAttempted(attempted);
        pretest.setActType("quiz");
        baseline.add(pretest);
        course.setBaselineActivities(baseline);

        Bundle args = new Bundle();
        args.putSerializable(Course.TAG, course);

        if (jumpToDigest != null){
            args.putSerializable(CourseIndexActivity.JUMPTO_TAG, jumpToDigest);
        }


        Intent i = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), CourseIndexActivity.class);
        i.putExtras(args);
        return i;
    }

    private void configureMockProviderResponse(CompleteCourse course){

        CompleteCourse mockCourse = mockCourse(course);

        Mockito.doAnswer((Answer<Void>) invocation -> {
            Context ctx = (Context) invocation.getArguments()[0];
            ((ParseCourseXMLTask.OnParseXmlListener) ctx).onParseComplete(mockCourse);
            return null;
        }).when(completeCourseProvider).getCompleteCourseAsync(any(Context.class), any());

        Mockito.doAnswer((Answer<CompleteCourse>) invocation -> mockCourse
        ).when(completeCourseProvider).getCompleteCourseSync(any(Context.class), any());


    }

    @Test
    public void showPretestDialogIfPretestNotAttempted() throws Exception{

        final CompleteCourse completeCourse = CourseUtils.createMockCompleteCourse(5, 7);

        Intent i = getIntentWithPretest(completeCourse, false, null);
        configureMockProviderResponse(completeCourse);

        try (ActivityScenario<CourseIndexActivity> scenario = ActivityScenario.launch(i)) {

            onView(withText(R.string.alert_pretest))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void dontNavigateWithDigestIfPretestNotAttempted() throws Exception{

        final CompleteCourse completeCourse = CourseUtils.createMockCompleteCourse(5, 7);

        Intent i = getIntentWithPretest(completeCourse, false, "actdigest");

        configureMockProviderResponse(completeCourse);

        try (ActivityScenario<CourseIndexActivity> scenario = ActivityScenario.launch(i)) {

            onView(withText(R.string.alert_pretest))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void navigateWithDigestIfPretestAttempted() throws Exception{

        final CompleteCourse completeCourse = CourseUtils.createMockCompleteCourse(5, 7);
        completeCourse.getSections().get(0).getActivities().get(1).setDigest("actdigest");

        Intent i = getIntentWithPretest(completeCourse, true, "actdigest");
        configureMockProviderResponse(completeCourse);

        try (ActivityScenario<CourseIndexActivity> scenario = ActivityScenario.launch(i)) {

            assertEquals(CourseActivity.class, TestUtils.getCurrentActivity().getClass());
        }
    }

    @Test
    public void dontShowPretestDialogIfPretestAttempted() throws Exception{

        final CompleteCourse completeCourse = CourseUtils.createMockCompleteCourse(5, 7);
        Intent i = getIntentWithPretest(completeCourse, true, null);
        configureMockProviderResponse(completeCourse);

        try (ActivityScenario<CourseIndexActivity> scenario = ActivityScenario.launch(i)) {
            onView(withText(R.string.alert_pretest))
                    .check(doesNotExist());
        }
    }
}
