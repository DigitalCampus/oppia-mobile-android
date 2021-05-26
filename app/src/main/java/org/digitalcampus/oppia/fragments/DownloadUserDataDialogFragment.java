package org.digitalcampus.oppia.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.DialogDownloadUserDataBinding;
import org.digitalcampus.oppia.activity.AppActivity;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.application.PermissionsManager;
import org.digitalcampus.oppia.service.DownloadOppiaDataService;
import org.digitalcampus.oppia.utils.UIUtils;

import androidx.annotation.Nullable;

import java.io.File;

public class DownloadUserDataDialogFragment extends BottomSheetDialogFragment implements DownloadOppiaDataService.DownloadOppiaDataListener {

    protected static final String TAG = DownloadUserDataDialogFragment.class.getSimpleName();

    private DialogDownloadUserDataBinding binding;
    private DownloadOppiaDataService downloadOppiaDataService;

    public static DownloadUserDataDialogFragment newInstance() {
        return new DownloadUserDataDialogFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DialogDownloadUserDataBinding.inflate(inflater, container, false);
        downloadOppiaDataService = new DownloadOppiaDataService(getActivity());
        downloadOppiaDataService.setDownloadOppiaDataListener(this);
        downloadOppiaDataService.setShowOpenDownloadsDialogOnSuccess(true);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().setCanceledOnTouchOutside(false);

        binding.downloadDataProfile.setOnClickListener(v -> downloadUserData("profile"));
        binding.downloadDataCourse.setOnClickListener(v -> downloadUserData("activity"));
        binding.downloadDataQuizzes.setOnClickListener(v -> downloadUserData("quiz"));
        binding.downloadDataPoints.setOnClickListener(v -> downloadUserData("points"));
        binding.downloadDataBadges.setOnClickListener(v -> downloadUserData("badges"));
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean hasPermissions = PermissionsManager.checkPermissionsAndInform(getActivity(),
                PermissionsManager.STORAGE_PERMISSIONS, binding.permissionsExplanation);

        binding.downloadDataList.setVisibility(hasPermissions ? View.VISIBLE : View.GONE);

        downloadOppiaDataService.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        downloadOppiaDataService.onPause();

    }

    private void downloadUserData(String data){

        Log.d(TAG, data);

        String path = Paths.DOWNLOAD_ACCOUNT_DATA_PATH + data + File.separator;
        downloadOppiaDataService.downloadOppiaData(path, data + ".html");

    }


    @Override
    public void onDownloadStarted() {
        binding.loadingDownload.setVisibility(View.VISIBLE);
        binding.downloadDataList.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDownloadFinished(boolean success, String errorMessage) {

        binding.loadingDownload.setVisibility(View.GONE);
        binding.downloadDataList.setVisibility(View.VISIBLE);

        if (!success) {
            UIUtils.showAlert(getActivity(), R.string.error, getString(R.string.error_download_failure_reason_format, errorMessage));
        }
    }

}
