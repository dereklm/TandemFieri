package com.gmail.dleemcewen.tandemfieri.Filters;

import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.Interfaces.Criteria;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Ruth on 3/16/2017.
 */

public class CriteriaAfterStartDate implements Criteria {
    Date date;

    public CriteriaAfterStartDate(Date date) {
        this.date = date;
    }

    @Override
    public List<Order> meetCriteria(List<Order> orders) {
        List<Order> ordersAfterStartDate = new ArrayList<Order>();

        for (Order order : orders) {
            Date orderDate = order.getOrderDate();
            if(orderDate.equals(date) || orderDate.after(date)){
                ordersAfterStartDate.add(order);
            }
        }
        return ordersAfterStartDate;
    }
}
