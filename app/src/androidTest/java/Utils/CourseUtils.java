package Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.utils.storage.*;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.digitalcampus.oppia.utils.xmlreaders.CourseXMLReader;

import java.io.File;
import java.util.Locale;

public class CourseUtils {

    public static String getCourseTitle(Context ctx){

        File dir = new File(Storage.getDownloadPath(ctx));
        String[] children = dir.list();

        String courseXMLPath = "";
        CourseXMLReader cxr = null;
        File tempdir = new File(Storage.getStorageLocationRoot(ctx) + "temp/");
        tempdir.mkdirs();

        boolean unzipResult = org.digitalcampus.oppia.utils.storage.FileUtils.unzipFiles(Storage.getDownloadPath(ctx), children[0], tempdir.getAbsolutePath());


        if (!unzipResult){
            //then was invalid zip file and should be removed
            FileUtils.cleanUp(tempdir, Storage.getDownloadPath(ctx) + children[0]);

        }

        String[] courseDirs = tempdir.list();
        Course c = null;
        SharedPreferences prefs = null;

        try {
            courseXMLPath = tempdir + File.separator + courseDirs[0] + File.separator + MobileLearning.COURSE_XML;
            cxr = new CourseXMLReader(courseXMLPath, 0, ctx);

            prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
            c = new Course(prefs.getString(PrefsActivity.PREF_STORAGE_LOCATION, ""));
            c.setTitles(cxr.getTitles());

        } catch (ArrayIndexOutOfBoundsException aioobe){
            org.digitalcampus.oppia.utils.storage.FileUtils.cleanUp(tempdir, Storage.getDownloadPath(ctx) + children[0]);
            return null;
        } catch (InvalidXMLException e) {
            e.printStackTrace();
            return null;
        } catch(Exception e ){
            e.printStackTrace();
            return null;
        }

        return c.getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));
    }

}
