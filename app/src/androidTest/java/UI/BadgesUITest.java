package UI;


import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.fragments.BadgesFragment;
import org.digitalcampus.oppia.model.Activity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;



import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;

@RunWith(AndroidJUnit4.class)
public class BadgesUITest {


    private Activity act;
    private Bundle args;

    @Before
    public void setup() throws Exception {
        // Setting up before every test
        act = new Activity();
    }

    @Test
    public void displayBadges() {
        launchInContainer(BadgesFragment.class, args, R.style.Oppia_ToolbarTheme, null);
    }
}
