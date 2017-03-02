package com.gmail.dleemcewen.tandemfieri.menubuilder;

import java.io.Serializable;

/**
 * Created by jfly1_000 on 2/13/2017.
 */
public class OptionSelection implements Serializable {
    private String SelectionName;
    private  double addedPrice;
    private String description;

    public OptionSelection(){

    }


    public String getSelectionName() {
        return SelectionName;
    }

    public void setSelectionName(String selectionName) {
        SelectionName = selectionName;
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
