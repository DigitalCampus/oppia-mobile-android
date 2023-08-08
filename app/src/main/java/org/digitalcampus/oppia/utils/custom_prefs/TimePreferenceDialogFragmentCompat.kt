package org.digitalcampus.oppia.utils.custom_prefs

import android.content.Context
import android.view.View
import android.widget.TimePicker
import androidx.preference.DialogPreference.TargetFragment
import androidx.preference.Preference
import androidx.preference.PreferenceDialogFragmentCompat
import org.digitalcampus.oppia.utils.custom_prefs.TimePreference.Companion.timeToString

class TimePreferenceDialogFragmentCompat : PreferenceDialogFragmentCompat(), TargetFragment {
    var timePicker: TimePicker? = null
    override fun onCreateDialogView(context: Context): View? {
        timePicker = TimePicker(context)
        return timePicker
    }

    override fun onBindDialogView(v: View) {
        super.onBindDialogView(v)
        timePicker?.setIs24HourView(true)
        val pref = preference as TimePreference
        timePicker?.currentHour = pref.hour
        timePicker?.currentMinute = pref.minute
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            val pref = preference as TimePreference
            pref.hour = timePicker!!.currentHour
            pref.minute = timePicker!!.currentMinute
            val value = timeToString(pref.hour, pref.minute)
            if (pref.callChangeListener(value)) pref.persistStringValue(value)
        }
    }

    override fun <T : Preference?> findPreference(key: CharSequence): T? {
        return preference as T?
    }
}