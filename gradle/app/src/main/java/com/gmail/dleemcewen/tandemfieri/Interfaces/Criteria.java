package com.gmail.dleemcewen.tandemfieri.Interfaces;

/**
 * Created by Ruth on 3/16/2017.
 */

import com.gmail.dleemcewen.tandemfieri.Entities.Order;

import java.util.List;

public interface Criteria {
    public List<Order> meetCriteria(List<Order> orders);
}
