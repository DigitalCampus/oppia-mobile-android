package androidTestFiles.utils;


import android.os.IBinder;

import androidx.test.espresso.Root;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class WindowManagerLayoutParamTypeMatcher extends TypeSafeMatcher<Root> {
    private final String description;
    private final int type;
    private final boolean expectedWindowTokenMatch;
    public WindowManagerLayoutParamTypeMatcher(String description, int type) {
        this(description, type, true);
    }
    public WindowManagerLayoutParamTypeMatcher(String description, int type, boolean expectedWindowTokenMatch) {
        this.description = description;
        this.type = type;
        this.expectedWindowTokenMatch = expectedWindowTokenMatch;
    }
    @Override public void describeTo(Description description) {
        description.appendText(this.description);
    }
    @Override public boolean matchesSafely(Root root) {
        if (type == root.getWindowLayoutParams().get().type) {
            IBinder windowToken = root.getDecorView().getWindowToken();
            IBinder appToken = root.getDecorView().getApplicationWindowToken();
            if (windowToken == appToken == expectedWindowTokenMatch) {
                // windowToken == appToken means this window isn't contained by any other windows.
                // if it was a window for an activity, it would have TYPE_BASE_APPLICATION.
                return true;
            }
        }
        return false;
    }
}
