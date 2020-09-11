package org.digitalcampus.oppia.api;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;

public class RemoteApiEndpoint implements ApiEndpoint{

    private static final int MIN_MAJOR_VERSION = 0;
    private static final int MIN_MIN_VERSION = 12;
    private static final int MIN_BUILD_VERSION = 6;

    @Override
    public String getFullURL(Context ctx, String apiPath) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String url = prefs.getString(PrefsActivity.PREF_SERVER, ctx.getString(R.string.prefServerDefault));
        if (!url.endsWith("/") && (!apiPath.startsWith("/"))) url += "/";
        return url + apiPath;
    }

    public static boolean isServerVersionCompatible(String version){

        if (version.startsWith("v")){
            version = version.substring(1);
        }

        String[] versioning = version.split("\\.");
        if (versioning.length < 3){
            // No correct format (expected x.x.x)
            return false;
        }

        try{
            int majorVersion = Integer.parseInt(versioning[0]);
            int minorVersion = Integer.parseInt(versioning[1]);
            int buildVersion = Integer.parseInt(versioning[2].split("-")[0]);

            return ((majorVersion > MIN_MAJOR_VERSION) ||
                    (majorVersion == MIN_MAJOR_VERSION && minorVersion > MIN_MIN_VERSION) ||
                    (majorVersion == MIN_MAJOR_VERSION && minorVersion == MIN_MIN_VERSION && buildVersion >= MIN_BUILD_VERSION));
        }
        catch (NumberFormatException e){
            return false;
        }

    }
}
