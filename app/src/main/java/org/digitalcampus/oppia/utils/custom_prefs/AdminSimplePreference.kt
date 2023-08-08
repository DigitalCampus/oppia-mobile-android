package org.digitalcampus.oppia.utils.custom_prefs

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference

class AdminSimplePreference : Preference, AdminPreference {
    private val customOnPreferenceClickListener: OnPreferenceClickListener? = null

    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context!!, attrs, defStyleAttr) {}

    override fun setOnPreferenceClickListener(onPreferenceClickListener: OnPreferenceClickListener?) {
        super.setOnPreferenceClickListener(onPreferenceClickListener)
    }

    override fun onAccessGranted() {
        super.setOnPreferenceClickListener(null)
        super.performClick()
    }
}