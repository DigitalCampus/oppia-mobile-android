package org.digitalcampus.oppia.utils;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateUtils {


    public static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd");
    public static final DateTimeFormatter MONTH_FORMAT = DateTimeFormat.forPattern("MMM");
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormat.forPattern("HH:mm:ss");
    public static final DateTimeFormatter DATE_FORMAT_DAY_MONTH = DateTimeFormat.forPattern("d MMM");
    public static final DateTimeFormatter TIME_FORMAT_HOURS_MINUTES = DateTimeFormat.forPattern("HH:mm");
    public static final DateTimeFormatter DISPLAY_DATETIME_FORMAT = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm");

    private DateUtils() {
        throw new IllegalStateException("Utility class");
    }
}
