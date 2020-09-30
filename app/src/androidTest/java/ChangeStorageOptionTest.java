
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import androidx.preference.PreferenceManager;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.listener.MoveStorageListener;
import org.digitalcampus.oppia.task.ChangeStorageOptionTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.storage.ExternalStorageState;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.digitalcampus.oppia.utils.storage.StorageAccessStrategyFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

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
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        signal = new CountDownLatch(1);
    }

    @After
    public void tearDown() throws Exception {
        signal.countDown();
    }

    /*
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
    */



    //Is Storage Available Tests
    @Test
    public void fromInternalToExternal_storageNotAvailable_mediaRemoved() throws Exception {

        ExternalStorageState state = Mockito.mock(ExternalStorageState.class);  //Mock ExternalStorageState object
        ExternalStorageState.setExternalStorageState(state);    //Inject mocked object in ExternalStorageState class
        when(state.getExternalStorageState()).thenReturn(Environment.MEDIA_REMOVED);    //Provide mocked behaviour

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

        assertFalse(response.isResult());
        assertEquals(context.getString(R.string.error_sdcard), response.getResultResponse());

        ExternalStorageState.setExternalStorageState(new ExternalStorageState());   //Replace mocked object
    }
    @Test
    public void fromInternalToExternal_storageNotAvailable_mediaUnmountable() throws Exception {

        ExternalStorageState state = Mockito.mock(ExternalStorageState.class);  //Mock ExternalStorageState object
        ExternalStorageState.setExternalStorageState(state);    //Inject mocked object in ExternalStorageState class
        when(state.getExternalStorageState()).thenReturn(Environment.MEDIA_UNMOUNTABLE);    //Provide mocked behaviour

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

        assertFalse(response.isResult());
        assertEquals(context.getString(R.string.error_sdcard), response.getResultResponse());

        ExternalStorageState.setExternalStorageState(new ExternalStorageState());   //Replace mocked object
    }
    @Test
    public void fromInternalToExternal_storageNotAvailable_mediaUnmounted() throws Exception {

        ExternalStorageState state = Mockito.mock(ExternalStorageState.class);  //Mock ExternalStorageState object
        ExternalStorageState.setExternalStorageState(state);    //Inject mocked object in ExternalStorageState class
        when(state.getExternalStorageState()).thenReturn(Environment.MEDIA_UNMOUNTED);    //Provide mocked behaviour

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

        assertFalse(response.isResult());
        assertEquals(context.getString(R.string.error_sdcard), response.getResultResponse());

        ExternalStorageState.setExternalStorageState(new ExternalStorageState());   //Replace mocked object
    }
    @Test
    public void fromInternalToExternal_storageNotAvailable_mediaMountedReadOnly() throws Exception {

        ExternalStorageState state = Mockito.mock(ExternalStorageState.class);  //Mock ExternalStorageState object
        ExternalStorageState.setExternalStorageState(state);    //Inject mocked object in ExternalStorageState class
        when(state.getExternalStorageState()).thenReturn(Environment.MEDIA_MOUNTED_READ_ONLY);    //Provide mocked behaviour

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

        assertFalse(response.isResult());
        assertEquals(context.getString(R.string.error_sdcard), response.getResultResponse());

        ExternalStorageState.setExternalStorageState(new ExternalStorageState());   //Replace mocked object
    }
    @Test
    public void fromInternalToExternal_storageNotAvailable_mediaShared() throws Exception {

        ExternalStorageState state = Mockito.mock(ExternalStorageState.class);  //Mock ExternalStorageState object
        ExternalStorageState.setExternalStorageState(state);    //Inject mocked object in ExternalStorageState class
        when(state.getExternalStorageState()).thenReturn(Environment.MEDIA_SHARED);    //Provide mocked behaviour

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

        assertFalse(response.isResult());
        assertEquals(context.getString(R.string.error_sdcard), response.getResultResponse());

        ExternalStorageState.setExternalStorageState(new ExternalStorageState());   //Replace mocked object
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
