package org.digitalcampus.oppia.analytics;

import android.app.Activity;
import android.content.Context;

import com.splunk.mint.Mint;

import org.digitalcampus.oppia.application.App;

public class MintAnalytics extends BaseAnalytics {

    public MintAnalytics(Context ctx){
        super(ctx);
    }

    @Override
    protected void startTrackingSession() {
        Mint.disableNetworkMonitoring();
        if (!com.splunk.mint.Properties.isPluginInitialized()){
            Mint.initAndStartSession(ctx, App.MINT_API_KEY);
        }
    }

    @Override
    protected void stopTrackingSession() {
        Mint.flush();
        Mint.closeSession(ctx);
    }

    @Override
    protected void trackingConfigChanged() {

    }

    @Override
    public void setUserIdentifier(String username) {
        Mint.setUserIdentifier(username.equals("") ? "anon" : username);
    }

    @Override
    public void trackViewOnStart(Activity activity) {

    }

    @Override
    public void trackViewOnStop(Activity activity) {

    }

    @Override
    protected void logHandledException(Exception e) {
        Mint.logException(e);
    }
}
