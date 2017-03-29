package com.gmail.dleemcewen.tandemfieri.Subscribers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;

import com.gmail.dleemcewen.tandemfieri.DinerMainMenu;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.Filters.SubscriberFilter;
import com.gmail.dleemcewen.tandemfieri.Interfaces.ISubscriber;
import com.gmail.dleemcewen.tandemfieri.R;

import java.util.HashMap;
import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * DinerSubscriber identifies a diner that has subscribed to notification messages
 */

public class DinerSubscriber implements ISubscriber {
    private Context context;
    private List<SubscriberFilter> filters;
    private User dinerUser;
    private static final String notificationType = "Order";

    /**
     * Default constructor
     * @param context indicates the current application context
     * @param dinerUser identifies the diner user
     * @param filters indicates the record filters supplied by the subscriber
     */
    public DinerSubscriber(Context context, User dinerUser, List<SubscriberFilter> filters) {
        this.context = context;
        this.dinerUser = dinerUser;
        this.filters = filters;
    }

    /**
     * Optional constructor
     * @param context indicates the current application context
     */
    public DinerSubscriber(Context context) {
        this.context = context;
        this.filters = null;
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
    public void update(Bundle notification) {
        if (context.getResources().getBoolean(R.bool.send_notifications)) {
            HashMap notificationData = ((HashMap)notification.getSerializable("entity"));

            StringBuilder contentTextBuilder = new StringBuilder();
            contentTextBuilder.append("Rate your driver?");

            StringBuilder notificationTextBuilder = new StringBuilder();
            notificationTextBuilder.append("You recently placed an order from ");
            notificationTextBuilder.append(notificationData.get("restaurantName"));
            notificationTextBuilder.append(".");
            notificationTextBuilder.append("\r\n");
            notificationTextBuilder.append("Would you like to rate your driver?");

            //set an id for the notification
            int notificationId = Integer.valueOf(notificationData.get("notificationId").toString());

            Bundle rateDriverBundle = new Bundle();
            rateDriverBundle.putSerializable("User", dinerUser);
            rateDriverBundle.putInt("notificationId", notificationId);
            rateDriverBundle.putString("driverId", notificationData.get("userId").toString());

            Intent ratingIntent = new Intent(context, DinerMainMenu.class);
            ratingIntent.putExtras(rateDriverBundle);
            ratingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            Bundle cancelRatingBundle = new Bundle();
            cancelRatingBundle.putSerializable("User", dinerUser);
            cancelRatingBundle.putInt("notificationId", notificationId);
            rateDriverBundle.putString("driverId", notificationData.get("userId").toString());
            cancelRatingBundle.putBoolean("skipRating", true);

            Intent cancelIntent = new Intent(context, DinerMainMenu.class);
            cancelIntent.putExtras(cancelRatingBundle);
            cancelIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            TaskStackBuilder cancelBackStackBuilder = TaskStackBuilder.create(context);
            cancelBackStackBuilder.addNextIntentWithParentStack(cancelIntent);

            PendingIntent cancelNotificationPendingIntent = cancelBackStackBuilder
                    .getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);

            TaskStackBuilder ratingBackStackBuilder = TaskStackBuilder.create(context);
            ratingBackStackBuilder.addNextIntentWithParentStack(ratingIntent);

            PendingIntent resultPendingIntent = ratingBackStackBuilder
                    .getPendingIntent(1, PendingIntent.FLAG_CANCEL_CURRENT);

            // Build notification
            NotificationCompat.Builder notificationBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_mail_outline)
                    .setContentTitle(notificationType + " notification message")
                    .setContentText(contentTextBuilder.toString())
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationTextBuilder.toString()))
                    .setAutoCancel(false)
                    .addAction(R.drawable.ic_remove, "No thanks", cancelNotificationPendingIntent)
                    .addAction(R.drawable.ic_open_in_new, "Rate your driver", resultPendingIntent);

            // This sets the pending intent that should be fired when the user clicks the
            // notification. Clicking the notification launches a new activity.
            notificationBuilder
                .setDeleteIntent(cancelNotificationPendingIntent)
                .setContentIntent(resultPendingIntent);

            // Gets an instance of the NotificationManager service
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

            // Builds the notification and issues it.
            notificationManager.notify(notificationId, notificationBuilder.build());
        }
    }
}

