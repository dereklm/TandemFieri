package com.gmail.dleemcewen.tandemfieri.Publishers;

import com.gmail.dleemcewen.tandemfieri.Interfaces.IPublish;
import com.gmail.dleemcewen.tandemfieri.Interfaces.ISubscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Publisher is a singleton object that maintains and notifies subscribers as necessary
 */

public class Publisher implements IPublish {
    private List<ISubscriber> subscribers;

    //create an object of Publisher
    private static Publisher instance = new Publisher();

    //make the constructor private so that this class cannot be
    //instantiated
    private Publisher() {
        subscribers = new ArrayList<>();
    }

    //Get the only object available
    public static Publisher getInstance(){
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
     * notifySubscribers notifies all of the subscribers that there is new information available
     */
    @Override
    public void notifySubscribers(Map.Entry<String, Object> changedEntry) {
        for (ISubscriber subscriber : subscribers) {
            if (changedEntry.getKey().equals(subscriber.getType())) {
                subscriber.update();
            }
        }
    }
}
