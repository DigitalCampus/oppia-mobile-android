package Utils;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.CompleteCourse;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.Section;
import org.digitalcampus.oppia.utils.storage.*;
import org.digitalcampus.oppia.utils.storage.FileUtils;

import java.io.File;
import java.util.ArrayList;

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

        Context ctx = InstrumentationRegistry.getTargetContext();

        File root = new File(Storage.getStorageLocationRoot(ctx));
        FileUtils.cleanDir(root);

        Storage.createFolderStructure(ctx);

        //Clean temp folder
        File tempFolder = new File(Storage.getStorageLocationRoot(ctx) + "temp/");
        if (tempFolder.exists()) {
            FileUtils.deleteDir(tempFolder);
        }

        //Clean database
        DbHelper db = DbHelper.getInstance(ctx);
        for(Course c : db.getAllCourses()){
            db.deleteCourse(c.getCourseId());
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

            Activity activity = new Activity();
            activity.setTitles(new ArrayList<Lang>(){{ add(new Lang("en", "Activity " + n)); }});
            activity.setDigest("");
            activity.setActType("ActType");
            activities.add(activity);
        }

        //Add sections
        for(int i = 0; i < numberOfSections; i++){
            final int n = i;

            Section section = new Section();
            section.setTitles(new ArrayList<Lang>(){{ add(new Lang("en", "Section " + n)); }});
            section.setActivities(activities);
            sections.add(section);

        }

        CompleteCourse completeCourse = new CompleteCourse();
        completeCourse.setShortname("Mock Course");
        completeCourse.setTitles(new ArrayList<Lang>(){{ add(new Lang("en", "Mock Course")); }});
        completeCourse.setSections(sections);

        return completeCourse;

    }
}
