package UI;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.di.AppComponent;
import org.digitalcampus.oppia.di.AppModule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import it.cosenonjaviste.daggermock.DaggerMockRule;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class LoginUITest {

    private MockWebServer mockServer;

    private static final String VALID_LOGIN_RESPONSE = "responses/response_200_login.json";
    private static final String WRONG_CREDENTIALS_RESPONSE = "responses/response_400_login.json";

    @Rule
    public DaggerMockRule<AppComponent> daggerRule =
                new DaggerMockRule<>(AppComponent.class, new AppModule(getApp())).set(
                    new DaggerMockRule.ComponentSetter<AppComponent>() {
                        @Override
                        public void setComponent(AppComponent component) {
                            getApp().setComponent(component);
                        }
                    });

    @Rule
    public ActivityTestRule<WelcomeActivity> welcomeActivityTestRule =
            new ActivityTestRule<>(WelcomeActivity.class, false, false);

    private App getApp() {
        return (App) InstrumentationRegistry.getInstrumentation()
                .getTargetContext().getApplicationContext();
    }

    @Mock
    ApiEndpoint apiEndpoint;


    private void startServer(int responseCode, String responseAsset, int timeoutDelay){
        try {
            mockServer = new MockWebServer();
            MockResponse response = new MockResponse();
            response.setResponseCode(responseCode);
            String responseBody = Utils.FileUtils.getStringFromFile(
                    InstrumentationRegistry.getInstrumentation().getContext(), responseAsset);

            if (responseBody!=null) { response.setBody(responseBody); }
            if (timeoutDelay > 0){
                response.setBodyDelay(timeoutDelay, TimeUnit.MILLISECONDS);

            }
            mockServer.enqueue(response);
            mockServer.start();

            when(apiEndpoint.getFullURL((Context) any(), anyString())).thenReturn(mockServer.url("").toString());

        }catch(IOException ioe) {
            ioe.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void showsErrorMessageWhenThereIsNoUsername() throws Exception{
        welcomeActivityTestRule.launchActivity(null);

        onView(withId(R.id.welcome_login))
                .perform(scrollTo(), click());

        onView(withId(R.id.login_btn))
                .perform(scrollTo(), click());

        onView(withText(R.string.error_no_username))
                .check(matches(isDisplayed()));
    }

    @Test
    public void showsErrorMessageWhenTheUsernameOrPasswordAreWrong() throws Exception{

        startServer(400, WRONG_CREDENTIALS_RESPONSE, 0);
        welcomeActivityTestRule.launchActivity(null);

        onView(withId(R.id.welcome_login))
                .perform(scrollTo(), click());

        onView(withId(R.id.login_username_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("WrongUsername"));

        onView(withId(R.id.login_password_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("WrongPassword"));

        onView(withId(R.id.login_btn))
                .perform(scrollTo(), click());

        onView(withText(R.string.error_login))
                .check(matches(isDisplayed()));
    }

    @Test
    public void changeActivityWhenTheCredentialsAreCorrect() throws Exception {

        startServer(200, VALID_LOGIN_RESPONSE, 0);
        welcomeActivityTestRule.launchActivity(null);

        onView(withId(R.id.welcome_login))
               .perform(scrollTo(), click());

        onView(withId(R.id.login_username_field))
               .perform(closeSoftKeyboard(), scrollTo(), typeText("valid_username"));

        onView(withId(R.id.login_password_field))
               .perform(closeSoftKeyboard(), scrollTo(), typeText("valid_password"));

        onView(withId(R.id.login_btn))
               .perform(scrollTo(), click());

        assertEquals(MainActivity.class, Utils.TestUtils.getCurrentActivity().getClass());
    }


}
