package androidTestFiles.org.digitalcampus.oppia.activity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static androidTestFiles.Matchers.RecyclerViewMatcher.withRecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.activity.TagSelectActivity;
import org.digitalcampus.oppia.model.Tag;
import org.digitalcampus.oppia.model.TagRepository;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.ArrayList;

import androidTestFiles.TestRules.DaggerInjectMockUITest;
import androidTestFiles.Utils.FileUtils;
import androidTestFiles.Utils.MockedApiEndpointTest;
import androidx.test.rule.GrantPermissionRule;

@RunWith(AndroidJUnit4.class)
public class TagActivityUITest extends DaggerInjectMockUITest {

    @Mock
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
            }});
            return null;
        }).when(tagRepository).refreshTagList(any(), any());

        try (ActivityScenario<TagSelectActivity> scenario = ActivityScenario.launch(TagSelectActivity.class)) {

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.tag_name))
                    .check(matches(withText(startsWith("Mocked Course Name"))));
        }
    }



}
