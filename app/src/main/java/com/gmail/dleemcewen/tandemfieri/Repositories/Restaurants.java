package com.gmail.dleemcewen.tandemfieri.Repositories;

import android.support.annotation.NonNull;
import android.util.Log;

import com.gmail.dleemcewen.tandemfieri.Abstracts.Entity;
import com.gmail.dleemcewen.tandemfieri.Abstracts.Repository;
import com.gmail.dleemcewen.tandemfieri.Entities.Restaurant;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.EventListeners.QueryCompleteListener;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

/**
 * Restaurants repository defines the database logic to use when adding, removing, or updating a Restaurant
 */

public class Restaurants<T extends Entity> extends Repository<Restaurant> {
    private DatabaseReference dataContext;

    /**
     * add adds a new restaurant and then returns the value as a task so that some other action
     * can be taken
     * @param entity indicates the entity to add
     * @return Task containing the results of the find that can be chained to other tasks
     */
    public Task<AbstractMap.SimpleEntry<Boolean, DatabaseError>> add(Restaurant entity) {
        final Restaurant entityReference = entity;

        return Tasks.<Void>forResult(null)
                .continueWithTask(new Continuation<Void, Task<AbstractMap.SimpleEntry<Boolean, DatabaseError>>>() {
                    @Override
                    public Task<AbstractMap.SimpleEntry<Boolean, DatabaseError>> then(@NonNull Task<Void> task) throws Exception {
                        final TaskCompletionSource<AbstractMap.SimpleEntry<Boolean, DatabaseError>> taskCompletionSource = new TaskCompletionSource<>();

                        dataContext = getDataContext(entityReference.getClass().getSimpleName(), new String[]{});
                        dataContext.child(entityReference.getKey().toString()).setValue(entityReference);

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

                        return taskCompletionSource.getTask();
                    }
                });
    }

    /**
     * find restaurants from the database
     * @param childNodes identifies the list of string arguments that indicates the
     *                         child node(s) that identify the location of the desired data
     * @param value indicates the data value to search for
     * @param onQueryComplete identifies the QueryCompleteListener to push results back to
     * ex: to find a restaurant downtown on Broadway:
    users.find(Arrays.asList("Downtown", "Street"), "Broadway", new QueryCompleteListener<User>() {
    @Override
    public void onQueryComplete(ArrayList<User> entities) {
    // ...
    }
    });
     */
    @Override
    public void find(List<String> childNodes, String value, QueryCompleteListener<Restaurant> onQueryComplete) {
        search(childNodes, value, onQueryComplete);
    }

    /**
     * find restaurants from the database
     * @param childNodes identifies the list of string arguments that indicates the
     *                         child node(s) that identify the location of the desired data
     * @param value indicates the data value to search for
     * @return Task containing the results of the find that can be chained to other tasks
     */
    @Override
    public Task<ArrayList<Restaurant>> find(List<String> childNodes, String value) {
        final List<String> childNodesReference = childNodes;
        final String valueReference = value;

        return Tasks.<Void>forResult(null)
                .continueWithTask(new Continuation<Void, Task<ArrayList<Restaurant>>>() {
                    @Override
                    public Task<ArrayList<Restaurant>> then(@NonNull Task<Void> task) throws Exception {
                        final TaskCompletionSource<ArrayList<Restaurant>> taskCompletionSource = new TaskCompletionSource<ArrayList<Restaurant>>();

                        search(childNodesReference, valueReference, new QueryCompleteListener<Restaurant>() {
                            @Override
                            public void onQueryComplete(ArrayList<Restaurant> entities) {
                                taskCompletionSource.setResult(entities);
                            }
                        });

                        return taskCompletionSource.getTask();
                    }
                });
    }

    /**
     * execute a search from the database
     * @param childNodes identifies the list of string arguments that indicates the
     *                         child node(s) that identify the location of the desired data
     * @param value indicates the value to search for
     * @param onQueryComplete identifies the QueryCompleteListener to push results back to
     */
    private void search(List<String> childNodes, String value, QueryCompleteListener<Restaurant> onQueryComplete) {
        DatabaseReference dataContext = FirebaseDatabase
                .getInstance()
                .getReference(Restaurant.class.getSimpleName());

        String[] childNodesArray = new String[childNodes.size()];
        childNodesArray = childNodes.toArray(childNodesArray);

        Query query = buildEqualsQuery(dataContext, value, childNodesArray);
        final QueryCompleteListener<Restaurant> finalQueryCompleteListener = onQueryComplete;

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();

                for (DataSnapshot record : dataSnapshot.getChildren()) {
                    Restaurant foundRestaurant = record.getValue(Restaurant.class);
                    foundRestaurant.setKey(record.getKey());

                    restaurants.add(foundRestaurant);
                }

                finalQueryCompleteListener.onQueryComplete(restaurants);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Restaurant failed, log a message
                Log.w("Restaurants", "Restaurants.find:onCancelled", databaseError.toException());
            }
        });
    }
}
