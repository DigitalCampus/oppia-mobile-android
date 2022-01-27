package androidTestFiles.database;

import android.Manifest;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class GeneralTest extends BaseTestDB {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

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
