package androidTestFiles.features;

import static androidx.test.espresso.Espresso.pressBackUnconditionally;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.web.assertion.WebViewAssertions.webMatches;
import static androidx.test.espresso.web.sugar.Web.onWebView;
import static androidx.test.espresso.web.webdriver.DriverAtoms.findElement;
import static androidx.test.espresso.web.webdriver.DriverAtoms.getText;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.mockito.Mockito.when;
import static androidTestFiles.utils.UITestActionsUtils.waitForView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.web.webdriver.Locator;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.repository.InterfaceLanguagesRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Locale;

import androidTestFiles.utils.parent.DaggerInjectMockUITest;

@RunWith(AndroidJUnit4.class)
public class InterfaceLanguageUITest extends DaggerInjectMockUITest {


    @Mock
    InterfaceLanguagesRepository interfaceLanguagesRepository;

    @Before
    public void setUp() throws Exception {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"));
    }

    @After
    public void tearDown() throws Exception {

    }


    @Test
    public void changeInterfaceLanguageInSettings() throws Exception {

        when(interfaceLanguagesRepository.getLanguageOptions()).thenReturn(Arrays.asList("en", "es"));

        try (ActivityScenario<PrefsActivity> scenario = ActivityScenario.launch(PrefsActivity.class)) {

            waitForView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(R.string.prefDisplay_title)),
                            click()));

            waitForView(withText("Interface language")).check(matches(isDisplayed()));
            waitForView(withText("Idioma de interfaz")).check(doesNotExist());

            waitForView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(R.string.prefInterfaceLanguage)),
                            click()));

            waitForView(withText(getDisplayLanguage("es"))).perform(click());

            waitForView(withText("Interface language")).check(doesNotExist());
            waitForView(withText("Idioma de interfaz")).check(matches(isDisplayed()));

            waitForView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(R.string.prefInterfaceLanguage)),
                            click()));

            waitForView(withText(getDisplayLanguage("en"))).perform(click());

            waitForView(withText("Interface language")).check(matches(isDisplayed()));
            waitForView(withText("Idioma de interfaz")).check(doesNotExist());


        }
    }

    @Test
    public void changeInterfaceLanguageInAboutPage() throws Exception {

        when(interfaceLanguagesRepository.getLanguageOptions()).thenReturn(Arrays.asList("en", "es"));

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {

            openDrawer();
            waitForView(withText(R.string.menu_about)).perform(click());

            onWebView()
                    .withElement(findElement(Locator.TAG_NAME, "h3"))
                    .check(webMatches(getText(), containsStringIgnoringCase("Installing new courses")));

            pressBackUnconditionally();

            openDrawer();
            waitForView(withText(R.string.menu_settings)).perform(click());

            waitForView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(R.string.prefDisplay_title)),
                            click()));

            waitForView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(R.string.prefInterfaceLanguage)),
                            click()));

            waitForView(withText(getDisplayLanguage("es"))).perform(click());

            pressBackUnconditionally();
            pressBackUnconditionally();

            openDrawer();
            waitForView(withText(R.string.menu_about)).perform(click());

            onWebView()
                    .withElement(findElement(Locator.TAG_NAME, "h3"))
                    .check(webMatches(getText(), containsStringIgnoringCase("Instalar nuevos cursos")));


            pressBackUnconditionally();

            openDrawer();
            waitForView(withText(R.string.menu_settings)).perform(click());

            waitForView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(R.string.prefDisplay_title)),
                            click()));

            waitForView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(R.string.prefInterfaceLanguage)),
                            click()));

            waitForView(withText(getDisplayLanguage("en"))).perform(click());
        }
    }

    private String getDisplayLanguage(String langCode) {
        return new Locale(langCode).getDisplayLanguage(new Locale(langCode));
    }
}
