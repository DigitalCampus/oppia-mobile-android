package org.digitalcampus.oppia.model;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.learning.R;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class QuizAttemptModel {

    private Context context;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    }

    @Test
    public void getAndSetTest(){
        DateTime dt = new DateTime(2020,9,29,9,0,0);
        String dateTime = "2020-09-29 09:00:00";

        QuizAttempt qa = new QuizAttempt();

        qa.setSent(false);
        assertEquals(false, qa.isSent());

        qa.setDatetime(dt);
        assertEquals(dt, qa.getDatetime());
        assertEquals("2020-09-29 09:00:00", qa.getDateTimeString());

        qa.setDateTimeFromString(dateTime);
        assertEquals("2020-09-29 09:00:00", qa.getDateTimeString());

        qa.setId(12345);
        assertEquals(12345, qa.getId());

        qa.setMaxscore(11);
        qa.setScore(7);
        assertEquals(7, qa.getScore(),0);
        assertEquals(63.63, qa.getScoreAsPercent(),0.007);
        assertEquals("64%", qa.getScorePercentLabel());

        User u = new User();
        u.setUserId(1);
        u.setUsername("me");

        qa.setUser(u);
        assertEquals(u, qa.getUser());

        qa.setTimetaken(17);
        assertEquals("0 min 17s", qa.getHumanTimetaken());

        qa.setTimetaken(73);
        assertEquals("1 min 13s", qa.getHumanTimetaken());

        qa.setTimetaken(3673);
        assertEquals("61 min 13s", qa.getHumanTimetaken());


        assertEquals(context.getString(R.string.quiz_attempts_unknown_quiz), qa.getDisplayTitle(context));

        qa.setCourseTitle("my course");
        qa.setSectionTitle("my section");
        qa.setActivityDigest("act1234");
        qa.setQuizTitle("my quiz");

        assertEquals("my course", qa.getCourseTitle());
        assertEquals("my quiz", qa.getQuizTitle());
        assertEquals("my section", qa.getSectionTitle());
        assertEquals("act1234", qa.getActivityDigest());

        assertEquals("my section > my quiz", qa.getDisplayTitle(context));

        qa.setSectionTitle(null);
        assertEquals(context.getString(R.string.quiz_attempts_unknown_quiz), qa.getDisplayTitle(context));

        qa.setSectionTitle("my section");
        qa.setQuizTitle(null);
        assertEquals(context.getString(R.string.quiz_attempts_unknown_quiz), qa.getDisplayTitle(context));

    }

    @Test
    public void asJSONCollectionStringTest(){
        Collection<QuizAttempt> c = new ArrayList<>();
        QuizAttempt qa1 = new QuizAttempt();
        qa1.setData("data1");
        c.add(qa1);

        QuizAttempt qa2 = new QuizAttempt();
        qa2.setData("data2");
        c.add(qa2);

        assertEquals("[data1,data2]", QuizAttempt.asJSONCollectionString(c));
    }
}
