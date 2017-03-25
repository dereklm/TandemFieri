package com.gmail.dleemcewen.tandemfieri.Formatters;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    /**
     * convertStandardTimeToMilitaryTime converts standard time to military time
     * @param dateValue date containing the time to convert
     * @return converted time in military format
     */
    public static int convertStandardTimeToMilitaryTime(Date dateValue) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateValue);

        String militaryTime = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) + String.valueOf(calendar.get(Calendar.MINUTE));
        return Integer.valueOf(militaryTime);
    }

    /**
     * convertMilitaryTimeToStandard converts an integer representing military time
     * to standard time
     * @param militaryTime integer representing military time
     * @return standard time
     */
    public static String convertMilitaryTimeToStandard(int militaryTime) {
        int hours = militaryTime / 100;
        int minutes = militaryTime - (hours * 100);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);

        DateFormat dateFormatter = new SimpleDateFormat("hh:mm a", Locale.US);
        return dateFormatter.format(calendar.getTime());
    }
}
