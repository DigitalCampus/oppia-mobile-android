package androidTestFiles.utils;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;

import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.Root;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.util.TreeIterables;

import com.google.android.material.tabs.TabLayout;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

public class UITestActionsUtils {

    public static void clickRecyclerViewPosition(int recyclerViewId, int position) {
        waitForView(ViewMatchers.withId(recyclerViewId))
                .perform(RecyclerViewActions.actionOnItemAtPosition(position, click()));
    }

    public static void clickViewWithText(int stringId) {
        waitForView(withText(stringId)).perform(click());
    }

    public static void clickViewWithText(String text) {
        waitForView(withText(text)).perform(click());
    }


    @NonNull
    public static ViewAction selectTabAtPosition(final int position) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return allOf(isDisplayed(), isAssignableFrom(TabLayout.class));
            }

            @Override
            public String getDescription() {
                return "with tab at index" + position;
            }

            @Override
            public void perform(UiController uiController, View view) {
                if (view instanceof TabLayout) {
                    TabLayout tabLayout = (TabLayout) view;
                    TabLayout.Tab tab = tabLayout.getTabAt(position);

                    if (tab != null) {
                        tab.select();
                    }
                }
            }
        };
    }

    public static Matcher<Root> isToast() {
        return new WindowManagerLayoutParamTypeMatcher("is toast", WindowManager.LayoutParams.TYPE_TOAST);
    }

    private static ViewAction searchForView(final Matcher<View> viewMatcher) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "search for view with " + viewMatcher.toString() + " in the root view";
            }

            @Override
            public void perform(UiController uiController, View view) {
                Iterable<View> iterable = TreeIterables.breadthFirstViewTraversal(view);
                for (View child : iterable) {
                    if (viewMatcher.matches(child)) {
                        return;
                    }
                }

                throw new NoMatchingViewException.Builder()
                        .withRootView(view)
                        .withViewMatcher(viewMatcher)
                        .build();
            }
        };
    }


    /**
     * [waitForView] tries to find a view with given [viewMatchers]. If found, it returns the
     * [ViewInteraction] for given [viewMatchers]. If not found, it waits for given [wait]
     * before attempting to find the view again. It retries for given number of [retries].
     *
     * Adaptation of the [StackOverflow post by manbradcalf](https://stackoverflow.com/a/56499223/2410641)
     */
    public static ViewInteraction waitForView(final Matcher<View> viewMatcher) {
        int retries = 5;
        long wait = 1000L;
        for (int i = 0; i < retries; i++) {
            try {
                onView(isRoot()).perform(searchForView(viewMatcher));
                break;
            } catch (NoMatchingViewException e) {
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
        }

        return onView(viewMatcher);
    }

}
