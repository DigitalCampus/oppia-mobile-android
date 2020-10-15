package org.digitalcampus.oppia.model;
import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.oppia.database.DbHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class CoursesRepositoryModelTest {
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test
    public void getByDigestTest(){
        DbHelper dbHelper = DbHelper.getInstance(context);

        Activity a = new Activity();
        a.setDigest("abcd");

        List<Activity> aList = new ArrayList<>();
        aList.add(a);
        dbHelper.insertActivities(aList);
        CoursesRepository cr = new CoursesRepository();
        assertEquals("abcd", cr.getActivityByDigest(context, "abcd").getDigest());
        assertEquals(null, cr.getActivityByDigest(context, "notfound"));
    }

    @Test
    public void getCourseByShortnameValid(){

        DbHelper dbHelper = DbHelper.getInstance(context);

        Course c = new Course();
        c.setShortname("mycourse");

        dbHelper.addOrUpdateCourse(c);

        CoursesRepository cr = new CoursesRepository();
        assertEquals("mycourse", cr.getCourseByShortname(context, "mycourse", 1).getShortname());
    }

    @Test
    public void getCourseByShortnameInvalid(){

        DbHelper dbHelper = DbHelper.getInstance(context);

        Course c = new Course();
        c.setShortname("mycourse");

        dbHelper.addOrUpdateCourse(c);

        CoursesRepository cr = new CoursesRepository();
        assertEquals(null, cr.getCourseByShortname(context, "nocourse", 1));
    }

    @Test
    public void getCourseInvalid(){

        DbHelper dbHelper = DbHelper.getInstance(context);

        Course c = new Course();
        c.setCourseId(999);
        c.setShortname("mycourse");

        dbHelper.addOrUpdateCourse(c);

        CoursesRepository cr = new CoursesRepository();
        assertEquals(null, cr.getCourse(context, 999, 1));
    }
}
