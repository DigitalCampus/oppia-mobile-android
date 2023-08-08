package androidTestFiles.utils.matchers;

import android.text.TextUtils;
import android.view.View;
import android.widget.Spinner;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class SpinnerMatcher {

    public static Matcher<View> withSpinnerSelectedItemText(String text) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("spinner first item text");
            }

            @Override
            protected boolean matchesSafely(View item) {
                Spinner spinner = (Spinner) item;
                String itemText = spinner.getSelectedItem().toString();
                return TextUtils.equals(text, itemText);
            }
        };
    }
}
