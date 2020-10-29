package database;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.GamificationEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class GamificationTest {

    private Context context;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    }

    @Test
    public void insertActivityGamification() {

        DbHelper dbHelper = DbHelper.getInstance(context);
        List<GamificationEvent> gamificationEvents = new ArrayList<>();
        GamificationEvent ge = new GamificationEvent();
        ge.setEvent("completed");
        ge.setCompleted(true);
        ge.setPoints(10);
        gamificationEvents.add(ge);

        dbHelper.insertActivityGamification(123, gamificationEvents);
    }

    @Test
    public void insertCourseGamification() {

        DbHelper dbHelper = DbHelper.getInstance(context);
        List<GamificationEvent> gamificationEvents = new ArrayList<>();
        GamificationEvent ge = new GamificationEvent();
        ge.setEvent("downloaded");
        ge.setCompleted(true);
        ge.setPoints(50);
        gamificationEvents.add(ge);

        dbHelper.insertCourseGamification(1234, gamificationEvents);
    }
    // TODO getUserPoints - with the gamifcation options
    // TODO getLeaderboardList
}
