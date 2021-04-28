package org.digitalcampus.oppia.fragments;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.AppActivity;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.api.RemoteApiEndpoint;
import org.digitalcampus.oppia.application.PermissionsManager;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.DownloadUserDataTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.HTTPClientUtils;

import androidx.annotation.Nullable;

import java.io.File;

import static android.content.Context.DOWNLOAD_SERVICE;

public class DownloadUserDataDialogFragment extends BottomSheetDialogFragment implements APIRequestListener {

    protected static final String TAG = DownloadUserDataDialogFragment.class.getSimpleName();

    private View downloadDataList;
    private View loadingSpinner;
    private ViewGroup permissionsExplanation;
    private DownloadManager downloadManager;
    private String filename;


    public static DownloadUserDataDialogFragment newInstance() {
        return new DownloadUserDataDialogFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_download_user_data, container);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().setCanceledOnTouchOutside(false);

        downloadDataList = view.findViewById(R.id.download_data_list);
        loadingSpinner = view.findViewById(R.id.loading_download);
        permissionsExplanation = view.findViewById(R.id.permissions_explanation);

        view.findViewById(R.id.download_data_profile).setOnClickListener(v -> downloadUserData("profile"));
        view.findViewById(R.id.download_data_course).setOnClickListener(v -> downloadUserData("activity"));
        view.findViewById(R.id.download_data_quizzes).setOnClickListener(v -> downloadUserData("quiz"));
        view.findViewById(R.id.download_data_points).setOnClickListener(v -> downloadUserData("points"));
        view.findViewById(R.id.download_data_badges).setOnClickListener(v -> downloadUserData("badges"));
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean hasPermissions = PermissionsManager.checkPermissionsAndInform(getActivity(),
                PermissionsManager.STORAGE_PERMISSIONS, permissionsExplanation);

        downloadDataList.setVisibility(hasPermissions ? View.VISIBLE : View.GONE);
        getActivity().registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(onDownloadComplete);

    }

    private void downloadUserData(String data){
        Log.d(TAG, data);
//        loadingSpinner.setVisibility(View.VISIBLE);
//        downloadDataList.setVisibility(View.INVISIBLE);


        DbHelper db = DbHelper.getInstance(getActivity());
        User u = null;
        try {
            u = db.getUser(SessionManager.getUsername(getActivity()));
        } catch (UserNotFoundException e) {
            e.printStackTrace();
            return;
        }

        String path = Paths.DOWNLOAD_ACCOUNT_DATA_PATH + data + File.separator;
        String url = new RemoteApiEndpoint().getFullURL(getActivity(), path);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.addRequestHeader(HTTPClientUtils.HEADER_AUTH,
                HTTPClientUtils.getAuthHeaderValue(u.getUsername(), u.getApiKey()));

//        request.setTitle(getString(R.string.downloading) + " " + data);

        filename = u.getUsername() + "-" + data + ".html";
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); // to notify when download is complete
        request.allowScanningByMediaScanner();// if you want to be available from media players
        downloadManager = (DownloadManager) getActivity().getSystemService(DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);


//        DownloadUserDataTask task = new DownloadUserDataTask(getContext());
//        task.setAPIRequestListener(this);
//        task.execute(data);
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
            if (status == DownloadManager.STATUS_FAILED) {
                Log.i(TAG, "onReceive: download failed");
                String reason = c.getString(c.getColumnIndex(DownloadManager.COLUMN_REASON));
                Log.i(TAG, "onReceive: reason: " + reason);
                String fileUri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                Log.i(TAG, "onReceive: uri: " + fileUri);

                File file = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), filename);
                if (file.exists()) {
                    boolean deleteOk = file.delete();
                    Log.i(TAG, "onReceive: deleteOk " + deleteOk);
                }

                if (fileUri == null) {
                    return;
                }
                String path = Uri.parse(fileUri).getPath();
                Log.i(TAG, "onReceive: path: " + path);
                File file1 = new File(path);
                boolean deleteOk = file1.delete();
                Log.i(TAG, "onReceive: deleteOk: " + deleteOk);

            }

            Snackbar.make(getView().getRootView(), R.string.ok, Snackbar.LENGTH_LONG).show();
        }
    };

    @Override
    public void apiRequestComplete(Payload response) {
        if (response.isResult()){

        }
        else{
            Toast.makeText(getActivity(), response.getResultResponse(), Toast.LENGTH_LONG).show();
        }

        dismiss();
    }

    @Override
    public void apiKeyInvalidated() {
        ((AppActivity) getActivity()).apiKeyInvalidated();
        dismiss();
    }
}
