package com.gmail.dleemcewen.tandemfieri.Publishers;

import android.os.Bundle;

import com.gmail.dleemcewen.tandemfieri.Interfaces.IPublish;
import com.gmail.dleemcewen.tandemfieri.Interfaces.ISubscriber;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NotificationPublisher is a singleton object that maintains and notifies subscribers as necessary
 */

public class NotificationPublisher implements IPublish {
    private List<ISubscriber> subscribers;

    //create an object of NotificationPublisher
    private static NotificationPublisher instance = new NotificationPublisher();

    //make the constructor private so that this class cannot be
    //instantiated
    private NotificationPublisher() {
        subscribers = new ArrayList<>();
    }

    //Get the only object available
    public static NotificationPublisher getInstance(){
        return instance;
    }

    /**
     * subscribe adds a new subscriber to the list of subscribers
     * @param subscriber indicates the subscriber to add
     */
    @Override
    public void subscribe(ISubscriber subscriber) {
        subscribers.add(subscriber);
    }

    /**
     * unsubscribe removes an existing subscriber from the list of subscribers
     * @param subscriber indicates the subscriber to remove
     */
    @Override
    public void unsubscribe(ISubscriber subscriber) {
        subscribers.remove(subscriber);
    }

    /**
     * notifySubscribers notifies the appropriate subscribers that there is new information available
     */
    @Override
    public void notifySubscribers(Bundle notification) {
        for (ISubscriber subscriber : subscribers) {
            Map.Entry<String, List<Object>> filter = subscriber.getFilter();

            if (notification.getString("notificationType").equals(subscriber.getNotificationType())) {
                if (filter != null) {
                    Object entity = notification.getSerializable("entity");
                    HashMap entityHashMap = (HashMap)entity;

                    Object entityValue = entityHashMap.get(filter.getKey());

                    if (entityValue != null &&
                        filter.getValue().contains(entityValue)) {
                        entityHashMap.put("notificationId", notification.getString("notificationId"));
                        subscriber.update(notification);
                    }
                } else {
                    subscriber.update(notification);
                }
            }
        }
    }
}
