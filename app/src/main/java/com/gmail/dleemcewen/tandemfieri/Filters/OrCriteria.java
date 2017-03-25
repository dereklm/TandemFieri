package com.gmail.dleemcewen.tandemfieri.Filters;

import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.Interfaces.Criteria;

import java.util.List;

/**
 * Created by Ruth on 3/16/2017.
 */

public class OrCriteria implements Criteria {

    private Criteria criteria;
    private Criteria otherCriteria;

    public OrCriteria(Criteria criteria, Criteria otherCriteria) {
        this.criteria = criteria;
        this.otherCriteria = otherCriteria;
    }

    @Override
    public List<Order> meetCriteria(List<Order> orders) {
        List<Order> firstCriteriaItems = criteria.meetCriteria(orders);
        List<Order> otherCriteriaItems = otherCriteria.meetCriteria(orders);

        for (Order order : otherCriteriaItems) {
            if(!firstCriteriaItems.contains(order)){
                firstCriteriaItems.add(order);
            }
        }
        return firstCriteriaItems;
    }
}
