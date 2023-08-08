package org.digitalcampus.oppia.utils

import org.joda.time.format.DateTimeFormat

object DateUtils {

    @JvmField
    val DATETIME_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
    @JvmField
    val DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd")
    @JvmField
    val MONTH_FORMAT = DateTimeFormat.forPattern("MMM")
    @JvmField
    val TIME_FORMAT = DateTimeFormat.forPattern("HH:mm:ss")
    @JvmField
    val DATE_FORMAT_DAY_MONTH = DateTimeFormat.forPattern("d MMM")
    @JvmField
    val TIME_FORMAT_HOURS_MINUTES = DateTimeFormat.forPattern("HH:mm")
    @JvmField
    val DISPLAY_DATETIME_FORMAT = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm")
}