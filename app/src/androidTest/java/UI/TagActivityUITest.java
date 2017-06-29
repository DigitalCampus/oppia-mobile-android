package UI;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.TagSelectActivity;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.di.AppComponent;
import org.digitalcampus.oppia.di.AppModule;
import org.digitalcampus.oppia.model.Tag;
import org.digitalcampus.oppia.model.TagRepository;
import org.digitalcampus.oppia.task.Payload;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;

import TestRules.DisableAnimationsRule;
import it.cosenonjaviste.daggermock.DaggerMockRule;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

@RunWith(AndroidJUnit4.class)
public class TagActivityUITest {

    @Rule public DaggerMockRule<AppComponent> daggerRule =
            new DaggerMockRule<>(AppComponent.class, new AppModule((MobileLearning) InstrumentationRegistry.getInstrumentation()
                    .getTargetContext()
                    .getApplicationContext())).set(
                    new DaggerMockRule.ComponentSetter<AppComponent>() {
                        @Override public void setComponent(AppComponent component) {
                            MobileLearning app =
                                    (MobileLearning) InstrumentationRegistry.getInstrumentation()
                                            .getTargetContext()
                                            .getApplicationContext();
                            app.setComponent(component);
                        }
                    });

    @Rule
    public ActivityTestRule<TagSelectActivity> tagSelectActivityTestRule =
            new ActivityTestRule<>(TagSelectActivity.class, false, false);

    @Rule
    public DisableAnimationsRule disableAnimationsRule = new DisableAnimationsRule();


    @Mock TagRepository tagRepository;


    @Test
    public void showCorrectCategory() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Context ctx = (Context) invocationOnMock.getArguments()[0];
                Payload response = new Payload();
                response.setResult(true);
                response.setResultResponse("{}");
                ((TagSelectActivity) ctx).apiRequestComplete(response);
                return null;
            }
        }).when(tagRepository).getTagList((Context) any());


        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                ArrayList<Tag> tags = (ArrayList<Tag>) invocationOnMock.getArguments()[0];
                tags.add(new Tag() {{
                    setName("Mocked Course Name");
                    setDescription("Mocked Course Description");
                }});
                return null;
            }
        }).when(tagRepository).refreshTagList((ArrayList<Tag>) any(), (JSONObject) any());

        tagSelectActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.tag_list))
                .atPosition(0)
                .onChildView(withId(R.id.tag_name))
                .check(matches(withText(startsWith("Mocked Course Name"))));
    }

}
