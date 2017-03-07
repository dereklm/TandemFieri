package com.gmail.dleemcewen.tandemfieri.Repositories;

import android.content.Context;
import android.content.Intent;

import com.gmail.dleemcewen.tandemfieri.Abstracts.Entity;
import com.gmail.dleemcewen.tandemfieri.Abstracts.Repository;
import com.gmail.dleemcewen.tandemfieri.Constants.NotificationConstants;
import com.gmail.dleemcewen.tandemfieri.Entities.NotificationMessage;
import com.gmail.dleemcewen.tandemfieri.EventListeners.QueryCompleteListener;
import com.gmail.dleemcewen.tandemfieri.Services.NotificationService;
import com.google.android.gms.tasks.Task;
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

    /**
     * Default constructor
     * @param context indicates the current application context
     */
    public NotificationMessages(final Context context) {
        super(context);
        this.context = context;

        dataContext = getDataContext(NotificationMessage.class.getSimpleName());
        dataContext.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (sendNotificationMessages) {
                    NotificationMessage childNotificationMessageRecord = dataSnapshot.getValue(NotificationMessage.class);
                    childNotificationMessageRecord.setKey(dataSnapshot.getKey());

                    Intent intent = new Intent(context, NotificationService.class);
                    intent.setAction( childNotificationMessageRecord.getAction());
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
