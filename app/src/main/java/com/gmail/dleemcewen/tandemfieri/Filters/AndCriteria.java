package com.gmail.dleemcewen.tandemfieri.Filters;

import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.Interfaces.Criteria;

import java.util.List;

/**
 * Created by Ruth on 3/16/2017.
 */

public class AndCriteria implements Criteria {

    private Criteria criteria;
    private Criteria otherCriteria;

    public AndCriteria(Criteria criteria, Criteria otherCriteria) {
        this.criteria = criteria;
        this.otherCriteria = otherCriteria;
    }

    @Override
    public List<Order> meetCriteria(List<Order> orders) {

        List<Order> firstCriteriaPersons = criteria.meetCriteria(orders);
        return otherCriteria.meetCriteria(firstCriteriaPersons);
    }
}
