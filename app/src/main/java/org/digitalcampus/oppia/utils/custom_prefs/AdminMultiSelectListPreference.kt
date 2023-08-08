package org.digitalcampus.oppia.utils.custom_prefs

import android.content.Context
import android.util.AttributeSet
import androidx.preference.MultiSelectListPreference

class AdminMultiSelectListPreference : MultiSelectListPreference, AdminPreference {
    private var customOnPreferenceClickListener: OnPreferenceClickListener? = null

    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context!!, attrs, defStyleAttr) {}

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