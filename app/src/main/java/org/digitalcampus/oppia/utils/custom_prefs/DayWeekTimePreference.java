package org.digitalcampus.oppia.utils.custom_prefs;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;
import androidx.preference.EditTextPreference;

import org.digitalcampus.oppia.service.CoursesCompletionReminderWorkerManager;

public class DayWeekTimePreference extends DialogPreference {

    private String dayTimeMillis;

    public DayWeekTimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DayWeekTimePreference(Context context) {
        super(context);
    }

    public void setDayTimeMillis(String dayTimeMillis) {
        persistString(dayTimeMillis);
        notifyChanged();
    }

    public String getDayTimeMillis() {
        return getPersistedString(CoursesCompletionReminderWorkerManager.DEFAULT_COURSES_REMINDER_TIME_MILLIS);
    }
}
