package database;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.oppia.database.DbHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class GeneralTest extends BaseDBTests{

    @Test
    public void resetAndUpgrade() {

        for (int i=7; i<47; i++){
            getDbHelper().resetDatabase();
            getDbHelper().onUpgrade(getDbHelper().getDB(), 6, i);
        }

        getDbHelper().resetDatabase();
        getDbHelper().onUpgrade(getDbHelper().getDB(), 6, 7);
    }
}
