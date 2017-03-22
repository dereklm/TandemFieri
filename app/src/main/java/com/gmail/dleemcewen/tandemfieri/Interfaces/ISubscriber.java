package com.gmail.dleemcewen.tandemfieri.Interfaces;

import android.os.Bundle;

import com.gmail.dleemcewen.tandemfieri.Filters.SubscriberFilter;

import java.util.List;
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
     * getFilters returns the record filters supplied by the subscriber
     * @return record filters
     */
    List<SubscriberFilter> getFilters();

    /**
     * update provides the subscriber with updated information
     * @param notification indicates the notification
     */
    void update(Bundle notification);
}
