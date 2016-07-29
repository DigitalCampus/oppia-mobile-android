import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.digitalcampus.oppia.utils.storage.Storage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import Utils.FileUtils;

@RunWith(AndroidJUnit4.class)
public class InstallDownloadedCoursesTest {
    private final String CORRECT_COURSE = "Wash.zip";

    private Context context;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void installCourse_correctCourse()throws Exception{
        FileUtils.copyZipFromAssets(InstrumentationRegistry.getContext(), CORRECT_COURSE);
    }


}
