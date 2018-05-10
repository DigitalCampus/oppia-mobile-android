package org.digitalcampus.oppia.utils.resources;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.oppia.utils.storage.FileUtils;

import java.io.File;
import java.util.List;

import static android.support.v4.content.FileProvider.getUriForFile;

public class ExternalResourceOpener {

    public static String FILEPROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID  + ".provider";

    public static Intent getIntentToOpenResource(Context ctx, File resourceFile){

        Uri resourceUri = getUriForFile(ctx, FILEPROVIDER_AUTHORITY, resourceFile);
        String resourceMimeType = FileUtils.getMimeType(resourceFile.getName());

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

    public static Intent constructShareFileIntent(Context ctx, File filteToShare){

        Intent share = new Intent(Intent.ACTION_SEND);
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        share.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        share.setType("audio/*");

        Uri targetUri = FileProvider.getUriForFile(ctx, FILEPROVIDER_AUTHORITY, filteToShare);
        share.putExtra(Intent.EXTRA_STREAM, targetUri);

        return share;
    }
}
