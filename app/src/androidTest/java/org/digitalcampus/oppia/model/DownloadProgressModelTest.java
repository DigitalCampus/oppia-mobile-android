package org.digitalcampus.oppia.model;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class DownloadProgressModelTest {

    @Test
    public void getAndSetTest(){
        DownloadProgress dp = new DownloadProgress();
        dp.setMessage("my msg");
        assertEquals("my msg", dp.getMessage());
    }
}
