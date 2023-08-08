package org.digitalcampus.oppia.utils.custom_prefs

import android.content.Context
import android.util.AttributeSet

class AdminTimePreference(context: Context, attrs: AttributeSet) : TimePreference(context, attrs), AdminPreference {
    private var customOnPreferenceClickListener: OnPreferenceClickListener? = null
    override fun onClick() {
        if (customOnPreferenceClickListener != null) {
            val handled = customOnPreferenceClickListener!!.onPreferenceClick(this)
            if (handled) {
                return
            }
        }
        super.onClick()
    }

    override fun setOnPreferenceClickListener(onPreferenceClickListener: OnPreferenceClickListener?) {
        customOnPreferenceClickListener = onPreferenceClickListener
    }

    override fun onAccessGranted() {
        super.onClick()
    }
}