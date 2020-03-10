package org.digitalcampus.oppia.database.converters;

import androidx.room.TypeConverter;

import org.digitalcampus.oppia.utils.DateUtils;
import org.joda.time.DateTime;

public class TimestampConverter {

    private TimestampConverter() {
        throw new IllegalStateException("Utility class");
    }

    @TypeConverter
    public static DateTime fromTimestamp(String value) {
        try {
            return value == null ? null : DateUtils.DATETIME_FORMAT.parseDateTime(value);
        } catch (UnsupportedOperationException e) {
            return null;
        }
    }

    @TypeConverter
    public static String toTimestamp(DateTime dateTime) {
        return DateUtils.DATETIME_FORMAT.print(dateTime);
    }

}
