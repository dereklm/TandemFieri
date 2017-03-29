package com.gmail.dleemcewen.tandemfieri.Tasks;

import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;

import com.gmail.dleemcewen.tandemfieri.Abstracts.Entity;
import com.gmail.dleemcewen.tandemfieri.Constants.NotificationConstants;
import com.gmail.dleemcewen.tandemfieri.Entities.NotificationMessage;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;


/**
 * AddNotificationMessageTask defines the task to add a new notification message that can be used with other chained tasks
 */

public class AddNotificationMessageTask<T extends Entity> implements Continuation<Map.Entry<Boolean, DatabaseError>, Task<TaskResult<T>>> {
    private DatabaseReference dataContext;
    private NotificationConstants.Action action;
    private T entity;
    private String userId;

    /**
     * Default constructor
     * @param dataContext indicates the dataContext
     * @param entity identifies the entity to add
     */
    public AddNotificationMessageTask(DatabaseReference dataContext, NotificationConstants.Action action, T entity, String userId) {
        this.dataContext = dataContext;
        this.action = action;
        this.entity = entity;
        this.userId = userId;
    }

    @Override
    public Task<TaskResult<T>> then(@NonNull final Task<Map.Entry<Boolean, DatabaseError>> task) throws Exception {
        final TaskCompletionSource<TaskResult<T>> taskCompletionSource = new TaskCompletionSource<>();

        //only check the database if the result from the previous task was successful
        if (task.getResult().getKey()) {
            Date currentDateTime = new Date();
            String formattedDate = new SimpleDateFormat("SSSssmmHH").format(currentDateTime);

            //Create a new notificationmessage entity to add
            NotificationMessage notificationMessageEntity = new NotificationMessage();
            notificationMessageEntity.setAction(NotificationConstants.Action.ADDED.toString());
            notificationMessageEntity.setNotificationType(entity.getClass().getSimpleName());
            notificationMessageEntity.setData(entity, entity.getClass());
            notificationMessageEntity.setNotificationId("1" + formattedDate);
            notificationMessageEntity.setUserId(userId);

            dataContext.child(notificationMessageEntity.getKey().toString()).setValue(notificationMessageEntity);
            dataContext.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    taskCompletionSource.setResult(new TaskResult<>(action.toString(), Arrays.asList(entity), null));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    taskCompletionSource.setResult(new TaskResult<T>(task.getResult().getValue()));
                }
            });
        } else {
            taskCompletionSource.setResult(new TaskResult<T>(task.getResult().getValue()));
        }

        return taskCompletionSource.getTask();
    }
}
