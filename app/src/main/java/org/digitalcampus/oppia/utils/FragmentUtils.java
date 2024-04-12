package org.digitalcampus.oppia.utils;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceFragmentCompat;

public class FragmentUtils {
    public static Fragment findCallbackFragment(Fragment fragment) {
        Fragment parentFragment = fragment.getParentFragment();
        if (parentFragment != null && parentFragment instanceof PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback) {
            return parentFragment;
        } else {
            return findCallbackFragment(parentFragment);
        }
    }
}
