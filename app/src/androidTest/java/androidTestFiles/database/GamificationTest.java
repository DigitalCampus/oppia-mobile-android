package androidTestFiles.database;

import android.Manifest;

import org.junit.Rule;

import androidx.test.rule.GrantPermissionRule;

//@RunWith(AndroidJUnit4.class)
public class GamificationTest /*extends BaseTestDB*/ {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    // TODO getUserPoints - with the gamifcation options
    // TODO getLeaderboardList
}
