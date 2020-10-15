package database;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.oppia.database.DbHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class GeneralTest {

    private Context context;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    }
    @Test
    public void resetAndUpgrade() {

        DbHelper dbHelper = DbHelper.getInstance(context);

        for (int i=7; i<47; i++){
            dbHelper.resetDatabase();
            dbHelper.onUpgrade(dbHelper.getDB(), 6, i);
        }

        dbHelper.resetDatabase();
        dbHelper.onUpgrade(dbHelper.getDB(), 6, 7);
    }
}
