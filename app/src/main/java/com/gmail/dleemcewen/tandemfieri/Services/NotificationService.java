package com.gmail.dleemcewen.tandemfieri.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import com.gmail.dleemcewen.tandemfieri.Abstracts.Entity;
import com.gmail.dleemcewen.tandemfieri.Publishers.Publisher;
import com.gmail.dleemcewen.tandemfieri.R;

import java.util.AbstractMap;
import java.util.Map;

/**
 * NotificationService
 */

public class NotificationService extends IntentService {

    /**
     * Default constructor
     */
    public NotificationService() {
        super("NotificationService");
    }

    /**
     * This method is invoked on the worker thread with a request to process.
     * Only one Intent is processed at a time, but the processing happens on a
     * worker thread that runs independently from other application logic.
     * So, if this code takes a long time, it will hold up other requests to
     * the same IntentService, but it will not hold up anything else.
     * When all requests have been handled, the IntentService stops itself,
     * so you should not call {@link #stopSelf}.
     *
     * @param intent The value passed to {@link
     *               Context#startService(Intent)}.
     *               This may be null if the service is being restarted after
     *               its process has gone away; see
     *               {@link Service#onStartCommand}
     *               for details.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getStringExtra("action");
        Object entity = intent.getSerializableExtra("entity");
        ((Entity)entity).setKey(intent.getStringExtra("key"));

        Map.Entry<String, Object> changedEntry = new AbstractMap.SimpleEntry<>(action, entity);
        Publisher publisher = Publisher.getInstance();
        publisher.notifySubscribers(changedEntry);

        if (getResources().getBoolean(R.bool.send_notifications)) {
            // Send Notification
            NotificationCompat.Builder mBuilder =
                    (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_tv_light)
                            .setContentTitle("Order Notification")
                            .setContentText("A new order was " + action + " for " + ((Entity) entity).getKey() + "!");

            // Sets an ID for the notification
            int notificationId = 1;

            // Gets an instance of the NotificationManager service
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            // Builds the notification and issues it.
            notificationManager.notify(notificationId, mBuilder.build());
        }
    }
}
