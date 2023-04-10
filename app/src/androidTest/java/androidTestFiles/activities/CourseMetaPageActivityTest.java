package androidTestFiles.activities;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.digitalcampus.oppia.activity.CourseIndexActivity.JUMPTO_TAG;
import static org.hamcrest.Matchers.allOf;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseIndexActivity;
import org.digitalcampus.oppia.activity.CourseMetaPageActivity;
import org.digitalcampus.oppia.model.CompleteCourse;
import org.digitalcampus.oppia.model.CompleteCourseProvider;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CourseMetaPage;
import org.digitalcampus.oppia.model.Lang;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Collections;

import androidTestFiles.utils.parent.DaggerInjectMockUITest;

@RunWith(AndroidJUnit4.class)
public class CourseMetaPageActivityTest extends DaggerInjectMockUITest {

    private static String META_PAGE_TITLE = "Meta page test title";

    @Mock
    CompleteCourseProvider courseProvider;

    @Before
    public void setUp() throws Exception {
        CompleteCourse mockedCourse = getMockCourse();
        when(courseProvider.getCompleteCourseSync(any(), any())).thenReturn(mockedCourse);
    }

    private CompleteCourse getMockCourse(){
        CompleteCourse course = new CompleteCourse();
        course.setVersionId(0d);
        course.setShortname("courseactivity_test");
        ArrayList<Lang> langs = new ArrayList<>();
        langs.add(new Lang("en", "Test Course"));
        course.setTitles(langs);
        course.setCourseId(0);
        course.setMetaPages(new ArrayList<>(Collections.singletonList(getMockedMetaPage())));
        return course;
    }

    private CourseMetaPage getMockedMetaPage() {
        CourseMetaPage cmp = new CourseMetaPage();
        cmp.setId(0);
        Lang lang = new Lang("en", META_PAGE_TITLE);
        lang.setLocation("courses/meta/test_meta.html");
        cmp.addLang(lang);
        return cmp;
    }

    private Intent getIntentParams(Course course, String jumpToDigest){
        Intent i = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), CourseMetaPageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Course.TAG, course);
        if (jumpToDigest != null){
            bundle.putSerializable(JUMPTO_TAG, jumpToDigest);
        }
        bundle.putInt(CourseMetaPage.TAG, 0);
        i.putExtras(bundle);
        return i;
    }

    public void givenACorrectCourse(CompleteCourse c){
        doAnswer(invocationOnMock -> {
            Context ctx = (Context) invocationOnMock.getArguments()[0];
            ((CourseIndexActivity) ctx).onParseComplete(c);
            return null;
        }).when(courseProvider).getCompleteCourseAsync(any(), any());
    }

    @Test
    public void selectAllOptionIsClickableIfPendingMediaUnselected() throws Exception {
        CompleteCourse c = getMockCourse();
        Intent i = getIntentParams(c, null);
        givenACorrectCourse(c);

        try (ActivityScenario<CourseMetaPageActivity> scenario = ActivityScenario.launch(i)) {
            onView(withId(R.id.course_title)).check(matches(allOf(isDisplayed(), withText(META_PAGE_TITLE))));
            onView(withId(R.id.metapage_webview)).check(matches(isDisplayed()));

        }
    }
}
