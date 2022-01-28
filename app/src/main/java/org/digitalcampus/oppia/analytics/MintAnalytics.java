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
        //Configuration is changed on the run, so we don't need to do anything
    }

    @Override
    public void setUserIdentifier(String username) {
        Mint.setUserIdentifier(username.equals("") ? "anon" : username);
    }

    @Override
    public void trackViewOnStart(Activity activity) {
        // We are not using view tracking in Mint
    }

    @Override
    public void trackViewOnStop(Activity activity) {
        // We are not using view tracking in Mint
    }

    @Override
    protected void logHandledException(Exception e) {
        Mint.logException(e);
    }
}
