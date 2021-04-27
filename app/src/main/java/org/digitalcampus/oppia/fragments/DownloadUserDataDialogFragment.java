package org.digitalcampus.oppia.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.AppActivity;
import org.digitalcampus.oppia.application.PermissionsManager;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.task.DownloadUserDataTask;
import org.digitalcampus.oppia.task.Payload;

import androidx.annotation.Nullable;

public class DownloadUserDataDialogFragment extends BottomSheetDialogFragment implements APIRequestListener {

    protected static final String TAG = DownloadUserDataDialogFragment.class.getSimpleName();

    private View downloadDataList;
    private View loadingSpinner;
    private ViewGroup permissionsExplanation;


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
        view.findViewById(R.id.download_data_course).setOnClickListener(v -> downloadUserData("course"));
        view.findViewById(R.id.download_data_quizzes).setOnClickListener(v -> downloadUserData("quizzes"));
        view.findViewById(R.id.download_data_points).setOnClickListener(v -> downloadUserData("points"));
        view.findViewById(R.id.download_data_badges).setOnClickListener(v -> downloadUserData("badges"));
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean hasPermissions = PermissionsManager.checkPermissionsAndInform(getActivity(),
                PermissionsManager.STORAGE_PERMISSIONS, permissionsExplanation);

        downloadDataList.setVisibility(hasPermissions ? View.VISIBLE : View.GONE);
    }


    private void downloadUserData(String data){
        Log.d(TAG, data);
        loadingSpinner.setVisibility(View.VISIBLE);
        downloadDataList.setVisibility(View.INVISIBLE);

        DownloadUserDataTask task = new DownloadUserDataTask(getContext());
        task.setAPIRequestListener(this);
        task.execute(data);
    }

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
