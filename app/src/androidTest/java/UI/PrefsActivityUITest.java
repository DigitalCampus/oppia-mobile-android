package UI;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.di.AppComponent;
import org.digitalcampus.oppia.di.AppModule;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.Lang;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;

import Utils.CourseUtils;
import it.cosenonjaviste.daggermock.DaggerMockRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class PrefsActivityUITest {


    @Rule
    public DaggerMockRule<AppComponent> daggerRule =
            new DaggerMockRule<>(AppComponent.class, new AppModule((MobileLearning) InstrumentationRegistry.getInstrumentation()
                    .getTargetContext()
                    .getApplicationContext())).set(
                    new DaggerMockRule.ComponentSetter<AppComponent>() {
                        @Override
                        public void setComponent(AppComponent component) {
                            MobileLearning app =
                                    (MobileLearning) InstrumentationRegistry.getInstrumentation()
                                            .getTargetContext()
                                            .getApplicationContext();
                            app.setComponent(component);
                        }
                    });

    @Rule
    public ActivityTestRule<PrefsActivity> prefsActivityTestRule =
            new ActivityTestRule<>(PrefsActivity.class, false, false);

    @Mock
    CoursesRepository coursesRepository;
    @Mock
    SharedPreferences prefs;
    @Mock
    SharedPreferences.Editor editor;


    @Before
    public void setUp() throws Exception {
        initMockEditor();
        when(prefs.edit()).thenReturn(editor);
    }

    private void initMockEditor() {
        when(editor.putString(anyString(), anyString())).thenReturn(editor);
        when(editor.putLong(anyString(), anyLong())).thenReturn(editor);
        when(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor);
    }

    private void givenThereAreSomeCourses(int numberOfCourses) {

        ArrayList<Course> courses = new ArrayList<>();

        for (int i = 0; i < numberOfCourses; i++) {
            courses.add(CourseUtils.createMockCourse());
        }

        when(coursesRepository.getCourses((Context) any())).thenReturn(courses);

    }

    private int getCoursesCount() {
        return coursesRepository.getCourses((Context) any()).size();
    }



    @Test
    public void showsChangeLanguageOptionIfThereAreCoursesWithManyLanguages() throws Exception {

        givenThereAreSomeCourses(1);

        coursesRepository.getCourses((Context) any()).get(0).setLangs(new ArrayList<Lang>() {{
            add(new Lang("en", "English"));
            add(new Lang("es", "Spanish"));
        }});

        prefsActivityTestRule.launchActivity(null);

        onView(withText(R.string.prefLanguage)).check(matches(isDisplayed()));
    }

    @Test
    public void hideChangeLanguageOptionIfThereAreCoursesWithOnlyOneLanguage() throws Exception {

        givenThereAreSomeCourses(1);

        coursesRepository.getCourses((Context) any()).get(0).setLangs(new ArrayList<Lang>() {{
            add(new Lang("en", "English"));
        }});

        prefsActivityTestRule.launchActivity(null);

        onView(withText(R.string.prefLanguage)).check(doesNotExist());
    }
}
