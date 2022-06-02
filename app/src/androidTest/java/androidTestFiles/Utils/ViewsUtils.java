package androidTestFiles.Utils;

import android.view.View;
import android.widget.EditText;

import androidx.annotation.IdRes;
import androidx.test.espresso.Root;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.BoundedMatcher;

import com.google.android.material.textfield.TextInputLayout;

import org.digitalcampus.mobile.learning.R;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.internal.util.Checks.checkNotNull;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.is;

import androidTestFiles.Utils.Matchers.ToastMatcher;

public class ViewsUtils {


    /*
     * Use this method to find the EditText within the TextInputLayout. Useful for typing into the TextInputLayout
     */
    public static ViewInteraction onEditTextWithinTextInputLayoutWithId(@IdRes int textInputLayoutId) {
        //Note, if you have specified an ID for the EditText that you place inside
        //the TextInputLayout, use that instead - i.e, onView(withId(R.id.my_edit_text));
        return onView(allOf(isDescendantOfA(withId(textInputLayoutId)), isAssignableFrom(EditText.class)));
    }

    public static ViewInteraction onEditTextWithinTextInputLayout(final Matcher<View> parent) {
        //Note, if you have specified an ID for the EditText that you place inside
        //the TextInputLayout, use that instead - i.e, onView(withId(R.id.my_edit_text));
        return onView(allOf(isDescendantOfA(parent), isAssignableFrom(EditText.class)));
    }

    /*
     * Use this method to find the error view within the TextInputLayout. Useful for asseting that certain errors are displayed to the user
     */
    public static ViewInteraction onErrorViewWithinTextInputLayoutWithId(@IdRes int textInputLayoutId) {
        return onView(allOf(isDescendantOfA(withId(textInputLayoutId)), withId(R.id.textinput_error)));
    }

    public static ViewInteraction onHelperTextViewWithinTextInputLayoutWithId(@IdRes int textInputLayoutId) {
        return onView(allOf(isDescendantOfA(withId(textInputLayoutId)), withId(R.id.textinput_helper_text)));
    }

    public static Matcher<View> withHintInInputLayout(final Matcher<String> stringMatcher) {
        checkNotNull(stringMatcher);

        return new BoundedMatcher<View, TextInputLayout>(TextInputLayout.class) {
            String actualHint = "";

            @Override
            public void describeTo(Description description) {
                description.appendText("with hint: ");
                stringMatcher.describeTo(description);
                description.appendText("But got: " + actualHint);
            }

            @Override
            public boolean matchesSafely(TextInputLayout textInputLayout) {
                CharSequence hint = textInputLayout.getHint();
                if (hint != null) {
                    actualHint = hint.toString();
                    return stringMatcher.matches(actualHint);
                }
                return false;
            }
        };
    }

    public static Matcher<View> withHintInInputLayout(final String string) {
        return withHintInInputLayout(is(string));
    }

    public static Matcher<Root> isToast() {
        return new ToastMatcher();
    }
}
