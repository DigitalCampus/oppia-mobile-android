package androidTestFiles.org.digitalcampus.oppia.model;

import android.Manifest;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.digitalcampus.oppia.model.Points;
import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class PointsModelTest {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Test
    public void getAndSetTest(){

        DateTime dt = new DateTime(2020,9,30,20,11,0);

        Points p = new Points();
        p.setDateTime("2020-09-30 20:11:00");

        assertEquals("30 Sep", p.getDateDayMonth());
        assertEquals("20:11", p.getTimeHoursMinutes());

        p.setPointsAwarded(10);
        assertEquals(10, p.getPointsAwarded());

        p.setDescription("my desc");
        assertEquals("my desc", p.getDescription());
    }
}
