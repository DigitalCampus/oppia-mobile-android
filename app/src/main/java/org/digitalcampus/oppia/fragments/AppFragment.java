package org.digitalcampus.oppia.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.digitalcampus.oppia.activity.AppActivity;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.di.AppComponent;
import org.digitalcampus.oppia.listener.APIKeyRequestListener;

import javax.inject.Inject;

public class AppFragment extends Fragment implements APIKeyRequestListener{

    public final String TAG = this.getClass().getSimpleName();

    @Inject
    SharedPreferences prefs;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeDaggerBase();
    }

    public AppComponent getAppComponent(){
        MobileLearning app = (MobileLearning) getActivity().getApplication();
        return app.getComponent();
    }


    private void initializeDaggerBase() {
        MobileLearning app = (MobileLearning) getActivity().getApplication();
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


}
