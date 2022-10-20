package testFiles.model;

import static org.junit.Assert.assertEquals;

import org.digitalcampus.oppia.model.TrackerLog;
import org.junit.Test;

public class TrackerLogModelTest {

    @Test
    public void getAndSetTest(){
        TrackerLog tl = new TrackerLog();
        tl.setId(123456);
        assertEquals(123456, tl.getId());
    }
}
