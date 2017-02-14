package com.gmail.dleemcewen.tandemfieri.Entities;

import com.gmail.dleemcewen.tandemfieri.Abstracts.Entity;

/**
 * Rating defines all the properties and behaviors for a Rating entity
 */

public class Rating extends Entity {
    private String restaurantId;
    private String orderId;
    private String driverId;
    private int rating;
    private String date;

    /**
     * Default constructor
     */
    public Rating() {
    }

    /**
     * Optional constructor
     *
     * @param id uniquely identifies a rating
     */
    public Rating(String id) {
        setKey(id);
    }

    /**
     * return the rating id
     *
     * @return String uniquely identifying the rating
     */
    public String getId() {
        return getKey();
    }

    /**
     * returns the unique restaurant identifier
     *
     * @return unique identifier of the restaurant
     */
    public String getRestaurantId() {
        return restaurantId;
    }

    /**
     * set the restaurant identifier
     *
     * @param restaurantId uniquely identifies the restaurant
     */
    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    /**
     * returns the unique order identifier
     *
     * @return unique identifier of the order
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * set the order identifier
     *
     * @param orderId uniquely identifies the order
     */
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * returns the unique driver identifier
     *
     * @return unique identifier of the driver
     */
    public String getDriverId() {
        return driverId;
    }

    /**
     * set the driver identifier
     *
     * @param driverId uniquely identifies the driver
     */
    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    /**
     * returns the rating
     *
     * @return integer indicating the rating
     */
    public int getRating() {
        return rating;
    }

    /**
     * set the rating
     *
     * @param rating identifies the rating
     */
    public void setRating(int rating) {
        this.rating = rating;
    }

    /**
     * returns the date of the rating
     * @return string representation of the date of the rating
     */
    public String getDate() {
        return date;
    }

    /**
     * sets the date of the rating
     * @param date identifies the date of the rating
     */
    public void setDate(String date) {
        this.date = date;
    }
}