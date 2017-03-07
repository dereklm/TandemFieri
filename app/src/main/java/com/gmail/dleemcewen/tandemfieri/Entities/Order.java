package com.gmail.dleemcewen.tandemfieri.Entities;

import com.gmail.dleemcewen.tandemfieri.Abstracts.Entity;
import com.gmail.dleemcewen.tandemfieri.Constants.OrderConstants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Derek on 3/1/2017.
 */

public class Order extends Entity implements Serializable {
    private List<OrderItem> items = new ArrayList<>();
    private int orderId;
    private int restaurantId;
    private int customerId;
    private double subTotal;
    private double tax;
    private double total;

    public Order() {}

    public double calculateSubTotal() {
        double subTotal = 0;
        for (OrderItem item : items) {
            subTotal += item.getBasePrice();
            for (OrderItemOptionGroup group : item.getOptionGroups()) {
                for (OrderItemOption selection : group.getOptions()) {
                    subTotal += selection.getAddedPrice();
                }
            }
        }
        return subTotal;
    }

    public void addItem(OrderItem item) {
        items.add(item);
        this.subTotal = calculateSubTotal();
        this.tax = calculateTax();
        this.total = calculateTotal();
    }

    public double calculateTax() {
        return this.subTotal * OrderConstants.TAX_RATE;
    }

    public double calculateTotal() {
        return this.subTotal + this.tax;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(int restaurantId) {
        this.restaurantId = restaurantId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(double subTotal) {
        this.subTotal = subTotal;
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
