package com.gmail.dleemcewen.tandemfieri.Services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;

import com.gmail.dleemcewen.tandemfieri.Abstracts.Entity;
import com.gmail.dleemcewen.tandemfieri.Publishers.NotificationPublisher;
import com.gmail.dleemcewen.tandemfieri.R;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Map;

/**
 * NotificationService implements an intent service that will notify all subscribers through
 * a notification publisher when a database record is added, updated, or deleted
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
        Object entity = intent.getSerializableExtra("entity");

        Bundle notification = new Bundle();
        notification.putString("action", intent.getAction());
        notification.putString("notificationType", intent.getStringExtra("notificationType"));
        notification.putString("key", intent.getStringExtra("key"));
        notification.putString("notificationId", intent.getStringExtra("notificationId"));
        notification.putString("userId", intent.getStringExtra("userId"));
        notification.putSerializable("entity", (Serializable)entity);

        NotificationPublisher notificationPublisher = NotificationPublisher.getInstance();
        notificationPublisher.notifySubscribers(notification);
    }
}
