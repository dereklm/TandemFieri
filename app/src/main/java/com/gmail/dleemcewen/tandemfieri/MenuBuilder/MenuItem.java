package com.gmail.dleemcewen.tandemfieri.menubuilder;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by jfly1_000 on 2/13/2017.
 */

public class MenuItem extends MenuCompenet implements Serializable {

    private double basePrice;
    private ArrayList<ItemOption> options;

    public MenuItem(){

    }


    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public ArrayList<ItemOption> getOptions() {
        if(options!=null)return options;
        options = new ArrayList<>();
        return options;
    }

    public void setOptions(ArrayList<ItemOption> options) {
        this.options = options;
    }

    public String toString(){
        return super.getName();
    }
}

