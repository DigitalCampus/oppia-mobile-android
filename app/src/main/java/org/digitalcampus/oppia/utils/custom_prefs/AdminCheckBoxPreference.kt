package org.digitalcampus.oppia.utils.custom_prefs

import android.content.Context
import android.util.AttributeSet
import androidx.preference.CheckBoxPreference

class AdminCheckBoxPreference : CheckBoxPreference, AdminPreference {
    enum class PromptAdminDialogMode {
        ON_CHECK, ON_UNCHECK, ON_BOTH
    }

    private var mode = PromptAdminDialogMode.ON_BOTH
    private var customOnPreferenceClickListener: OnPreferenceClickListener? = null

    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context!!, attrs, defStyleAttr) {}

    override fun onClick() {
        if (customOnPreferenceClickListener != null) {
            if ((!isChecked && mode == PromptAdminDialogMode.ON_CHECK || isChecked) && mode == PromptAdminDialogMode.ON_UNCHECK || mode == PromptAdminDialogMode.ON_BOTH) {
                val handled = customOnPreferenceClickListener!!.onPreferenceClick(this)
                if (handled) {
                    return
                }
            }
        }
        super.onClick()
    }

    override fun setOnPreferenceClickListener(onPreferenceClickListener: OnPreferenceClickListener?) {
        customOnPreferenceClickListener = onPreferenceClickListener
    }

    fun setPromptAdminDialogMode(mode: PromptAdminDialogMode) {
        this.mode = mode
    }

    override fun onAccessGranted() {
        super.onClick()
    }
}