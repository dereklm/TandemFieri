package com.gmail.dleemcewen.tandemfieri;

import com.gmail.dleemcewen.tandemfieri.Entities.DeliveryHours;
import com.gmail.dleemcewen.tandemfieri.Entities.Day;

/**
 * Created by Ruth on 2/24/2017.
 */

public class DeliveryHoursBuilder {
    private DeliveryHours deliveryHours;

    public DeliveryHoursBuilder() {
        deliveryHours = new DeliveryHours();
    }

    public DeliveryHoursBuilder(String id){
        deliveryHours = new DeliveryHours(id);
    }

    public void add(String name, int open, int close){
        deliveryHours.add(new Day(name, open, close, true));
    }

    public void add(String name){
        deliveryHours.add(new Day(name, false));
    }

    public DeliveryHours getDeliveryHours() {
        return deliveryHours;
    }
}
