package com.gmail.dleemcewen.tandemfieri.Repositories;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.gmail.dleemcewen.tandemfieri.Abstracts.Entity;
import com.gmail.dleemcewen.tandemfieri.Abstracts.Repository;
import com.gmail.dleemcewen.tandemfieri.Constants.NotificationConstants;
import com.gmail.dleemcewen.tandemfieri.Entities.NotificationMessage;
import com.gmail.dleemcewen.tandemfieri.EventListeners.QueryCompleteListener;
import com.gmail.dleemcewen.tandemfieri.Services.NotificationService;
import com.gmail.dleemcewen.tandemfieri.Tasks.AddEntityTask;
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
    private boolean sendNotificationMessages = false;
    private ChildEventListener notificationChildEventListener;

    /**
     * Default constructor
     * @param context indicates the current application context
     */
    public NotificationMessages(final Context context) {
        super(context);
        this.context = context;

        dataContext = getDataContext(NotificationMessage.class.getSimpleName());
        notificationChildEventListener = dataContext.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (sendNotificationMessages) {
                    NotificationMessage childNotificationMessageRecord = dataSnapshot.getValue(NotificationMessage.class);
                    childNotificationMessageRecord.setKey(dataSnapshot.getKey());

                    Intent intent = new Intent(context, NotificationService.class);
                    intent.setAction( childNotificationMessageRecord.getAction());
                    intent.putExtra("notificationId", childNotificationMessageRecord.getNotificationId());
                    intent.putExtra("notificationType", childNotificationMessageRecord.getNotificationType());
                    intent.putExtra("entity", (Serializable) childNotificationMessageRecord.getData());
                    intent.putExtra("key", childNotificationMessageRecord.getKey());
                    context.startService(intent);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                NotificationMessage childNotificationMessageRecord = dataSnapshot.getValue(NotificationMessage.class);
                childNotificationMessageRecord.setKey(dataSnapshot.getKey());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                NotificationMessage childNotificationMessageRecord = dataSnapshot.getValue(NotificationMessage.class);
                childNotificationMessageRecord.setKey(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                //not implemented
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //TODO
            }
        });
        dataContext.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                sendNotificationMessages = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * finalize cleans up the child event listener when the repository removed
     */
    public void finalize() {
        dataContext.removeEventListener(notificationChildEventListener);
    }

    /**
     * sends a new notification containing the data from the supplied entity
     * @param action indicates the notification constant to include in the activity
     * @param entity indicates the data from the supplied entity to include
     */
    public <V extends Entity> Task<TaskResult<NotificationMessage>> sendNotification(final NotificationConstants.Action action, V entity) {
        String[] childNodes = new String[searchNodes.size()];
        childNodes = searchNodes.toArray(childNodes);

        dataContext = getDataContext(NotificationMessage.class.getSimpleName(), childNodes);

        return Tasks.<Void>forResult(null)
                .continueWithTask(new NetworkConnectivityCheckTask(context))
                .continueWithTask(new AddNotificationMessageTask<>(dataContext, action, entity))
                .continueWith(new Continuation<TaskResult<V>, TaskResult<NotificationMessage>>() {
                    @Override
                    public TaskResult<NotificationMessage> then(@NonNull Task<TaskResult<V>> task) throws Exception {
                        TaskCompletionSource<TaskResult<NotificationMessage>> taskCompletionSource =
                                new TaskCompletionSource<>();

                        List<V> entities = task.getResult().getResults();
                        List<NotificationMessage> messages = new ArrayList<>();
                        if (!entities.isEmpty()) {
                            messages.add((NotificationMessage)entities.get(0));

                            taskCompletionSource.setResult(new TaskResult<>(action.toString(), messages, null));
                        }

                        //Clear after find complete
                        searchNodes.clear();

                        return taskCompletionSource.getTask().getResult();
                    }
                });
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
