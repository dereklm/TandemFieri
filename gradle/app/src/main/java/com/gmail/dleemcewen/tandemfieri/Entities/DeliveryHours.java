package com.gmail.dleemcewen.tandemfieri.Entities;

import com.gmail.dleemcewen.tandemfieri.Abstracts.Entity;
import com.google.firebase.database.Exclude;

import java.util.ArrayList;

import static java.lang.String.valueOf;

/**
 * Created by Ruth on 2/24/2017.
 */

public class DeliveryHours extends Entity{
    ArrayList<Day> days;
    String restaurantId;

    public DeliveryHours() {
        days = new ArrayList<>();
    }

    public DeliveryHours(String id){
        this.restaurantId = id;
        days = new ArrayList<>();

    }

    public void add(Day day){
        days.add(day);
    }

    public ArrayList<Day> getDays() {
        return days;
    }

    public void setDays(ArrayList<Day> days) {
        this.days = days;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    @Exclude
    public boolean isComplete(){
        return days.size() == 7;
    }

    @Exclude
    public String toString(){
        return valueOf(days.size());
    }
}
