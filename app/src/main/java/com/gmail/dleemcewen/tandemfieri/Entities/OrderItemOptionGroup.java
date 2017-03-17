package com.gmail.dleemcewen.tandemfieri.Entities;

import com.gmail.dleemcewen.tandemfieri.menubuilder.ItemOption;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Derek on 3/5/2017.
 */

public class OrderItemOptionGroup implements Serializable {

    private String name;
    private List<OrderItemOption> options;
    private boolean required;
    private boolean exclusive;
    private boolean hasChildSelected;

    /**
     * Empty constructor required for firebase deserialization
     */
    public OrderItemOptionGroup() {}

    public OrderItemOptionGroup(ItemOption itemOption) {
        options = new ArrayList<>();
        this.name = itemOption.getOptionName();
        required = itemOption.isRequired();
        exclusive = !itemOption.isAndRelationship();
        this.hasChildSelected = false;
    }

    public void addOption(OrderItemOption option) {
        options.add(option);
    }


    public boolean isHasChildSelected() {
        return hasChildSelected;
    }

    public void setHasChildSelected(boolean hasChildSelected) {
        this.hasChildSelected = hasChildSelected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<OrderItemOption> getOptions() {
        return options;
    }

    public void setOptions(List<OrderItemOption> options) {
        this.options = options;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isExclusive() {
        return exclusive;
    }

    public void setExclusive(boolean exclusive) {
        this.exclusive = exclusive;
    }
}
