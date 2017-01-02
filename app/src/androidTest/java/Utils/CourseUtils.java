package Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.CompleteCourse;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.MultiLangInfo;
import org.digitalcampus.oppia.model.Section;
import org.digitalcampus.oppia.utils.storage.*;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.digitalcampus.oppia.utils.xmlreaders.CourseXMLReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import static org.apache.commons.io.FileUtils.cleanDirectory;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CourseUtils {

    public static Course getCourseFromDatabase(Context ctx, String shortTitle){
        DbHelper db = DbHelper.getInstance(ctx);
        long courseId = db.getCourseID(shortTitle);
        long userId = db.getUserId(SessionManager.getUsername(ctx));
        return db.getCourse(courseId, userId);
    }

    public static void cleanUp(){
        //Clean course folders and database
        try {

            Context ctx = InstrumentationRegistry.getTargetContext();

            //Clean downloads folder
            File downloadsFolder = new File(Storage.getDownloadPath(ctx));
            if(downloadsFolder.exists()) {
                cleanDirectory(downloadsFolder);
            }

            //Clean courses folder
            File coursesFolder = new File(Storage.getCoursesPath(ctx));
            if(coursesFolder.exists()) {
                cleanDirectory(coursesFolder);
            }

            //Clean temp folder
            File tempFolder = new File(Storage.getStorageLocationRoot(ctx) + "temp/");
            if (tempFolder.exists()) {
                cleanDirectory(tempFolder);
            }

            //Clean database
            DbHelper db = DbHelper.getInstance(ctx);
            for(Course c : db.getAllCourses()){
                db.deleteCourse(c.getCourseId());
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Course createMockCourse(){

        Course mockCourse = new Course("");

        mockCourse.setShortname("Mock Course");


        Section mockSection = mock(Section.class);


        Activity mockActivity = mock(Activity.class);
        when(mockActivity.getCourseId()).thenReturn((long) 999);
        when(mockActivity.getActId()).thenReturn(999);
        when(mockActivity.getSectionId()).thenReturn(999);

        mockSection.addActivity(mockActivity);


        return mockCourse;

    }

    public static CompleteCourse createMockCompleteCourse(int numberOfSections, int numberOfActivities){
        ArrayList<Section> sections = new ArrayList<>();
        ArrayList<Activity> activities = new ArrayList<>();

        //Add activities
        for(int i = 0; i < numberOfActivities; i++){
            final int n = i;
            MultiLangInfo mli = new MultiLangInfo();
            mli.setTitles(new ArrayList<Lang>(){{ add(new Lang("en", "Activity " + n)); }});

            Activity activity = new Activity();
            activity.setMultiLangInfo(mli);
            activity.setDigest("");
            activity.setActType("ActType");
            activities.add(activity);
        }

        //Add sections
        for(int i = 0; i < numberOfSections; i++){
            final int n = i;
            MultiLangInfo mli = new MultiLangInfo();
            mli.setTitles(new ArrayList<Lang>(){{ add(new Lang("en", "Section " + n)); }});

            Section section = new Section();
            section.setMultiLangInfo(mli);
            section.setActivities(activities);
            sections.add(section);

        }

        MultiLangInfo mli = new MultiLangInfo();
        mli.setTitles(new ArrayList<Lang>(){{ add(new Lang("en", "Mock Course")); }});


        CompleteCourse completeCourse = new CompleteCourse();
        completeCourse.setShortname("Mock Course");
        completeCourse.setMultiLangInfo(mli);
        completeCourse.setSections(sections);

        return completeCourse;

    }
}
