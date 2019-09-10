package org.digitalcampus.oppia.fragments;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.digitalcampus.oppia.activity.AppActivity;
import org.digitalcampus.oppia.listener.APIKeyRequestListener;

public class AppFragment extends Fragment implements APIKeyRequestListener{

    @Override
    public void apiKeyInvalidated() {
        FragmentActivity parent = this.getActivity();
        if (parent instanceof AppActivity){
            ((AppActivity) parent).apiKeyInvalidated();
        }
    }
}
