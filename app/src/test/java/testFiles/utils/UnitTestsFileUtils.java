package testFiles.utils;

import org.digitalcampus.oppia.analytics.Analytics;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class UnitTestsFileUtils {

    public static String readFileFromTestResources(String path) {

        try (InputStream is = UnitTestsFileUtils.class.getClassLoader().getResourceAsStream(path)) {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            return new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Analytics.logException(e);
            return null;
        }
    }
}
