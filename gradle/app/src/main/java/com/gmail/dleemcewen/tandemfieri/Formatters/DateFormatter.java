package com.gmail.dleemcewen.tandemfieri.Formatters;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * DateFormatter provides date formatting logic
 */

public class DateFormatter {
    /**
     * toTimeStamp converts a date string to a timestamp format
     * @param dateString indicates the date string to convert
     * @return date in timestamp format
     */
    public static Timestamp toTimeStamp(String dateString) {
        DateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        Date formattedDate = new Date();

        try {
            formattedDate = format.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return new Timestamp(formattedDate.getTime());
    }

    /**
     * toTimeStamp converts a date to a timestamp format
     * @param dateValue indicates the date to convert
     * @return date in timestamp format
     */
    public static Timestamp toTimeStamp(Date dateValue) {
        DateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        Date formattedDate = new Date();

        try {
            formattedDate = format.parse(dateValue.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return new Timestamp(formattedDate.getTime());
    }
}
