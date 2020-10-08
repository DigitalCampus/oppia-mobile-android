package org.digitalcampus.oppia.model;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class QuizStatsModelTest {

    @Test
    public void simpleName(){
        assertEquals("QuizStats", QuizStats.TAG);
    }

    @Test
    public void getAndSet(){
        QuizStats qs = new QuizStats();
        qs.setAttempted(false);
        qs.setAverageScore(4);
        qs.setDigest("abcd");
        qs.setMaxScore(7);
        qs.setAttempted(false);
        qs.setNumAttempts(5);
        qs.setPassed(false);
        qs.setQuizTitle("my quiz");
        qs.setSectionTitle("my section");
        qs.setUserScore(3);

        assertEquals(57, qs.getAveragePercent());
        assertEquals("abcd", qs.getDigest());
        assertEquals(7, qs.getMaxScore(),0);
        assertEquals(false, qs.isAttempted());
        assertEquals(false, qs.isPassed());
        assertEquals(43, qs.getPercent());
        assertEquals(5, qs.getNumAttempts());
        assertEquals("my quiz", qs.getQuizTitle());
        assertEquals("my section", qs.getSectionTitle());
        assertEquals(3, qs.getUserScore(),0);
    }


}
