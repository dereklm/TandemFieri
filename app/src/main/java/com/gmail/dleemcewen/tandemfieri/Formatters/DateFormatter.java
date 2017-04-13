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
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateValue);

        StringBuilder formattedDateString = new StringBuilder();
        formattedDateString.append(Integer.valueOf(calendar.get(Calendar.MONTH)) + 1);
        formattedDateString.append("/");
        formattedDateString.append(Integer.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
        formattedDateString.append("/");
        formattedDateString.append(Integer.valueOf(calendar.get(Calendar.YEAR)));

        DateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        Date formattedDate = new Date();

        try {
            formattedDate = format.parse(formattedDateString.toString());
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

        String militaryTime = String.format(Locale.US, "%02d", calendar.get(Calendar.HOUR_OF_DAY)) + String.format(Locale.US, "%02d", calendar.get(Calendar.MINUTE));
        return Integer.valueOf(militaryTime);
    }

    /**
     * convertMilitaryTimeToStandardString converts an integer representing military time
     * to standard time and returns the result as a string
     * @param militaryTime integer representing military time
     * @return standard time in string format
     */
    public static String convertMilitaryTimeToStandardString(int militaryTime) {
        Date newDate = militaryTimeToDate(militaryTime);

        DateFormat dateFormatter = new SimpleDateFormat("hh:mm a", Locale.US);
        return dateFormatter.format(newDate);
    }

    /**
     * convertMilitaryTimeToStandardDate converts an integer representing military time to
     * standard time and returns the result as a date
     * @param militaryTime integer representing military time
     * @return standard time in date format
     */
    public static Date convertMilitaryTimeToStandardDate(int militaryTime) {
        return militaryTimeToDate(militaryTime);
    }

    /**
     * militaryTimeToDate converts military time to a date
     * @param militaryTime integer representing military time
     * @return date
     */
    private static Date militaryTimeToDate(int militaryTime) {
        int hours = militaryTime / 100;
        int minutes = militaryTime - (hours * 100);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);

        return calendar.getTime();
    }
}
