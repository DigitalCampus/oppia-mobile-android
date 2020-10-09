package database;

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.User;

import java.util.ArrayList;

public class TestData {

    DbHelper db;
    public void load(DbHelper dbHelper){
        this.db = dbHelper;
        this.addUsers();
        this.addCourses();
    }

    // add users
    private void addUsers(){
        User u = new User();
        u.setUserId(1);
        u.setPassword("password");
        u.setUsername("user1");
        u.setFirstname("User");
        u.setLastname("One");
        db.addOrUpdateUser(u);

        u = new User();
        u.setUserId(2);
        u.setPassword("password");
        u.setUsername("user2");
        u.setFirstname("User");
        u.setLastname("Two");
        db.addOrUpdateUser(u);
    }

    // courses
    private void addCourses(){
        Course c = new Course();
        c.setCourseId(100);
        c.setShortname("my-course");
        Lang l = new Lang("en", "course title");
        ArrayList langList = new ArrayList<>();
        langList.add(l);
        c.setTitles(langList);
        db.addOrUpdateCourse(c);
    }



    // activities/sections


    // quizzes

    // trackers


    // quiz attempts

}
