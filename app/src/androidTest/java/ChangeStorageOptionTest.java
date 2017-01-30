import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.di.AppComponent;
import org.digitalcampus.oppia.di.AppModule;
import org.digitalcampus.oppia.listener.MoveStorageListener;
import org.digitalcampus.oppia.task.ChangeStorageOptionTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.storage.ExternalStorageState;
import org.digitalcampus.oppia.utils.storage.ExternalStorageStrategy;
import org.digitalcampus.oppia.utils.storage.InternalStorageStrategy;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.digitalcampus.oppia.utils.storage.StorageAccessStrategyFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import it.cosenonjaviste.daggermock.DaggerMockRule;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class ChangeStorageOptionTest {

    private Context context;
    private SharedPreferences prefs;
    private CountDownLatch signal;
    private Payload response;

    @Mock ExternalStorageState externalStorageState;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getTargetContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        signal = new CountDownLatch(1);
    }

    @After
    public void tearDown() throws Exception {
        signal.countDown();
    }

    @Test
    public void fromInternalToExternal_success() throws Exception {

        Storage.setStorageStrategy(StorageAccessStrategyFactory.createStrategy(PrefsActivity.STORAGE_OPTION_INTERNAL));
        Storage.createFolderStructure(context);

        ChangeStorageOptionTask task = new ChangeStorageOptionTask(context);

        ArrayList<Object> data = new ArrayList<>();
        String storageOption = PrefsActivity.STORAGE_OPTION_EXTERNAL;
        data.add(storageOption);
        Payload p = new Payload(data);
        task.setMoveStorageListener(new MoveStorageListener() {
            @Override
            public void moveStorageComplete(Payload p) {
                response = p;
                signal.countDown();
            }

            @Override
            public void moveStorageProgressUpdate(String s) {

            }
        });
        task.execute(p);

        signal.await();

        assertTrue(response.isResult());
        assertEquals(PrefsActivity.STORAGE_OPTION_EXTERNAL, prefs.getString(PrefsActivity.PREF_STORAGE_OPTION, ""));
    }

    @Test
    public void fromInternalToExternal_storageNotAvailable() throws Exception {

        when(externalStorageState.getExternalStorageState()).thenReturn(Environment.MEDIA_REMOVED);

        Storage.setStorageStrategy(StorageAccessStrategyFactory.createStrategy(PrefsActivity.STORAGE_OPTION_INTERNAL));
        Storage.createFolderStructure(context);

        ChangeStorageOptionTask task = new ChangeStorageOptionTask(context);

        ArrayList<Object> data = new ArrayList<>();
        String storageOption = PrefsActivity.STORAGE_OPTION_EXTERNAL;
        data.add(storageOption);
        Payload p = new Payload(data);
        task.setMoveStorageListener(new MoveStorageListener() {
            @Override
            public void moveStorageComplete(Payload p) {
                response = p;
                signal.countDown();
            }

            @Override
            public void moveStorageProgressUpdate(String s) {

            }
        });
        task.execute(p);

        signal.await();

        assertTrue(response.isResult());
        assertEquals(PrefsActivity.STORAGE_OPTION_EXTERNAL, prefs.getString(PrefsActivity.PREF_STORAGE_OPTION, ""));
    }

    @Test
    public void fromExternalToExternal_moveFilesToSameLocationFailure() throws Exception {

        Storage.setStorageStrategy(StorageAccessStrategyFactory.createStrategy(PrefsActivity.STORAGE_OPTION_EXTERNAL));
        Storage.createFolderStructure(context);

        ChangeStorageOptionTask task = new ChangeStorageOptionTask(context);

        ArrayList<Object> data = new ArrayList<>();
        String storageOption = PrefsActivity.STORAGE_OPTION_EXTERNAL;
        data.add(storageOption);
        Payload p = new Payload(data);
        task.setMoveStorageListener(new MoveStorageListener() {
            @Override
            public void moveStorageComplete(Payload p) {
                response = p;
                signal.countDown();
            }

            @Override
            public void moveStorageProgressUpdate(String s) {

            }
        });
        task.execute(p);

        signal.await();

        //Expected to fail when moving to the same location
        assertFalse(response.isResult());
        assertEquals(PrefsActivity.STORAGE_OPTION_EXTERNAL, prefs.getString(PrefsActivity.PREF_STORAGE_OPTION, ""));
    }

}
