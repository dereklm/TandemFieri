package com.gmail.dleemcewen.tandemfieri.Repositories;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.gmail.dleemcewen.tandemfieri.Abstracts.Entity;
import com.gmail.dleemcewen.tandemfieri.Abstracts.Repository;
import com.gmail.dleemcewen.tandemfieri.Constants.NotificationConstants;
import com.gmail.dleemcewen.tandemfieri.Entities.NotificationMessage;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.EventListeners.QueryCompleteListener;
import com.gmail.dleemcewen.tandemfieri.Services.NotificationService;
import com.gmail.dleemcewen.tandemfieri.Tasks.AddNotificationMessageTask;
import com.gmail.dleemcewen.tandemfieri.Tasks.NetworkConnectivityCheckTask;
import com.gmail.dleemcewen.tandemfieri.Tasks.TaskResult;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * NotificationMessages repository defines the database logic to use when adding, removing, or updating a NotificationMessage
 */

public class NotificationMessages<T extends Entity> extends Repository<NotificationMessage> {
    private Context context;
    private DatabaseReference dataContext;

    /**
     * Default constructor
     * @param context indicates the current application context
     */
    public NotificationMessages(final Context context) {
        super(context);
        this.context = context;
    }

    /**
     * sends a new notification containing the data from the supplied entity
     * @param action indicates the notification constant to include in the activity
     * @param entity indicates the data from the supplied entity to include
     * @param userId indicates the user id
     */
    public <T extends Entity> Task<TaskResult<T>> sendNotification(final NotificationConstants.Action action, T entity, String userId) {
        String[] childNodes = new String[searchNodes.size()];
        childNodes = searchNodes.toArray(childNodes);

        dataContext = getDataContext(NotificationMessage.class.getSimpleName(), childNodes);

        return Tasks.<Void>forResult(null)
                .continueWithTask(new NetworkConnectivityCheckTask(context))
                .continueWithTask(new AddNotificationMessageTask<>(dataContext, action, entity, userId))
                .continueWith(new Continuation<TaskResult<T>, TaskResult<T>>() {
                    @Override
                    public TaskResult<T> then(@NonNull Task<TaskResult<T>> task) throws Exception {
                        TaskCompletionSource<TaskResult<T>> taskCompletionSource =
                                new TaskCompletionSource<>();

                        if (task.getResult().getError() == null) {
                            List<T> entities = task.getResult().getResults();
                            taskCompletionSource.setResult(new TaskResult<>(action.toString(), entities, null));

                            //Clear after find complete
                            searchNodes.clear();
                        }

                        return taskCompletionSource.getTask().getResult();
                    }
                });
    }

    /**
     * resendNotification re-sends a previously sent notification
     * @param notificationMessageRecord indicates the notificationmessage record which contains
 *                                       the data to send as a notification
     */
    public void resendNotification(NotificationMessage notificationMessageRecord) {
        Intent intent = new Intent(context, NotificationService.class);
        intent.setAction(notificationMessageRecord.getAction());
        intent.putExtra("notificationId", notificationMessageRecord.getNotificationId());
        intent.putExtra("notificationType", notificationMessageRecord.getNotificationType());
        intent.putExtra("entity", (Serializable) notificationMessageRecord.getData());
        intent.putExtra("key", notificationMessageRecord.getKey());
        intent.putExtra("userId", notificationMessageRecord.getUserId());
        context.startService(intent);
    }

    /**
     * find entities from the database
     *
     * @param childNodes      identifies the list of string arguments that indicates the
     *                        child node(s) that identify the location of the desired data
     * @param value           indicates the data value to search for
     * @param onQueryComplete identifies the QueryCompleteListener to push results back to
     */
    @Override
    public void find(List<String> childNodes, String value, QueryCompleteListener<NotificationMessage> onQueryComplete) {
        //not implemented
    }

    /**
     * find entities from the database
     *
     * @param childNodes identifies the list of string arguments that indicates the
     *                   child node(s) that identify the location of the desired data
     * @param value      indicates the data value to search for
     * @return Task containing the results of the find that can be chained to other tasks
     */
    @Override
    public Task<ArrayList<NotificationMessage>> find(List<String> childNodes, String value) {
        return null;
    }
}
