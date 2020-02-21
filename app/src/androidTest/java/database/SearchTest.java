package database;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.listener.DBListener;
import org.digitalcampus.oppia.model.SearchResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class SearchTest {

    private Context context;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    }


    // searchWithTerm
    @Test
    public void searchWithTerm() {

        DbHelper dbHelper = DbHelper.getInstance(context);
        dbHelper.getReadableDatabase(); // To force migration if needed

        DBListener dbListener = new DBListener() {
            @Override
            public void onQueryPerformed() {
            }
        };
        // TODO add some real data to test with
        ArrayList<SearchResult> searchResults = (ArrayList<SearchResult>)
                dbHelper.search("test", 50, 1, context, dbListener);
        assertEquals(0, searchResults.size());
    }

    // TODO deleteSearchIndex
    // TODO searchIndexRemoveCourse
    // TODO insertActivityIntoSearchTable
}
