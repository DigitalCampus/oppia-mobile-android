package testFiles.model;

import static org.junit.Assert.assertEquals;

import org.digitalcampus.oppia.model.ActivityCount;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

public class ActivityCountModelTest {

    @Test
    public void getAndSetTest(){

        ActivityCount ac = new ActivityCount();

        Map<String, Integer> typeCount = new LinkedHashMap<>();
        typeCount.put("mykey", 123);
        typeCount.put("another", 999);

        ac.setTypeCount(typeCount);

        assertEquals(123, ac.getValueForType("mykey"));
        assertEquals(0, ac.getValueForType("invalidkey"));
    }
}
