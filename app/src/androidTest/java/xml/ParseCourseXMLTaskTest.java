package xml;

import android.content.Context;
import android.content.SharedPreferences;

import org.digitalcampus.oppia.adapter.CourseInstallViewAdapter;
import org.digitalcampus.oppia.api.MockApiEndpoint;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.model.CompleteCourse;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.task.CourseInfoTask;
import org.digitalcampus.oppia.task.ParseCourseXMLTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.digitalcampus.oppia.utils.xmlreaders.CourseXMLReader;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import Utils.FileUtils;
import androidx.preference.PreferenceManager;
import androidx.test.platform.app.InstrumentationRegistry;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ParseCourseXMLTaskTest {

    private Context context;
    private SharedPreferences prefs;
    private CompleteCourse resultCourse;
    private boolean success;

    private static final String ASSETS_FOLDER = "course_xmls";
    private static final String CORRECT_XML = "correct_course.xml";
    private static final String INCORRECT_XML = "incorrect_xml.xml";

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    private String copyCourseFile(String shortname, String filename){
        String destination = Storage.getStorageLocationRoot(context) +  File.separator + Storage.APP_COURSES_DIR_NAME + File.separator + shortname + File.separator;
        FileUtils.copyFileFromAssets(context, ASSETS_FOLDER, filename, new File(destination), App.COURSE_XML);
        return destination + filename;
    }


    private void parseCourseSync(Course course){
        final CountDownLatch signal = new CountDownLatch(1);  //Control AsyncTask sincronization for testing

        ParseCourseXMLTask task = new ParseCourseXMLTask(context);
        task.setListener(new ParseCourseXMLTask.OnParseXmlListener() {
            @Override
            public void onParseComplete(CompleteCourse parsedCourse) {
                success = true;
                resultCourse = parsedCourse;
                signal.countDown();
            }

            @Override
            public void onParseError() {
                success = false;
                signal.countDown();
            }
        });
        task.execute(course);

        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void parseCourse_CompleteCorrect() throws Exception{

        Course course = new Course(Storage.getStorageLocationRoot(context));
        course.setShortname("test");
        copyCourseFile("test", CORRECT_XML);
        parseCourseSync(course);
        assertTrue(success);
        assertEquals("Reference course", resultCourse.getTitle("en"));
        assertEquals(20150202182552d, resultCourse.getVersionId());
    }

    @Test
    public void parseCourse_invalidXML() throws Exception{

        Course course = new Course(Storage.getStorageLocationRoot(context));
        course.setShortname("test");
        copyCourseFile("test", INCORRECT_XML);
        parseCourseSync(course);
        assertFalse(success);
    }

    @Test
    public void parseCourse_nonExistingXML() throws Exception{

        Course course = new Course(Storage.getStorageLocationRoot(context));
        course.setShortname("nonexist");
        parseCourseSync(course);
        assertFalse(success);
    }


}
