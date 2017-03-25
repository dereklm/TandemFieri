package com.gmail.dleemcewen.tandemfieri.Filters;

import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.Entities.OrderItem;
import com.gmail.dleemcewen.tandemfieri.Interfaces.Criteria;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ruth on 3/21/2017.
 */

public class CriteriaSelectedItem implements Criteria {
    String item;
    boolean itemHasBeenOrdered = false;

    public CriteriaSelectedItem(String item) {
        this.item = item;
    }

    @Override
    public List<Order> meetCriteria(List<Order> orders) {
        //returns all the orders which contain the selected item
        List<Order> selectedItems = new ArrayList<Order>();
        for (Order order : orders) {
            for(OrderItem orderItem : order.getItems()){
                if(orderItem.getName().equals(item)){
                    itemHasBeenOrdered = true;
                }
            }
            if(itemHasBeenOrdered){
                selectedItems.add(order);
            }
            itemHasBeenOrdered = false;
        }
        return selectedItems;
    }
}
