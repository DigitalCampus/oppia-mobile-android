package androidTestFiles.database;

import android.Manifest;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.digitalcampus.oppia.model.SearchResult;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class SearchTest extends BaseTestDB {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    // searchWithTerm
    @Test
    public void searchWithTerm() {

        // TODO add some real data to test with
        ArrayList<SearchResult> searchResults = (ArrayList<SearchResult>)
                getDbHelper().search("test", 50, 1, getContext());
        assertEquals(0, searchResults.size());
    }

    // TODO deleteSearchIndex
    // TODO searchIndexRemoveCourse
    // TODO insertActivityIntoSearchTable
}
