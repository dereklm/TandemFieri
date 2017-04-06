package com.gmail.dleemcewen.tandemfieri.Builders;


import android.content.Context;

import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.Enums.OrderEnum;
import com.gmail.dleemcewen.tandemfieri.EventListeners.SubscriberBuilderCompleteListener;
import com.gmail.dleemcewen.tandemfieri.Filters.SubscriberFilter;
import com.gmail.dleemcewen.tandemfieri.Interfaces.ISubscriber;
import com.gmail.dleemcewen.tandemfieri.Subscribers.DinerSubscriber;

import java.util.ArrayList;
import java.util.List;

/**
 * DinerSubscriberBuilder abstracts the logic used to build a new diner subscriber that can be registered
 * with a NotificationPublisher
 */

public class DinerSubscriberBuilder {
    public static void Build(final User diner,
                             final Context context,
                             final SubscriberBuilderCompleteListener<DinerSubscriber> subscriberBuilderCompleteListener) {
        List<ISubscriber> subscribers = new ArrayList<>();

        List<Object> customerIds = new ArrayList<>();
        customerIds.add(diner.getAuthUserID());

        List<Object> refundedOrderStatuses = new ArrayList<>();
        refundedOrderStatuses.add(OrderEnum.REFUNDED.toString());

        List<SubscriberFilter> refundedSubscriberFilters = new ArrayList<>();
        refundedSubscriberFilters.add(new SubscriberFilter("customerId", customerIds));
        refundedSubscriberFilters.add(new SubscriberFilter("status", refundedOrderStatuses));

        ISubscriber refundSubscriber = new DinerSubscriber(
                context,
                diner,
                refundedSubscriberFilters);

        subscribers.add(refundSubscriber);

        List<Object> completedOrderStatuses = new ArrayList<>();
        completedOrderStatuses.add(OrderEnum.COMPLETE.toString());

        List<SubscriberFilter> completedSubscriberFilters = new ArrayList<>();
        completedSubscriberFilters.add(new SubscriberFilter("customerId", customerIds));
        completedSubscriberFilters.add(new SubscriberFilter("status", completedOrderStatuses));

        ISubscriber completedSubscriber = new DinerSubscriber(
                context,
                diner,
                completedSubscriberFilters);

        subscribers.add(completedSubscriber);

        subscriberBuilderCompleteListener.onBuildComplete(subscribers);
    }
}
