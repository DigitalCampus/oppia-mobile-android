package org.digitalcampus.oppia.service;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.AppActivity;
import org.digitalcampus.oppia.api.RemoteApiEndpoint;
import org.digitalcampus.oppia.application.PermissionsManager;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.HTTPClientUtils;
import org.digitalcampus.oppia.utils.UIUtils;

import okhttp3.HttpUrl;

import static android.content.Context.DOWNLOAD_SERVICE;

/**
 * This class wraps system DOWNLOAD_SERVICE with Oppia customizations
 */
public class DownloadOppiaDataService {


    private final Context context;
    private DownloadManager downloadManager;
    private DownloadOppiaDataListener listener;
    private boolean showDialog;

    public DownloadOppiaDataService(Context context) {
        this.context = context;
    }

    public void setDownloadOppiaDataListener(DownloadOppiaDataListener listener) {
        this.listener = listener;
    }

    public void setShowOpenDownloadsDialogOnSuccess(boolean showDialog) {
        this.showDialog = showDialog;
    }

    /**
     * Download an oppia file
     * @param path for the download request
     * @param filename for store in downloads folder. If null, the last segment of path will be used
     */
    public void downloadOppiaData(String path, @Nullable String filename) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int permitted = context.checkSelfPermission( Manifest.permission.WRITE_EXTERNAL_STORAGE );
            if( permitted != PackageManager.PERMISSION_GRANTED ) {
                Toast.makeText(context, R.string.storage_permission_not_granted, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        DbHelper db = DbHelper.getInstance(context);
        User user;
        try {
            user = db.getUser(SessionManager.getUsername(context));
        } catch (UserNotFoundException e) {
            e.printStackTrace();
            return;
        }

        String url = new RemoteApiEndpoint().getFullURL(context, path);
        HttpUrl urlWithCredentials = HTTPClientUtils.getUrlWithCredentials(url, user.getUsername(), user.getApiKey());
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(urlWithCredentials.toString()));

        if (TextUtils.isEmpty(filename)) {
            filename = urlWithCredentials.pathSegments().get(urlWithCredentials.pathSize() - 1);
        }

        if (listener != null) {
            listener.onDownloadStarted();
        }

        String filenameUser = user.getUsername() + "-" + filename;
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filenameUser);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE); // to notify when download is complete
        downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);
    }

    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Fetching the download id received with the broadcast
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            Cursor c = downloadManager.query(new DownloadManager.Query()
                    .setFilterById(id));

            c.moveToFirst();

            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));

            switch (status) {

                case DownloadManager.STATUS_SUCCESSFUL:
                    if (listener != null) {
                        listener.onDownloadFinished(true, null);
                    }

                    if (showDialog) {
                        showDownloadSuccessDialog();
                    }
                    break;

                case DownloadManager.STATUS_FAILED:
                    downloadManager.remove(id);
                    String reason = c.getString(c.getColumnIndex(DownloadManager.COLUMN_REASON));
                    if (listener != null) {
                        listener.onDownloadFinished(false, reason);
                    }
                    break;
            }

        }
    };

    private void showDownloadSuccessDialog() {
        new AlertDialog.Builder(context)
                .setTitle(R.string.download_complete)
                .setMessage(R.string.user_data_download_success)
                .setPositiveButton(R.string.open_download_folder, (dialog, which) -> context.startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)))
                .setNeutralButton(R.string.back, null)
                .show();

    }

    public void onResume() {
        context.registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public void onPause() {
        context.unregisterReceiver(onDownloadComplete);
    }

    public interface DownloadOppiaDataListener {
        void onDownloadStarted();

        void onDownloadFinished(boolean success, String errorMessage);

    }
}
