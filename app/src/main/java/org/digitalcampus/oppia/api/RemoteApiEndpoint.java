package org.digitalcampus.oppia.api;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;

public class RemoteApiEndpoint implements ApiEndpoint{

    @Override
    public String getFullURL(Context ctx, String apiPath) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String url = prefs.getString(PrefsActivity.PREF_SERVER, ctx.getString(R.string.prefServerDefault));
        if (!url.endsWith("/") && (!apiPath.startsWith("/"))) url += "/";
        return url + apiPath;
    }
}
