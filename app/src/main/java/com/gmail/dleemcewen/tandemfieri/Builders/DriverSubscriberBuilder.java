package com.gmail.dleemcewen.tandemfieri.Builders;

import android.content.Context;

import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.Enums.OrderEnum;
import com.gmail.dleemcewen.tandemfieri.EventListeners.SubscriberBuilderCompleteListener;
import com.gmail.dleemcewen.tandemfieri.Filters.SubscriberFilter;
import com.gmail.dleemcewen.tandemfieri.Interfaces.ISubscriber;
import com.gmail.dleemcewen.tandemfieri.Subscribers.DriverSubscriber;

import java.util.ArrayList;
import java.util.List;

/**
 * DriverSubscriberBuilder abstracts the logic used to build a new driver subscriber that can be registered
 * with a NotificationPublisher
 */

public class DriverSubscriberBuilder {
    public static void Build(final User driver,
                             final Context context,
                             final SubscriberBuilderCompleteListener<DriverSubscriber> subscriberBuilderCompleteListener) {

        List<ISubscriber> subscribers = new ArrayList<>();

        List<Object> restaurantIds = new ArrayList<>();
        restaurantIds.add(driver.getRestaurantId());

        List<Object> enrouteOrderStatuses = new ArrayList<>();
        enrouteOrderStatuses.add(OrderEnum.EN_ROUTE.toString());

        List<SubscriberFilter> enrouteSubscriberFilters = new ArrayList<>();
        enrouteSubscriberFilters.add(new SubscriberFilter("restaurantId", restaurantIds));
        enrouteSubscriberFilters.add(new SubscriberFilter("status", enrouteOrderStatuses));

        ISubscriber enrouteSubscriber = new DriverSubscriber(
                context,
                driver,
                enrouteSubscriberFilters);

        subscribers.add(enrouteSubscriber);

        List<Object> refundedOrderStatuses = new ArrayList<>();
        refundedOrderStatuses.add(OrderEnum.REFUNDED.toString());

        List<SubscriberFilter> refundedSubscriberFilters = new ArrayList<>();
        refundedSubscriberFilters.add(new SubscriberFilter("restaurantId", restaurantIds));
        refundedSubscriberFilters.add(new SubscriberFilter("status", refundedOrderStatuses));

        ISubscriber refundedSubscriber = new DriverSubscriber(
                context,
                driver,
                refundedSubscriberFilters);

        subscribers.add(refundedSubscriber);

        subscriberBuilderCompleteListener.onBuildComplete(subscribers);
    }
}
