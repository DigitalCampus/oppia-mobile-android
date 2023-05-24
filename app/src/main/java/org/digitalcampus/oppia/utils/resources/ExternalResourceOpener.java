package org.digitalcampus.oppia.utils.resources;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ExternalResourceOpener {

    private static final String FILEPROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";

    private static final String GOOGLE_PLAY_INTENT_URI = "market://details?id=";
    private static final String GOOGLE_PLAY_INTENT_URL = "https://play.google.com/store/apps/details?id=";
    private static Map<String, String> MIMETYPE_OPENER_PACKAGES =new HashMap<String , String>() {{
        put("application/pdf", "com.artifex.mupdf.viewer.app");
    }};

    private ExternalResourceOpener() {
        throw new IllegalStateException("Utility class");
    }

    public static Intent getIntentToOpenResource(Context ctx, File resourceFile) {

        String resourceMimeType = FileUtils.getMimeType(resourceFile.getPath());

        String storageLocationRoot = Storage.getStorageLocationRoot(ctx);
        if (resourceFile.getAbsolutePath().contains(storageLocationRoot)){
            String relativePath = resourceFile.getAbsolutePath().substring(storageLocationRoot.length() +1);
            //Create the file descriptor again to avoid possible file:// prefixes in the URI
            resourceFile = new File(Storage.getStorageLocationRoot(ctx), relativePath);
        }
        Uri resourceUri = FileProvider.getUriForFile(ctx, FILEPROVIDER_AUTHORITY, resourceFile);

        // check there is actually an app installed to open this filetype
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(resourceUri, resourceMimeType);

        String targetAppPackage = getAppToResolveIntent(ctx, intent);
        if (targetAppPackage != null) {
            //In case there is a valid filter, we grant permission and return the intent, otherwise null
            ctx.grantUriPermission(targetAppPackage, resourceUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            return intent;
        }
        return null;
    }

    public static Intent getIntentToInstallAppForResource(Context ctx, File resourceFile){
        String resourceMimeType = FileUtils.getMimeType(resourceFile.getPath());
        if (! MIMETYPE_OPENER_PACKAGES.containsKey(resourceMimeType)) {
            return null;
        }

        String appPackage = MIMETYPE_OPENER_PACKAGES.get(resourceMimeType);
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setData(Uri.parse(GOOGLE_PLAY_INTENT_URI + appPackage));

        // If Google Play is not installed, we open the Google Play link in the browser
        if (getAppToResolveIntent(ctx, intent) == null) {
            intent.setData(Uri.parse(GOOGLE_PLAY_INTENT_URL + appPackage));
        }
        return intent;
    }

    private static String getAppToResolveIntent(Context ctx, Intent i) {
        PackageManager pm = ctx.getPackageManager();
        ActivityInfo activityInfo = i.resolveActivityInfo(pm, 0);
        boolean appFound = activityInfo !=  null && activityInfo.exported;
        return appFound ? activityInfo.packageName : null;
    }

    public static Intent constructShareFileIntent(Context ctx, File fileToShare, String type){

        if (!fileToShare.exists()) {
            return null;
        }

        Intent share = new Intent(Intent.ACTION_SEND);
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        share.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        share.setType(type);

        Uri targetUri = FileProvider.getUriForFile(ctx, FILEPROVIDER_AUTHORITY, fileToShare);
        share.putExtra(Intent.EXTRA_STREAM, targetUri);
        return share;

    }

    public static void shareFile(Context context, File fileToShare, String type) {
        Intent intentShare = constructShareFileIntent(context, fileToShare, type);
        if (intentShare == null) {
            Toast.makeText(context, context.getString(R.string.error_resource_not_found, fileToShare.getName()), Toast.LENGTH_SHORT).show();
        }
        try {
            context.startActivity(intentShare);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.no_app_to_share, Toast.LENGTH_SHORT).show();
        }
    }
}
