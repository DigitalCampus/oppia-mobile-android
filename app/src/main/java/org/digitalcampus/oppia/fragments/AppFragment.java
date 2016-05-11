package org.digitalcampus.oppia.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import org.digitalcampus.oppia.activity.AppActivity;
import org.digitalcampus.oppia.listener.APIKeyRequestListener;

public class AppFragment extends Fragment implements APIKeyRequestListener{

    @Override
    public void apiKeyInvalidated() {
        FragmentActivity parent = this.getActivity();
        if (parent != null && parent instanceof AppActivity){
            ((AppActivity) parent).apiKeyInvalidated();
        }
    }
}
