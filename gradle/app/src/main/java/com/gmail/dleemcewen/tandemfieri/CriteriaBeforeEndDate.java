package com.gmail.dleemcewen.tandemfieri;

import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.Interfaces.Criteria;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Ruth on 3/16/2017.
 */

public class CriteriaBeforeEndDate implements Criteria {

    Date date;

    public CriteriaBeforeEndDate(Date date) {
        this.date = date;
    }

    @Override
    public List<Order> meetCriteria(List<Order> orders) {
        List<Order> ordersBeforeEndDate = new ArrayList<Order>();

        for (Order order : orders) {
            Date orderDate = order.getOrderDate();
            if(orderDate.equals(date) || orderDate.before(date)){
                ordersBeforeEndDate.add(order);
            }
        }
        return ordersBeforeEndDate;
    }
}
