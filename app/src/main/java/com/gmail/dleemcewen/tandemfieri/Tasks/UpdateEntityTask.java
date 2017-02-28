package com.gmail.dleemcewen.tandemfieri.Tasks;

import android.support.annotation.NonNull;

import com.gmail.dleemcewen.tandemfieri.Abstracts.Entity;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.AbstractMap;
import java.util.Map;

/**
 * UpdateEntityTask defines the task to update an existing restaurant that can be used with other chained tasks
 */

public class UpdateEntityTask<T extends Entity> implements Continuation<Map.Entry<Boolean, DatabaseError>, Task<Map.Entry<Boolean, DatabaseError>>> {
    private DatabaseReference dataContext;
    private T entity;

    /**
     * Default constructor
     * @param dataContext indicates the dataContext
     * @param entity identifies the entity to update
     */
    public UpdateEntityTask(DatabaseReference dataContext, T entity) {
        this.dataContext = dataContext;
        this.entity = entity;
    }

    @Override
    public Task<Map.Entry<Boolean, DatabaseError>> then(@NonNull Task<Map.Entry<Boolean, DatabaseError>> task) throws Exception {
        final TaskCompletionSource<Map.Entry<Boolean, DatabaseError>> taskCompletionSource = new TaskCompletionSource<>();

        //only check the database if the result from the previous task was successful
        if (task.getResult().getKey()) {
            dataContext.child(entity.getKey().toString()).setValue(entity);

            dataContext.addListenerForSingleValueEvent(new ValueEventListener() {
                AbstractMap.SimpleEntry<Boolean, DatabaseError> listenerResult;

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    listenerResult = new AbstractMap.SimpleEntry(true, null);
                    taskCompletionSource.setResult(listenerResult);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    listenerResult = new AbstractMap.SimpleEntry(false, databaseError);
                    taskCompletionSource.setResult(listenerResult);
                }
            });
        } else {
            taskCompletionSource.setResult(task.getResult());
        }

        return taskCompletionSource.getTask();
    }
}
