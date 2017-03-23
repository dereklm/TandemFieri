package com.gmail.dleemcewen.tandemfieri;

/**
 * Created by Ruth on 3/16/2017.
 */

public class DisplayItem {
    String name;
    double basePrice;
    double total;

    public DisplayItem(String name, double basePrice) {
        this.name = name;
        this.basePrice = basePrice;
        this.total = this.basePrice;
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

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public void addOne() {
        //and base price to total
        setTotal(this.total + this.basePrice);
    }

    public String toString(){
        return name + " " + basePrice + " " + total;
    }
}