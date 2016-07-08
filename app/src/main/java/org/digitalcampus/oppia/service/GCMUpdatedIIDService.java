package org.digitalcampus.oppia.service;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.iid.InstanceIDListenerService;

public class GCMUpdatedIIDService extends InstanceIDListenerService {

    public static final String TAG = GCMUpdatedIIDService.class.getSimpleName();

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    @Override
    public void onTokenRefresh() {

        Log.d(TAG, "InstanceID token updated. Calling Intent to refresh on server");

        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        Intent intent = new Intent(this, GCMRegistrationService.class);
        startService(intent);
    }
}
