package com.gmail.dleemcewen.tandemfieri.Builders;

import android.content.Context;
import android.support.annotation.NonNull;

import com.gmail.dleemcewen.tandemfieri.Entities.Restaurant;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.Enums.OrderEnum;
import com.gmail.dleemcewen.tandemfieri.EventListeners.SubscriberBuilderCompleteListener;
import com.gmail.dleemcewen.tandemfieri.Filters.SubscriberFilter;
import com.gmail.dleemcewen.tandemfieri.Interfaces.ISubscriber;
import com.gmail.dleemcewen.tandemfieri.Repositories.Restaurants;
import com.gmail.dleemcewen.tandemfieri.Subscribers.RestaurantSubscriber;
import com.gmail.dleemcewen.tandemfieri.Tasks.TaskResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * RestaurantSubscriberBuilder abstracts the logic used to build a new restaurant subscriber that can be registered
 * with a NotificationPublisher
 */

public class RestaurantSubscriberBuilder {
    public static void Build(final User restaurantOwner, final Context context,
                                             final SubscriberBuilderCompleteListener<RestaurantSubscriber> subscriberBuilderCompleteListener) {
        Restaurants<Restaurant> restaurants = new Restaurants<>(context);

        restaurants
            .find("ownerId = '" + restaurantOwner.getAuthUserID() + "'")
            .addOnCompleteListener(new OnCompleteListener<TaskResult<Restaurant>>() {
                @Override
                public void onComplete(@NonNull Task<TaskResult<Restaurant>> task) {
                    List<Restaurant> restaurants = task.getResult().getResults();

                    List<Object> restaurantIds = new ArrayList<>();
                    for (Restaurant ownerRestaurant : restaurants) {
                        restaurantIds.add(ownerRestaurant.getKey());
                    }

                    List<Object> orderStatuses = new ArrayList<>();
                    orderStatuses.add(OrderEnum.CREATING.toString());

                    List<SubscriberFilter> subscriberFilters = new ArrayList<>();
                    subscriberFilters.add(new SubscriberFilter("restaurantId", restaurantIds));
                    subscriberFilters.add(new SubscriberFilter("status", orderStatuses));

                    ISubscriber subscriber = new RestaurantSubscriber(
                            context,
                            restaurantOwner,
                            subscriberFilters);

                    List<ISubscriber> subscribers = new ArrayList<>();
                    subscribers.add(subscriber);

                    subscriberBuilderCompleteListener.onBuildComplete(subscribers);
                }
            });
    }
}
