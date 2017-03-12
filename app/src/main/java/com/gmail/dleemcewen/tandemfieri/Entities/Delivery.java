package com.gmail.dleemcewen.tandemfieri.Entities;

import com.gmail.dleemcewen.tandemfieri.Abstracts.Entity;

import java.io.Serializable;

/**
 * Delivery identifies a delivery for a driver
 */

public class Delivery extends Entity implements Serializable {
    private String customerId;
    private Boolean isCurrentOrder;

    /**
     * Default constructor
     */
    public Delivery() {
    }

    /**
     * getCustomerId returns the customer id associated with the delivery
     * @return customer id associated with the delivery
     */
    public String getCustomerId() {
        return customerId;
    }

    /**
     * setCustomerId sets the customer id associated with the delivery
     * @param customerId uniquely identifies the customer associated with the delivery
     */
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    /**
     * getIsCurrentOrder returns the IsCurrentOrder flag
     * @return true or false
     */
    public Boolean getIsCurrentOrder() {
        return isCurrentOrder;
    }

    /**
     * setIsCurrentOrder sets the IsCurrentOrder flag that identifies which order is the current order
     * @param isCurrentOrder indicates if the order is the current order
     */
    public void setIsCurrentOrder(Boolean isCurrentOrder) {
        this.isCurrentOrder = isCurrentOrder;
    }

}
