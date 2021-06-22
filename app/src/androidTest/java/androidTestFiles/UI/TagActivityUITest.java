package androidTestFiles.UI;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.DownloadCoursesActivity;
import org.digitalcampus.oppia.model.Tag;
import org.digitalcampus.oppia.model.TagRepository;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.ArrayList;

import androidTestFiles.TestRules.DaggerInjectMockUITest;

import static androidTestFiles.Matchers.RecyclerViewMatcher.withRecyclerView;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

@RunWith(AndroidJUnit4.class)
public class TagActivityUITest extends DaggerInjectMockUITest {

    @Rule
    public ActivityTestRule<DownloadCoursesActivity> tagSelectActivityTestRule =
            new ActivityTestRule<>(DownloadCoursesActivity.class, false, false);

    @Mock TagRepository tagRepository;

    @Test
    public void showCorrectCategory() throws Exception {
//        doAnswer(invocationOnMock -> {
//            Context ctx = (Context) invocationOnMock.getArguments()[0];
//            BasicResult result = new BasicResult();
//            result.setSuccess(true);
//            result.setResultMessage("{}");
//            ((DownloadCoursesActivity) ctx).apiRequestComplete(result);
//            return null;
//        }).when(tagRepository).getTagList(any(), any());


        doAnswer(invocationOnMock -> {
            ArrayList<Tag> tags = (ArrayList<Tag>) invocationOnMock.getArguments()[0];
            tags.add(new Tag() {{
                setName("Mocked Course Name");
                setDescription("Mocked Course Description");
            }});
            return null;
        }).when(tagRepository).refreshTagList(any(), any());

        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        Intent intent = new Intent(context, DownloadCoursesActivity.class);
        intent.putExtra(DownloadCoursesActivity.EXTRA_MODE, DownloadCoursesActivity.MODE_TAG_COURSES);

        tagSelectActivityTestRule.launchActivity(intent);

        onView(withRecyclerView(R.id.recycler_tags)
                .atPositionOnView(0, R.id.tag_name))
                .check(matches(withText(startsWith("Mocked Course Name"))));

    }

}
