package androidTestFiles.UI.bluetooth_transfer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.rule.ServiceTestRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.service.bluetooth.BluetoothTransferService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class BluetoothTransferTest {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Rule
    public final ServiceTestRule serviceRule = new ServiceTestRule();
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    }

    private void sendBluetoothServiceBroadcast(String message, String errorMsg) {

        Intent broadcastIntent = new Intent(BluetoothTransferService.BROADCAST_ACTION);
        broadcastIntent.putExtra(BluetoothTransferService.SERVICE_MESSAGE, message);
        broadcastIntent.putExtra(BluetoothTransferService.SERVICE_ERROR, errorMsg);
        context.sendBroadcast(broadcastIntent);
    }

    private void checkNotificationMessage(int stringId) {

        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        device.openNotification();
        device.wait(Until.hasObject(By.text(context.getString(stringId))), 1000);
        Assert.assertNotNull(device.findObject(By.text(context.getString(stringId))));
        device.pressBack();
    }

    @Test
    public void checkNotificationWhenTransferFails() throws Exception {

        serviceRule.startService(new Intent(context, BluetoothTransferService.class));
        sendBluetoothServiceBroadcast(BluetoothTransferService.MESSAGE_TRANSFER_FAIL, "");
        checkNotificationMessage(R.string.bluetooth_transfer_failure);
    }

    @Test
    public void checkNotificationWhenTransferComplete() throws Exception {

        serviceRule.startService(new Intent(context, BluetoothTransferService.class));
        sendBluetoothServiceBroadcast(BluetoothTransferService.MESSAGE_TRANSFER_COMPLETE, "");
        checkNotificationMessage(R.string.bluetooth_all_transfers_complete);
    }


    @Test
    public void checkNotificationWhenCommunicationClosed() throws Exception {

        serviceRule.startService(new Intent(context, BluetoothTransferService.class));
        sendBluetoothServiceBroadcast(BluetoothTransferService.MESSAGE_DISCONNECT, "");
        checkNotificationMessage(R.string.bluetooth_communication_closed);
    }

}
