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
import java.util.List;
import java.util.Map;

/**
 * RemoveEntitiesTask defines the task to remove entities that can be used with other chained tasks
 */

public class RemoveEntitiesTask<T extends Entity> implements Continuation<Map.Entry<Boolean, DatabaseError>, Task<Map.Entry<Boolean, DatabaseError>>> {
    private DatabaseReference dataContext;
    private List<T> entities;

    /**
     * Default constructor
     * @param dataContext indicates the dataContext
     * @param entities identifies the entities to remove
     */
    public RemoveEntitiesTask(DatabaseReference dataContext, List<T> entities) {
        this.dataContext = dataContext;
        this.entities = entities;
    }

    @Override
    public Task<Map.Entry<Boolean, DatabaseError>> then(@NonNull Task<Map.Entry<Boolean, DatabaseError>> task) throws Exception {
        final TaskCompletionSource<Map.Entry<Boolean, DatabaseError>> taskCompletionSource = new TaskCompletionSource<>();

        //only check the database if the result from the previous task was successful
        if (task.getResult().getKey()) {
            for (T entity : entities) {
                dataContext.child(entity.getKey().toString()).removeValue();
            }

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
