package org.digitalcampus.oppia.fragments;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.api.RemoteApiEndpoint;
import org.digitalcampus.oppia.application.PermissionsManager;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.HTTPClientUtils;
import org.digitalcampus.oppia.utils.UIUtils;

import androidx.annotation.Nullable;

import java.io.File;

import static android.content.Context.DOWNLOAD_SERVICE;

public class DownloadUserDataDialogFragment extends BottomSheetDialogFragment {

    protected static final String TAG = DownloadUserDataDialogFragment.class.getSimpleName();

    private View downloadDataList;
    private View loadingSpinner;
    private ViewGroup permissionsExplanation;
    private DownloadManager downloadManager;

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
        loadingSpinner.setVisibility(View.VISIBLE);
        downloadDataList.setVisibility(View.INVISIBLE);


        DbHelper db = DbHelper.getInstance(getActivity());
        User u;
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

        String filename = u.getUsername() + "-" + data + ".html";
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE); // to notify when download is complete
        downloadManager = (DownloadManager) getActivity().getSystemService(DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);

    }

    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            loadingSpinner.setVisibility(View.GONE);
            downloadDataList.setVisibility(View.VISIBLE);

            //Fetching the download id received with the broadcast
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            Cursor c = downloadManager.query(new DownloadManager.Query()
                    .setFilterById(id));

            c.moveToFirst();

            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));

            switch (status) {

                case DownloadManager.STATUS_SUCCESSFUL:
                    showDownloadSuccessDialog();
                    break;

                case DownloadManager.STATUS_FAILED:
                    downloadManager.remove(id);
                    String reason = c.getString(c.getColumnIndex(DownloadManager.COLUMN_REASON));
                    UIUtils.showAlert(getActivity(), R.string.error, getString(R.string.error_download_failure_reason_format, reason));
                    break;
            }

        }
    };

    private void showDownloadSuccessDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.download_complete)
                .setMessage(R.string.user_data_download_success)
                .setPositiveButton(R.string.open_download_folder, (dialog, which) -> startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)))
                .setNeutralButton(R.string.back, null)
                .show();

    }

}
