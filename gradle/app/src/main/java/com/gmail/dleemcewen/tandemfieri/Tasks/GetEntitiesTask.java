package com.gmail.dleemcewen.tandemfieri.Tasks;

import android.support.annotation.NonNull;

import com.gmail.dleemcewen.tandemfieri.Abstracts.Entity;
import com.gmail.dleemcewen.tandemfieri.Entities.Restaurant;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GetEntitiesTask defines the task to get entities that can be used with other chained tasks
 */

public class GetEntitiesTask<T extends Entity> implements Continuation<Map.Entry<Boolean, DatabaseError>, Task<Map.Entry<List<T>, DatabaseError>>> {
    private DatabaseReference dataContext;
    private Class<T> entityClass;

    /**
     * Default constructor
     * @param dataContext indicates the dataContext
     * @param entityClass identifies the class of the entity
     */
    public GetEntitiesTask(DatabaseReference dataContext, Class<T> entityClass) {
        this.dataContext = dataContext;
        this.entityClass = entityClass;
    }

    @Override
    public Task<Map.Entry<List<T>, DatabaseError>> then(@NonNull Task<Map.Entry<Boolean, DatabaseError>> task) throws Exception {
        final TaskCompletionSource<Map.Entry<List<T>, DatabaseError>> taskCompletionSource = new TaskCompletionSource<>();

        //only check the database if the result from the previous task was successful
        if (task.getResult().getKey()) {
            dataContext.addListenerForSingleValueEvent(new ValueEventListener() {
                Map.Entry<List<T>, DatabaseError> listenerResult;

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<T> entities = new ArrayList<>();

                    for (DataSnapshot record : dataSnapshot.getChildren()) {
                        T childRecord = record.getValue(entityClass);
                        childRecord.setKey(record.getKey());

                        entities.add(childRecord);
                    }

                    listenerResult = new AbstractMap.SimpleEntry(entities, null);
                    taskCompletionSource.setResult(listenerResult);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    listenerResult = new AbstractMap.SimpleEntry(new ArrayList<>(), databaseError);
                    taskCompletionSource.setResult(listenerResult);
                }
            });
        } else {
            taskCompletionSource.setResult(new AbstractMap.SimpleEntry(new ArrayList<>(), task.getResult().getValue()));
        }

        return taskCompletionSource.getTask();
    }
}
