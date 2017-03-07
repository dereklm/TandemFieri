package com.gmail.dleemcewen.tandemfieri.Interfaces;

import android.os.Bundle;

import java.util.Map;

/**
 * ISubscriber describes the interface used by all subscribers
 */

public interface ISubscriber {
    /**
     * getNotificationType returns the type of notifications the subscriber wishes to receive
     * @return notification type
     */
    String getNotificationType();

    /**
     * getFilter returns the record filter supplied by the subscriber
     * @return record filter
     */
    Map.Entry<String, String> getFilter();

    /**
     * update provides the subscriber with updated information
     * @param notification indicates the notification
     */
    void update(Bundle notification);
}
