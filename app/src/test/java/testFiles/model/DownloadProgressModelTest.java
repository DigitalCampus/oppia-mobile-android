package testFiles.model;

import static org.junit.Assert.assertEquals;

import org.digitalcampus.oppia.model.DownloadProgress;
import org.junit.Test;

public class DownloadProgressModelTest {

    @Test
    public void getAndSetTest(){
        DownloadProgress dp = new DownloadProgress();
        dp.setMessage("my msg");
        assertEquals("my msg", dp.getMessage());
    }
}
