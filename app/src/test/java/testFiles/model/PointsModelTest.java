package testFiles.model;

import static org.junit.Assert.assertEquals;

import org.digitalcampus.oppia.model.Points;
import org.digitalcampus.oppia.utils.DateUtils;
import org.joda.time.DateTime;
import org.junit.Test;

public class PointsModelTest {

    @Test
    public void getAndSetTest(){

        DateTime dt = new DateTime(2020,9,30,20,11,0);

        Points p = new Points();
        p.setDateTime(DateUtils.DATETIME_FORMAT.print(dt));

        assertEquals(DateUtils.DATE_FORMAT_DAY_MONTH.print(dt), p.getDateDayMonth());
        assertEquals(DateUtils.TIME_FORMAT_HOURS_MINUTES.print(dt), p.getTimeHoursMinutes());

        p.setPointsAwarded(10);
        assertEquals(10, p.getPointsAwarded());

        p.setDescription("my desc");
        assertEquals("my desc", p.getDescription());
    }
}
