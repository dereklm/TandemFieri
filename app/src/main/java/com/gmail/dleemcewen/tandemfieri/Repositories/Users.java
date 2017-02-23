package com.gmail.dleemcewen.tandemfieri.Repositories;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.gmail.dleemcewen.tandemfieri.Abstracts.Entity;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.EventListeners.QueryCompleteListener;
import com.gmail.dleemcewen.tandemfieri.Abstracts.Repository;
import com.gmail.dleemcewen.tandemfieri.Logging.LogWriter;
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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Users repository defines the database logic to use when adding, removing, or updating a User
 */

public class Users<T extends Entity> extends Repository<User> {
    private DatabaseReference dataContext;
    private Context context;

    /**
     * Default constructor
     * @param context indicates the current application context
     */
    public Users(Context context) {
        this.context = context;
    }

    /**
     * find users from the database
     * @param childNodes identifies the list of string arguments that indicates the
     *                         child node(s) that identify the location of the desired data
     * @param value indicates the data value to search for
     * @param onQueryComplete identifies the QueryCompleteListener to push results back to
     * ex: to find a user driver with the first name of "Joe":
    users.find(Arrays.asList("Driver"), "Joe", new QueryCompleteListener<User>() {
    @Override
    public void onQueryComplete(ArrayList<User> entities) {
    // ...
    }
    });
     */
    @Override
    public void find(List<String> childNodes, String value, QueryCompleteListener<User> onQueryComplete) {
        search(childNodes, value, onQueryComplete);
    }

    /**
     * find users from the database
     * @param childNodes identifies the list of string arguments that indicates the
     *                         child node(s) that identify the location of the desired data
     * @param value indicates the data value to search for
     * @return Task containing the results of the find that can be chained to other tasks
     */
    @Override
    public Task<ArrayList<User>> find(List<String> childNodes, String value) {
        final List<String> childNodesReference = childNodes;
        final String valueReference = value;

        return Tasks.<Void>forResult(null)
                .continueWithTask(new Continuation<Void, Task<ArrayList<User>>>() {
                    @Override
                    public Task<ArrayList<User>> then(@NonNull Task<Void> task) throws Exception {
                        final TaskCompletionSource<ArrayList<User>> taskCompletionSource = new TaskCompletionSource<ArrayList<User>>();

                        search(childNodesReference, valueReference, new QueryCompleteListener<User>() {
                            @Override
                            public void onQueryComplete(ArrayList<User> entities) {
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
    private void search(List<String> childNodes, String value, QueryCompleteListener<User> onQueryComplete) {
        DatabaseReference dataContext = FirebaseDatabase
                .getInstance()
                .getReference(User.class.getSimpleName());

        String[] childNodesArray = new String[childNodes.size()];
        childNodesArray = childNodes.toArray(childNodesArray);
        final QueryCompleteListener<User> finalQueryCompleteListener = onQueryComplete;

        if (value != null && !value.equals("")) {
            performQuerySearch(dataContext, value, childNodesArray, finalQueryCompleteListener);
        } else {
            performDataRetrieve(dataContext, childNodesArray, finalQueryCompleteListener);
        }
    }

    /**
     * performQuerySearch performs a query search across all of the User entities
     * @param dataContext identifies the data context
     * @param value indicates the value to search for
     * @param childNodesArray identifies the list of string arguments that indicates the
     *                         child node(s) that identify the location of the desired data
     * @param queryCompleteListener identifies the QueryCompleteListener to push results back to
     */
    private void performQuerySearch(DatabaseReference dataContext, String value,
        String[] childNodesArray, final QueryCompleteListener<User> queryCompleteListener) {
        Query query = buildEqualsQuery(dataContext, value, childNodesArray);

        LogWriter.log(context, Level.FINE, "Searching for user data that equals " + value + " from " + query.toString());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<User> foundUsers = new ArrayList<User>();

                for (DataSnapshot record : dataSnapshot.getChildren()) {
                    User foundUser = record.getValue(User.class);
                    foundUser.setKey(record.getKey());

                    foundUsers.add(foundUser);
                }

                queryCompleteListener.onQueryComplete(foundUsers);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting User failed, log a message
                LogWriter.log(context, Level.FINE, "Users.find:onCancelled " + databaseError.toException());
            }
        });
    }

    /**
     * performDataRetrieve performs a retrieval operation across all of the User entities data
     * @param dataContext identifies the data context
     * @param childNodesArray identifies the list of string arguments that indicates the
     *                         child node(s) that identify the location of the desired data
     * @param queryCompleteListener identifies the QueryCompleteListener to push results back to
     */
    private void performDataRetrieve(DatabaseReference dataContext,
        String[] childNodesArray, final QueryCompleteListener<User> queryCompleteListener) {
        for (String childNode : childNodesArray) {
            dataContext = dataContext.child(childNode);
        }

        LogWriter.log(context, Level.FINE, "Retrieving user data for " + dataContext.toString());

        dataContext.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<User> foundUsers = new ArrayList<User>();

                for (DataSnapshot record : dataSnapshot.getChildren()) {
                    User foundUser = record.getValue(User.class);
                    foundUser.setKey(record.getKey());

                    foundUsers.add(foundUser);
                }

                queryCompleteListener.onQueryComplete(foundUsers);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting User failed, log a message
                LogWriter.log(context, Level.FINE, "Users.find:onCancelled " + databaseError.toException());
            }
        });

    }
}



