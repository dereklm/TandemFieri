package com.gmail.dleemcewen.tandemfieri;

/**
 * Created by Ruth on 3/16/2017.
 */

public class DisplayItem {
    String name;
    double basePrice;
    double total;
    int quantity;

    public DisplayItem(String name, double basePrice) {
        this.name = name;
        this.basePrice = basePrice;
        this.total = this.basePrice;
        this.quantity = 0;
    }

    public DisplayItem(String name){
        this.name = name;
        this.quantity = 0;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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
        this.quantity = getQuantity() + 1;
        setTotal(getQuantity() * getBasePrice());
    }

    public String toString(){
        return name + " " + quantity + " " + basePrice + " " + total;
    }
}