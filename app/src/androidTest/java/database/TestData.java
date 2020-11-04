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
    }


}
