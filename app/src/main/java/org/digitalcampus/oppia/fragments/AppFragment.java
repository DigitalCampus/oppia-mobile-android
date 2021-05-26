package org.digitalcampus.oppia.fragments;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.AppActivity;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.di.AppComponent;
import org.digitalcampus.oppia.listener.APIKeyRequestListener;

import javax.inject.Inject;

public class AppFragment extends Fragment implements APIKeyRequestListener{

    public static final String TAG = AppFragment.class.getSimpleName();

    @Inject
    SharedPreferences prefs;

    private ProgressDialog progressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeDaggerBase();
    }

    public AppComponent getAppComponent(){
        App app = (App) getActivity().getApplication();
        return app.getComponent();
    }


    private void initializeDaggerBase() {
        App app = (App) getActivity().getApplication();
        app.getComponent().inject(this);
    }

    @Override
    public void apiKeyInvalidated() {
        FragmentActivity parent = this.getActivity();
        if (parent instanceof AppActivity){
            ((AppActivity) parent).apiKeyInvalidated();
        }
    }

    public void toast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    public void toast(int stringId) {
        toast(getString(stringId));
    }


    public SharedPreferences getPrefs() {
        return prefs;
    }

    public void showProgressDialog(String message) {
        showProgressDialog(message, true);
    }

    public void showProgressDialog(String message, boolean cancelable) {

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }

        progressDialog = new ProgressDialog(getActivity(), R.style.Oppia_AlertDialogStyle);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(cancelable);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()){
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    protected boolean isProgressDialogShowing() {
        return progressDialog != null && progressDialog.isShowing();
    }
}
