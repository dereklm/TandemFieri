package com.gmail.dleemcewen.tandemfieri.Entities;

import com.gmail.dleemcewen.tandemfieri.Formatters.DateFormatter;
import com.google.firebase.database.Exclude;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Ruth on 2/24/2017.
 */

public class Day {
    String name;
    int hourOpen;   //military time
    int hourClosed; //military time

    boolean open;

    public Day() {
    }

    public Day(String name, boolean isOpen) {
        this.name = name;
        this.open = isOpen;
    }

    public Day(String name, int hourOpen, int hourClosed, boolean isOpen) {
        this.name = name;
        this.hourOpen = hourOpen;
        this.hourClosed = hourClosed;
        this.open = isOpen;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHourOpen() {
        return hourOpen;
    }

    public void setHourOpen(int hourOpen) {
        this.hourOpen = hourOpen;
    }

    public int getHourClosed() {
        return hourClosed;
    }

    public void setHourClosed(int hourClosed) {
        this.hourClosed = hourClosed;
    }

    public boolean getOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    /*return the hours of delivery similar to military format
    for example if delivery duration is 4hr 30min this will return 430
    12 hrs 0 min will return 1200
    * */

    @Exclude
    public int calculateDeliveryHoursDuration (){
        int m1 = hourOpen % 100;
        int m2 = hourClosed % 100;
        int h1 = (hourOpen - m1)/100;
        int h2 = (hourClosed - m2)/100;
        int dh = h2 - h1;
        int dm = m2 - m1;
        if (dm < 0){
            dm += 60;
            dh -= 1;
        }
        if(dh < 0){
            dh += 24;
        }
        return dh * 100 + dm;
    }

    @Override
    public String toString() {
        return "Day{" +
                "name='" + name + '\'' +
                ", hourOpen='" + hourOpen + '\'' +
                ", hourClosed='" + hourClosed + '\'' +
                ", isOpen=" + open +
                '}';
    }

    /**
     * compareOpenTimeWithCurrentTime compares the defined open time with the current time
     * to determine if the restaurant is currently open
     * @param currentDate indicates the current date/time
     * @return true or false
     */
    @Exclude
    public boolean compareOpenTimeWithCurrentTime(Date currentDate) {
        int currentMilitaryTime = DateFormatter.convertStandardTimeToMilitaryTime(currentDate);
        return currentMilitaryTime > this.getHourOpen();
    }

    /**
     * compareClosedTimeWithCurrentTime compares the defined closed time with the current date/time
     * to determine if the restaurant is currently closed
     * @param currentDate indicates the current date/time
     * @return true or false
     */
    @Exclude
    public boolean compareClosedTimeWithCurrentTime(Date currentDate) {
        Date closedTime = DateFormatter.convertMilitaryTimeToStandardDate(this.getHourClosed());

        //if the restaurant's closing time is earlier than its opening time
        //then consider the closing date to be the next day
        if (this.getHourClosed() < this.getHourOpen()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(closedTime);
            int day = calendar.get(Calendar.DAY_OF_YEAR);
            calendar.set(Calendar.DAY_OF_YEAR, ++day);
            closedTime = calendar.getTime();
        }
        return closedTime.after(currentDate);
    }
}