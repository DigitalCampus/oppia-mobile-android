package org.digitalcampus.oppia.utils.resources;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.oppia.utils.storage.FileUtils;

import java.io.File;

import androidx.core.content.FileProvider;

public class ExternalResourceOpener {

    private static final String FILEPROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID  + ".provider";
    public static final String EXTERNAL_APP_PACKAGE = "org.medicmobile.webapp.mobile.cht_cha_learning_care";

    private ExternalResourceOpener() {
        throw new IllegalStateException("Utility class");
    }

    public static Intent getIntentToOpenResource(Context ctx, File resourceFile){

        Uri resourceUri = FileProvider.getUriForFile(ctx, FILEPROVIDER_AUTHORITY, resourceFile);
        String resourceMimeType = FileUtils.getMimeType(resourceFile.getPath());

        // check there is actually an app installed to open this filetype
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(resourceUri, resourceMimeType);

        PackageManager pm = ctx.getPackageManager();

        ActivityInfo activityInfo = intent.resolveActivityInfo(pm, intent.getFlags());
        boolean appFound = activityInfo !=  null && activityInfo.exported;

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

    public static Intent getExternalActivityIntent(Context ctx){
        PackageManager pm = ctx.getPackageManager();
        Intent intent =  pm.getLaunchIntentForPackage(EXTERNAL_APP_PACKAGE);
        if (intent != null){
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        return intent;
    }
}
