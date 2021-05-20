package org.digitalcampus.oppia.analytics;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

public class DefaultNoOpAnalytics extends BaseAnalytics {

    protected static final String TAG = DefaultNoOpAnalytics.class.getSimpleName();

    public DefaultNoOpAnalytics(Context ctx){
        super(ctx);
    }

    @Override
    protected void startTrackingSession() {
        Log.d(TAG, "Starting tracking session");
    }

    @Override
    protected void stopTrackingSession() {
        Log.d(TAG, "Stop tracking session");
    }

    @Override
    public void setUserIdentifier(String username) {
        Log.d(TAG, "Set tracking session user: " + username);
    }

    @Override //no-op, does nothing
    protected void trackingConfigChanged() { }
    @Override //no-op, does nothing
    public void trackViewOnStart(Activity activity) { }
    @Override //no-op, does nothing
    public void trackViewOnStop(Activity activity) { }
    @Override //no-op, does nothing
    protected void logHandledException(Exception e) { }
}
