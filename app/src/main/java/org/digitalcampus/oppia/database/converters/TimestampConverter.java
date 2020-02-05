package org.digitalcampus.oppia.database.converters;

import androidx.room.TypeConverter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimestampConverter {
    static DateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @TypeConverter
    public static Date fromTimestamp(String value) {
        try {
            return value == null ? null : datetimeFormat.parse(value);
        } catch (ParseException e) {
            return null;
        }
    }

    @TypeConverter
    public static String toTimestamp(Date date) {
        return datetimeFormat.format(date);
    }

}
