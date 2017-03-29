package com.gmail.dleemcewen.tandemfieri.Interfaces;

import android.os.Bundle;

import java.util.List;

/**
 * IPublish defines the interface used by all publishers
 */
public interface IPublish {
    /**
     * subscribe registers a new subscriber
     * @param subscriber identifies the subscriber to register
     */
    void subscribe(ISubscriber subscriber);

    /**
     * unsubscribe removes an existing subscriber
     * @param subscriber identifies the subscriber to remove
     */
    void unsubscribe(ISubscriber subscriber);

    /**
     * getSubscribers returns a list of the current subscribers
     */
    List<ISubscriber> getSubscribers();

    /**
     * notifySubscribers notifies all of the subscribers whose criteria match those of the notification
     * @param notification identifies the new notification data
     */
    void notifySubscribers(Bundle notification);
}
