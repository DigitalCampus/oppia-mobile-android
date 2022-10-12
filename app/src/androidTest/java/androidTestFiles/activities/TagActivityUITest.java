package androidTestFiles.activities;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static androidTestFiles.utils.matchers.RecyclerViewMatcher.withRecyclerView;
import static androidTestFiles.utils.parent.BaseTest.COURSE_TEST;
import static androidTestFiles.utils.parent.BaseTest.COURSE_TEST_2;
import static androidTestFiles.utils.parent.BaseTest.PATH_COMMON_TESTS;
import static androidTestFiles.utils.parent.BaseTest.PATH_TESTS;
import static androidTestFiles.utils.parent.BaseTest.TAGS_LIVE_DRAFT_RESPONSE;
import static androidTestFiles.utils.parent.BaseTest.TAGS_NO_COURSE_STATUSES_FIELD_RESPONSE;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.TagSelectActivity;
import org.digitalcampus.oppia.model.Tag;
import org.digitalcampus.oppia.model.TagRepository;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;

import java.util.ArrayList;

import androidTestFiles.utils.parent.BaseTest;
import androidTestFiles.utils.parent.MockedApiEndpointTest;

@RunWith(AndroidJUnit4.class)
public class TagActivityUITest extends MockedApiEndpointTest {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    TagRepository tagRepository;

    @Test
    public void showCorrectCategory() throws Exception {

        doAnswer(invocationOnMock -> {
            Context ctx = (Context) invocationOnMock.getArguments()[0];
            BasicResult result = new BasicResult();
            result.setSuccess(true);
            result.setResultMessage("{}");
            ((TagSelectActivity) ctx).apiRequestComplete(result);
            return null;
        }).when(tagRepository).getTagList(any(), any());


        doAnswer(invocationOnMock -> {
            ArrayList<Tag> tags = (ArrayList<Tag>) invocationOnMock.getArguments()[0];
            tags.add(new Tag() {{
                setName("Mocked Course Name");
                setDescription("Mocked Course Description");
                setCountAvailable(3);
            }});
            return null;
        }).when(tagRepository).refreshTagList(any(), any(), any());

        startServer(200, null);

        try (ActivityScenario<TagSelectActivity> scenario = ActivityScenario.launch(TagSelectActivity.class)) {

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.tag_name))
                    .check(matches(withText(startsWith("Mocked Course Name"))));

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.tag_count))
                    .check(matches(withText("3")));
        }
    }


    @Test
    public void checkCountOfLiveAndDraftCourses() throws Exception {

        startServer(200, TAGS_LIVE_DRAFT_RESPONSE, 0, 2);

        try (ActivityScenario<TagSelectActivity> scenario = ActivityScenario.launch(TagSelectActivity.class)) {

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.tag_count))
                    .check(matches(withText("1")));
        }

    }

    @Test
    public void checkCountOfReadOnlyCoursesIfInstalled() throws Exception {

        installCourse(PATH_COMMON_TESTS, COURSE_TEST);

        startServer(200, TAGS_LIVE_DRAFT_RESPONSE, 0, 2);

        try (ActivityScenario<TagSelectActivity> scenario = ActivityScenario.launch(TagSelectActivity.class)) {

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.tag_count))
                    .check(matches(withText("2")));
        }

    }

    @Test
    public void checkCountOfNewDownloadsDisabledCoursesIfInstalled() throws Exception {

        installCourse(PATH_COMMON_TESTS, COURSE_TEST_2);

        startServer(200, TAGS_LIVE_DRAFT_RESPONSE, 0, 2);

        try (ActivityScenario<TagSelectActivity> scenario = ActivityScenario.launch(TagSelectActivity.class)) {

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.tag_count))
                    .check(matches(withText("2")));
        }

    }

    @Test
    public void checkCountOfReadOnlyAndNewDownloadsDisabledCoursesIfInstalled() throws Exception {

        installCourse(PATH_COMMON_TESTS, COURSE_TEST);
        installCourse(PATH_COMMON_TESTS, COURSE_TEST_2);

        startServer(200, TAGS_LIVE_DRAFT_RESPONSE, 0, 2);

        try (ActivityScenario<TagSelectActivity> scenario = ActivityScenario.launch(TagSelectActivity.class)) {

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.tag_count))
                    .check(matches(withText("3")));
        }

    }

    @Test
    public void checkCountIfNoCoursesStatusesFieldExists() throws Exception {

        startServer(200, TAGS_NO_COURSE_STATUSES_FIELD_RESPONSE, 0, 2);

        try (ActivityScenario<TagSelectActivity> scenario = ActivityScenario.launch(TagSelectActivity.class)) {

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.tag_count))
                    .check(matches(withText("3")));

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(1, R.id.tag_count))
                    .check(matches(withText("2")));
        }

    }
}
