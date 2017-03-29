package com.gmail.dleemcewen.tandemfieri.Publishers;

import android.os.Bundle;

import com.gmail.dleemcewen.tandemfieri.Filters.SubscriberFilter;
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
    public static NotificationPublisher getInstance() {
        return instance;
    }

    /**
     * subscribe adds a new subscriber to the list of subscribers
     *
     * @param subscriber indicates the subscriber to add
     */
    @Override
    public void subscribe(ISubscriber subscriber) {
        subscribers.add(subscriber);
    }

    /**
     * unsubscribe removes an existing subscriber from the list of subscribers
     *
     * @param subscriber indicates the subscriber to remove
     */
    @Override
    public void unsubscribe(ISubscriber subscriber) {
        subscribers.remove(subscriber);
    }

    /**
     * getSubscribers returns a list of the current subscribers
     *
     * @return list of the current subscribers
     */
    @Override
    public List<ISubscriber> getSubscribers() {
        return subscribers;
    }

    /**
     * notifySubscribers notifies the appropriate subscribers that there is new information available
     */
    @Override
    public void notifySubscribers(Bundle notification) {
        for (ISubscriber subscriber : subscribers) {
            List<SubscriberFilter> filters = subscriber.getFilters();

            if (notification.getString("notificationType").equals(subscriber.getNotificationType())) {
                if (!filters.isEmpty()) {
                    List<Boolean> filterResults = new ArrayList<>();

                    Object entity = notification.getSerializable("entity");
                    HashMap entityHashMap = (HashMap)entity;

                    for (int index = 0; index < filters.size(); index++) {
                        Object entityValue = entityHashMap.get(filters.get(index).getField());

                        if (entityValue != null) {
                            filterResults.add(filters.get(index).getValues().contains(entityValue));
                        }
                    }

                    if (!filterResults.contains(false)) {
                        entityHashMap.put("notificationId", notification.getString("notificationId"));
                        entityHashMap.put("userId", notification.getString("userId"));
                        subscriber.update(notification);
                    }
                } else {
                    subscriber.update(notification);
                }
            }
        }
    }
}
