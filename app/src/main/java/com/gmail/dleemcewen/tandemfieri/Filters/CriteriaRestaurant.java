package com.gmail.dleemcewen.tandemfieri.Filters;

import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.Interfaces.Criteria;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ruth on 3/25/2017.
 */

public class CriteriaRestaurant implements Criteria {

    String restaurant;

    public CriteriaRestaurant(String restaurant) {
        this.restaurant = restaurant;
    }

    @Override
    public List<Order> meetCriteria(List<Order> orders) {
        List<Order> ordersFromRestaurant = new ArrayList<Order>();
        for(Order order : orders){
            if(order.getRestaurantName().equals(restaurant)){
                ordersFromRestaurant.add(order);
            }
        }
        return ordersFromRestaurant;
    }
}
