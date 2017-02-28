package com.gmail.dleemcewen.tandemfieri.Tasks;

import android.content.Context;
import android.support.annotation.NonNull;

import com.gmail.dleemcewen.tandemfieri.Abstracts.Entity;
import com.gmail.dleemcewen.tandemfieri.Entities.Restaurant;
import com.gmail.dleemcewen.tandemfieri.EventListeners.QueryCompleteListener;
import com.gmail.dleemcewen.tandemfieri.Logging.LogWriter;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * FindEntitiesTask defines the task to find entities that can be used with other chained tasks
 */

public class FindEntitiesTask<T extends Entity> implements Continuation<Map.Entry<Boolean, DatabaseError>, Task<Map.Entry<List<T>, DatabaseError>>> {
    private Context context;
    private Class<T> entityClass;
    private Query query;

    /**
     *
     * @param context indicates the current application context
     * @param entityClass identifies the class of the entity
     * @param query indicates the query to execute
     */
    public FindEntitiesTask(Context context, Class<T> entityClass, Query query) {
        this.context = context;
        this.entityClass = entityClass;
        this.query = query;
    }

    @Override
    public Task<Map.Entry<List<T>, DatabaseError>> then(@NonNull Task<Map.Entry<Boolean, DatabaseError>> task) throws Exception {
        final TaskCompletionSource<Map.Entry<List<T>, DatabaseError>> taskCompletionSource = new TaskCompletionSource<>();
        final DatabaseError databaseError = task.getResult().getValue();

        //only check the database if the result from the previous task was successful
        if (task.getResult().getKey()) {
            search(new QueryCompleteListener<T>() {
                @Override
                public void onQueryComplete(ArrayList<T> entities) {
                    Map.Entry<List<T>, DatabaseError> listenerResult = new AbstractMap.SimpleEntry(entities, databaseError);
                    taskCompletionSource.setResult(listenerResult);
                }
            });

            return taskCompletionSource.getTask();
        } else {
            Map.Entry<List<T>, DatabaseError> listenerResult = new AbstractMap.SimpleEntry(new ArrayList<T>(), databaseError);
            taskCompletionSource.setResult(listenerResult);
        }

        return taskCompletionSource.getTask();
    }

    /**
     * execute a search from the database
     * @param onQueryComplete identifies the QueryCompleteListener to push results back to
     */
    private void search(QueryCompleteListener<T> onQueryComplete) {
        final QueryCompleteListener<T> finalQueryCompleteListener = onQueryComplete;

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<T> searchResults = new ArrayList<>();

                for (DataSnapshot record : dataSnapshot.getChildren()) {
                    T result = record.getValue(entityClass);
                    result.setKey(record.getKey());

                    searchResults.add(result);
                }

                finalQueryCompleteListener.onQueryComplete(searchResults);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Restaurant failed, log a message
                LogWriter.log(context, Level.FINE, "find:onCancelled " + databaseError.toException());
            }
        });
    }
}
