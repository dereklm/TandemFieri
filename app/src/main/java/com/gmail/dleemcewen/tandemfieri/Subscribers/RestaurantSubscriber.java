package com.gmail.dleemcewen.tandemfieri.Subscribers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;

import com.gmail.dleemcewen.tandemfieri.Abstracts.Entity;
import com.gmail.dleemcewen.tandemfieri.Entities.Restaurant;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.Interfaces.ISubscriber;
import com.gmail.dleemcewen.tandemfieri.Logging.LogWriter;
import com.gmail.dleemcewen.tandemfieri.R;
import com.gmail.dleemcewen.tandemfieri.RestaurantMainMenu;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static android.R.attr.action;
import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * RestaurantSubscriber identifies a restaurant subscriber
 */

public class RestaurantSubscriber implements ISubscriber {
    private Context context;
    private Map.Entry<String, String> filter;
    private User restaurantUser;
    private static final String notificationType = "Restaurant";

    /**
     * Default constructor
     * @param context indicates the current application context
     * @param restaurantUser identifies the restaurant user
     * @param filter indicates the record filter supplied by the subscriber
     */
    public RestaurantSubscriber(Context context, User restaurantUser, Map.Entry<String, String> filter) {
        this.context = context;
        this.restaurantUser = restaurantUser;
        this.filter = filter;
    }

    /**
     * Optional constructor
     * @param context indicates the current application context
     */
    public RestaurantSubscriber(Context context) {
        this.context = context;
        this.filter = null;
    }

    @Override
    public String getNotificationType() {
        return notificationType;
    }

    @Override
    public Map.Entry<String, String> getFilter() {
        return filter;
    }

    @Override
    public void update(Bundle notification) {
        if (context.getResources().getBoolean(R.bool.send_notifications)) {
            //TODO: update this to use orders instead of restaurants
            //Since the orders are not yet available, this simply simulates receiving a notification
            HashMap notificationData = ((HashMap)notification.getSerializable("entity"));

            StringBuilder contentTextBuilder = new StringBuilder();
            contentTextBuilder.append("Order received for ");
            contentTextBuilder.append(notificationData.get("name"));

            StringBuilder notificationTextBuilder = new StringBuilder();
            notificationTextBuilder.append(notificationData.get("name"));
            notificationTextBuilder.append(" received ");
            notificationTextBuilder.append(notification.getString("action") == "ADDED" ? "a new " : "an updated ");
            notificationTextBuilder.append(" order!");

            //set an id for the notification
            int notificationId = 1;

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
                            .setSmallIcon(R.drawable.cast_ic_notification_small_icon)
                            .setContentTitle(notificationType + " notification message")
                            .setContentText(contentTextBuilder.toString())
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationTextBuilder.toString()))
                            .addAction(R.drawable.cast_ic_notification_2, "View order", resultPendingIntent);

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
