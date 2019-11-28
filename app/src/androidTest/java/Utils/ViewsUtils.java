package Utils;

import android.widget.EditText;

import androidx.annotation.IdRes;
import androidx.test.espresso.ViewInteraction;

import org.digitalcampus.mobile.learning.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.allOf;

public class ViewsUtils {


    /*
     * Use this method to find the EditText within the TextInputLayout. Useful for typing into the TextInputLayout
     */
    public static ViewInteraction onEditTextWithinTextInputLayoutWithId(@IdRes int textInputLayoutId) {
        //Note, if you have specified an ID for the EditText that you place inside
        //the TextInputLayout, use that instead - i.e, onView(withId(R.id.my_edit_text));
        return onView(allOf(isDescendantOfA(withId(textInputLayoutId)), isAssignableFrom(EditText.class)));
    }

    /*
     * Use this method to find the error view within the TextInputLayout. Useful for asseting that certain errors are displayed to the user
     */
    public static ViewInteraction onErrorViewWithinTextInputLayoutWithId(@IdRes int textInputLayoutId) {
        return onView(allOf(isDescendantOfA(withId(textInputLayoutId)), withId(R.id.textinput_error)));
    }

}
