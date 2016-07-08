package org.digitalcampus.oppia.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class GooglePlayUtils {

    public interface DialogListener{
        void onErrorDialogClosed();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public static boolean checkPlayServices(Activity act, final DialogListener listener) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int status = apiAvailability.isGooglePlayServicesAvailable(act);
        if (status != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(status)) {
                Dialog dialog = apiAvailability.getErrorDialog(act, status, 0);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        listener.onErrorDialogClosed();
                    }
                });
                dialog.show();
            } else {
                Toast.makeText(act, "This device is not supported.", Toast.LENGTH_LONG).show();
                listener.onErrorDialogClosed();
            }
            return false;
        }
        else{
            return true;
        }
    }
}
