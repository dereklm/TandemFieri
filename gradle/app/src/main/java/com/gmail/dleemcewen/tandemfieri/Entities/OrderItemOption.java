package com.gmail.dleemcewen.tandemfieri.Entities;

import com.gmail.dleemcewen.tandemfieri.menubuilder.OptionSelection;

import java.io.Serializable;

/**
 * Created by Derek on 3/5/2017.
 */

public class OrderItemOption implements Serializable {
    private String name;
    private double addedPrice;
    private String description;
    private boolean selected;

    /**
     * Empty constructor required for firebase deserialization
     */
    public OrderItemOption() {}

    public OrderItemOption(OptionSelection optionSelection) {
        this.name = optionSelection.getSelectionName();
        this.addedPrice = optionSelection.getAddedPrice();
        this.description = optionSelection.getDescription();
        this.selected = false;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
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
