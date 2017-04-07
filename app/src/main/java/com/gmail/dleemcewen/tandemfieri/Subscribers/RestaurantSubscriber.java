package com.gmail.dleemcewen.tandemfieri.Subscribers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;

import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.Filters.SubscriberFilter;
import com.gmail.dleemcewen.tandemfieri.Interfaces.ISubscriber;
import com.gmail.dleemcewen.tandemfieri.R;
import com.gmail.dleemcewen.tandemfieri.RestaurantMainMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * RestaurantSubscriber identifies a restaurant that has subscribed to notification messages
 */

public class RestaurantSubscriber implements ISubscriber {
    private Context context;
    private List<SubscriberFilter> filters;
    private User restaurantUser;
    private static final String notificationType = "Order";

    /**
     * Default constructor
     * @param context indicates the current application context
     * @param restaurantUser identifies the restaurant user
     * @param filters indicates the record filters supplied by the subscriber
     */
    public RestaurantSubscriber(Context context, User restaurantUser, List<SubscriberFilter> filters) {
        this.context = context;
        this.restaurantUser = restaurantUser;
        this.filters = filters;
    }

    @Override
    public String getNotificationType() {
        return notificationType;
    }

    @Override
    public List<SubscriberFilter> getFilters() {
        return filters;
    }

    @Override
    public User getUser() {
        return restaurantUser;
    }

    @Override
    public void update(Bundle notification) {
        if (context.getResources().getBoolean(R.bool.send_notifications)) {
            HashMap notificationData = ((HashMap)notification.getSerializable("entity"));

            StringBuilder contentTextBuilder = new StringBuilder();
            contentTextBuilder.append("Order received for ");
            contentTextBuilder.append(String.format("$%.2f", notificationData.get("total")));

            StringBuilder notificationTextBuilder = new StringBuilder();
            notificationTextBuilder.append(notification.getString("action") == "ADDED" ? "A new " : "An updated ");
            notificationTextBuilder.append("order containing ");
            notificationTextBuilder.append(((List)notificationData.get("items")).size());
            notificationTextBuilder.append(" items was received for ");
            notificationTextBuilder.append(String.format("$%.2f", notificationData.get("total")));
            notificationTextBuilder.append(".");

            //set an id for the notification
            int notificationId = Integer.valueOf(notificationData.get("notificationId").toString());

            Bundle bundle = new Bundle();
            bundle.putSerializable("User", restaurantUser);
            bundle.putInt("notificationId", notificationId);

            Intent resultIntent = new Intent(context, RestaurantMainMenu.class);
            resultIntent.putExtras(bundle);
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);

            // Because clicking the notification launches a new ("special") activity,
            // there's no need to create an artificial back stack.
            PendingIntent resultPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    resultIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT);

            // Build notification
            NotificationCompat.Builder notificationBuilder =
                    (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_mail_outline)
                            .setContentTitle(notificationType + " notification message")
                            .setContentText(contentTextBuilder.toString())
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationTextBuilder.toString()))
                            .addAction(R.drawable.ic_open_in_new, "View order", resultPendingIntent);

            // This sets the pending intent that should be fired when the user clicks the
            // notification. Clicking the notification launches a new activity.
            notificationBuilder.setContentIntent(resultPendingIntent);

            // Gets an instance of the NotificationManager service
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

            // Builds the notification and issues it.
            notificationManager.notify(notificationId, notificationBuilder.build());
        }
    }
}
