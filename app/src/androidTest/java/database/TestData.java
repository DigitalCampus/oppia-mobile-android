package database;

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.User;

public class TestData {

    DbHelper db;
    public void load(DbHelper dbHelper){
        this.db = dbHelper;
        this.addUsers();
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

    // course 1

    // course 2


    // activities/sections


    // quizzes

    // trackers


    // quiz attempts

}
