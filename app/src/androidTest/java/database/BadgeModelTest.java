package database;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.oppia.model.Badge;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class BadgeModelTest {

    private Context context;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test
    public void setAndGetTest() {
        Badge b = new Badge();
        b.setDateTime("2020-09-28 16:00:00");
        b.setDescription("badge desc");
        b.setIcon("myicon.jpg");

        assertEquals("myicon.jpg", b.getIcon());
        assertEquals("badge desc", b.getDescription());
        assertEquals("2020-09-28", b.getDateAsString());

    }
}
