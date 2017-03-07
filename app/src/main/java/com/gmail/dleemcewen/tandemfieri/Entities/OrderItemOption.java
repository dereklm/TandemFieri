package com.gmail.dleemcewen.tandemfieri.Entities;

import com.gmail.dleemcewen.tandemfieri.menubuilder.OptionSelection;

/**
 * Created by Derek on 3/5/2017.
 */

public class OrderItemOption {
    private String name;
    private double addedPrice;
    private String description;

    public OrderItemOption(OptionSelection optionSelection) {
        this.name = optionSelection.getSelectionName();
        this.addedPrice = optionSelection.getAddedPrice();
        this.description = optionSelection.getDescription();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAddedPrice() {
        return addedPrice;
    }

    public void setAddedPrice(double addedPrice) {
        this.addedPrice = addedPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}