package com.gmail.dleemcewen.tandemfieri.Entities;

import com.gmail.dleemcewen.tandemfieri.Abstracts.Entity;

import java.io.Serializable;

/**
 * NotificationMessage defines all the properties and behaviors for a NotificationMessage entity
 */

public class NotificationMessage extends Entity implements Serializable {
    private String action;
    private String notificationType;
    private Object data;
    private String notificationId;
    private String userId;

    /**
     * Default constructor
     */
    public NotificationMessage() {}

    /**
     * getAction returns the action associated with the notification message
     * @return action
     */
    public String getAction() {
        return action;
    }

    /**
     * setAction sets the action associated with the notification message
     * @param action indicates the action to associate with the notification message
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * getData returns the data associated with the notification message
     * @return data
     */
    public Object getData() {
        return data;
    }

    /**
     * setData sets the data associated with the notification message
     * @param data indicates the data associated with the notification message
     */
    public void setData(Object data, Class dataType) {
        this.data = dataType.cast(data);
    }

    /**
     * getNotificationType returns the notification type associated with the notification message
     * @return notification type
     */
    public String getNotificationType() {
        return notificationType;
    }

    /**
     * setNotificationType sets the notification type to be associated with the notification message
     * @param notificationType indicates the notification type to be associated with the notification message
     */
    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    /**
     * getNotificationId returns the notification id of the notification message
     * @return notification id
     */
    public String getNotificationId() {
        return notificationId;
    }

    /**
     * setNotificationId sets the notification id to be associated with the notification message
     * @param notificationId indicates the notification id to be associated with the notification message
     */
    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    /**
     * getUserId returns the userid associated with the notification message
     * @return userid associated with the notification message
     */
    public String getUserId() {
        return userId;
    }

    /**
     * setUserId sets the userid to be associated with the notification message
     * @param userId indicates the user id to be associated with the notification message
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
}
