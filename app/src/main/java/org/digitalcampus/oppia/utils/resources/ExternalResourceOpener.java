package org.digitalcampus.oppia.utils.resources;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.widget.Toast;

import org.kano.training.oppia.R;

import java.util.List;

/**
 * Created by Joseba on 28/01/2015.
 */
public class ExternalResourceOpener {

    public static Intent getIntentToOpenResource(Context ctx, Uri resourceUri, String resourceMimeType){

        // check there is actually an app installed to open this filetype
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(resourceUri, resourceMimeType);

        PackageManager pm = ctx.getPackageManager();

        List<ResolveInfo> infos = pm.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);
        boolean appFound = false;
        for (ResolveInfo info : infos) {
            IntentFilter filter = info.filter;
            if (filter != null && filter.hasAction(Intent.ACTION_VIEW)) {
                // Found an app with the right intent/filter
                appFound = true;
            }
        }

        //In case there is a valid filter, we return the intent, otherwise null
        return (appFound? intent : null);



    }
}
