package com.gmail.dleemcewen.tandemfieri.Entities;

import com.gmail.dleemcewen.tandemfieri.menubuilder.ItemOption;
import com.gmail.dleemcewen.tandemfieri.menubuilder.MenuItem;
import com.gmail.dleemcewen.tandemfieri.menubuilder.OptionSelection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Derek on 3/5/2017.
 */

public class OrderItem implements Serializable {

    private String name;
    private double basePrice;
    private List<OrderItemOptionGroup> optionGroups;

    /**
     * Empty constructor required for firebase deserialization
     */
    public OrderItem() {}

    public OrderItem(MenuItem menuItem) {
        basePrice = 0;
        this.name = menuItem.getName();
        this.basePrice = menuItem.getBasePrice();
        optionGroups = new ArrayList<>();
    }

    public void addOptionGroup (ItemOption optionGroup) {
        this.optionGroups.add(new OrderItemOptionGroup(optionGroup));
    }

    public void addOption(ItemOption itemOption, List<OptionSelection> selections) {
        boolean optionExists = false;
        int optionIndex = -1;
        for(int i = 0; i < optionGroups.size(); i++) {
            if (optionGroups.get(i).getName().equals(itemOption.getOptionName())) {
                optionExists = true;
                optionIndex = i;
            }
        }
        // Group doesn't exist, create group and add all selections to group.
        if (!optionExists) {
            optionGroups.add(new OrderItemOptionGroup(itemOption));
            for (OptionSelection selection : selections) {
                optionGroups.get(optionGroups.size() - 1).addOption(new OrderItemOption(selection));
            }
        } else {
            // Group exists, only add options that don't exist already

            boolean selectionExists = false;
            List<OrderItemOption> itemOptionSelections = optionGroups.get(optionIndex).getOptions();

            for (int i = 0; i < selections.size(); i++) {
                for (int j = 0; j < itemOptionSelections.size(); j++) {
                    if (selections.get(i).getSelectionName().equals(itemOptionSelections.get(j).getName())) {
                        optionExists = true;
                        break;
                    }
                }
                if (!optionExists) optionGroups.get(optionIndex).addOption(new OrderItemOption(selections.get(i)));
            }
        }

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public List<OrderItemOptionGroup> getOptionGroups() {
        return optionGroups;
    }

    public void setOptionGroups(List<OrderItemOptionGroup> optionGroups) {
        this.optionGroups = optionGroups;
    }
}
