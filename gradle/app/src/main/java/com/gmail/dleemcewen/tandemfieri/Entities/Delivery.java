package com.gmail.dleemcewen.tandemfieri.Entities;

import com.gmail.dleemcewen.tandemfieri.Abstracts.Entity;

import java.io.Serializable;
import java.util.List;

/**
 * Delivery identifies a delivery for a driver
 */

public class Delivery extends Entity implements Serializable {
    private List<Order> order;
    private String currentOrderId;

    /**
     * Default constructor
     */
    public Delivery() {
    }

    /**
     * getOrder returns the orders associated with the delivery
     * @return order associated with the delivery
     */
    public List<Order> getOrder() {
        return order;
    }

    /**
     * setOrder sets the orders associated with the delivery
     * @param order indicates the orders associated with the delivery
     */
    public void setOrder(List<Order> order) {
        this.order = order;
    }

    /**
     * getCurrentOrderId returns the current order id
     * @return the id of the driver's current order
     */
    public String getCurrentOrderId() {
        return currentOrderId;
    }

    /**
     * setCurrentOrderId sets the current order id for a driver to be able to identify which order
     * is the driver's current order
     * @param currentOrderId indicates the current order id
     */
    public void setCurrentOrderId(String currentOrderId) {
        this.currentOrderId = currentOrderId;
    }

}
