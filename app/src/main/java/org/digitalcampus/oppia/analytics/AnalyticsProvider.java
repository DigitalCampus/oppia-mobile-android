package org.digitalcampus.oppia.analytics;

import android.content.Context;

public class AnalyticsProvider {

    public AnalyticsProvider() {

    }


    public boolean shouldShowOptOutRationale(Context ctx){
        return Analytics.shouldShowOptOutRationale(ctx);
    }

}
