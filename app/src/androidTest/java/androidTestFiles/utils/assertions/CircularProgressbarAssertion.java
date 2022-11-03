package androidTestFiles.utils.assertions;

import static org.junit.Assert.assertEquals;

import androidx.test.espresso.ViewAssertion;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

public class CircularProgressbarAssertion {

    public static ViewAssertion withProgress(float progress) {
        return (view, noViewFoundException) -> {
            if (noViewFoundException != null) {
                throw noViewFoundException;
            }

            float viewProgress = ((CircularProgressBar) view).getProgress();
            assertEquals(progress, viewProgress, 0.1f);
        };
    }
}
