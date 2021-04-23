package org.digitalcampus.oppia.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.digitalcampus.mobile.learning.R;

import androidx.annotation.Nullable;

public class DownloadUserDataDialogFragment extends BottomSheetDialogFragment {

    protected static final String TAG = DownloadUserDataDialogFragment.class.getSimpleName();

    private View downloadDataList;
    private View loadingSpinner;


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

        downloadDataList = view.findViewById(R.id.download_data_list);
        loadingSpinner = view.findViewById(R.id.loading_download);

        view.findViewById(R.id.download_data_profile).setOnClickListener(v -> downloadUserData("profile"));
        view.findViewById(R.id.download_data_course).setOnClickListener(v -> downloadUserData("course"));
        view.findViewById(R.id.download_data_quizzes).setOnClickListener(v -> downloadUserData("quizzes"));
        view.findViewById(R.id.download_data_points).setOnClickListener(v -> downloadUserData("points"));
        view.findViewById(R.id.download_data_badges).setOnClickListener(v -> downloadUserData("badges"));
    }


    private void downloadUserData(String data){
        getDialog().setCancelable(false);
        Log.d(TAG, data);
        loadingSpinner.setVisibility(View.VISIBLE);
        downloadDataList.setVisibility(View.INVISIBLE);
    }
}
