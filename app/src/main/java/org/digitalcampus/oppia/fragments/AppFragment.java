package org.digitalcampus.oppia.fragments;

import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.digitalcampus.oppia.activity.AppActivity;
import org.digitalcampus.oppia.listener.APIKeyRequestListener;

public class AppFragment extends Fragment implements APIKeyRequestListener{

    public final String TAG = this.getClass().getSimpleName();

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
}
