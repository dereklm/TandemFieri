package com.gmail.dleemcewen.tandemfieri.Interfaces;

import java.util.Map;

/**
 * IPublish defines the interface used by all publishers
 */
public interface IPublish {
    void subscribe(ISubscriber subscriber);
    void unsubscribe(ISubscriber subscriber);
    void notifySubscribers(Map.Entry<String, Object> changedEntry);
}
