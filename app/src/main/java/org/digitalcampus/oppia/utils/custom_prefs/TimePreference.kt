package org.digitalcampus.oppia.utils.custom_prefs

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.DialogPreference

open class TimePreference(context: Context, attrs: AttributeSet) : DialogPreference(context, attrs) {
    @JvmField
    var hour = 0
    @JvmField
    var minute = 0
    override fun onGetDefaultValue(a: TypedArray, index: Int): Any? {
        return a.getString(index)
    }

    override fun onSetInitialValue(restoreValue: Boolean, defaultValue: Any) {
        val value: String = if (restoreValue) {
            getPersistedString(defaultValue.toString())
        } else {
            defaultValue.toString()
        }
        hour = parseHour(value)
        minute = parseMinute(value)
    }

    var value: String
        get() = timeToString(hour, minute)
        set(value) {
            hour = parseHour(value)
            minute = parseMinute(value)
            persistString(value)
        }

    fun persistStringValue(value: String?) {
        persistString(value)
    }

    companion object {
        fun parseHour(value: String): Int {
            return try {
                val time = value.split(":".toRegex()).toTypedArray()
                time[0].toInt()
            } catch (e: Exception) {
                0
            }
        }

        fun parseMinute(value: String): Int {
            return try {
                val time = value.split(":".toRegex()).toTypedArray()
                time[1].toInt()
            } catch (e: Exception) {
                0
            }
        }

        fun timeToString(h: Int, m: Int): String {
            return String.format("%02d", h) + ":" + String.format("%02d", m)
        }
    }
}