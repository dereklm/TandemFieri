package com.gmail.dleemcewen.tandemfieri.Repositories;

import android.support.annotation.NonNull;
import android.util.Log;

import com.gmail.dleemcewen.tandemfieri.Abstracts.Entity;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.EventListeners.QueryCompleteListener;
import com.gmail.dleemcewen.tandemfieri.Abstracts.Repository;
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

/**
 * Users repository defines the database logic to use when adding, removing, or updating a User
 */

public class Users<T extends Entity> extends Repository<User> {
    private DatabaseReference dataContext;

    /**
     * add a single user to the database
     * @param entity indicates the user to add
     * @param childNodes indicates the variable number of string arguments that identify the
     *                         child nodes that identify the location of the desired data
     * ex: users.add(newUserEntity) or users.add(newUserEntity, new String[] {"Diner"}
     */
    @Override
    public void add(User entity, String... childNodes) {
        dataContext = getDataContext(entity.getClass().getSimpleName(), childNodes);
        dataContext.child(entity.getKey().toString()).setValue(entity);
    }

    /**
     * add multiple users to the database
     * @param entities indicates a list of users to add
     * @param childNodes indicates the variable number of string arguments that identify the
     *                         child nodes that identify the location of the desired data
     */
    @Override
    public void add(ArrayList<User> entities, String... childNodes) {
        dataContext = getDataContext(entities.get(0).getClass().getSimpleName(), childNodes);
        for (User entity : entities) {
            dataContext.child(entity.getKey().toString()).setValue(entity);
        }
    }

    /**
     * updates the specified user
     * @param entity indicates the user to update with the new values
     * @param childNodes indicates the variable number of string arguments that identify the
     *                         child nodes that identify the location of the desired data
     * ex: users.update(existingUserEntity) or users.update(existingUserEntity, new String[] {"Restaurant"}
     */
    @Override
    public void update(User entity, String... childNodes) {
        dataContext = getDataContext(entity.getClass().getSimpleName(), childNodes);
        dataContext.child(entity.getKey().toString()).setValue(entity);
    }

    /**
     * remove a single user from the database
     * @param entity indicates the user to remove
     * @param childNodes indicates the variable number of string arguments that identify the
     *                         child nodes that identify the location of the desired data
     * ex: users.remove(userEntityToRemove) or users.remove(userEntityToRemove, new String[] {"Driver"}
     */
    @Override
    public void remove(User entity, String... childNodes) {
        dataContext = getDataContext(entity.getClass().getSimpleName(), childNodes);
        dataContext.child(entity.getKey().toString()).removeValue();
    }

    /**
     * remove multiple users from the database
     * @param entities indicates a list of users to remove
     * @param childNodes indicates the variable number of string arguments that identify the
     *                         child nodes that identify the location of the desired data
     */
    @Override
    public void remove(ArrayList<User> entities, String... childNodes) {
        dataContext = getDataContext(entities.get(0).getClass().getSimpleName(), childNodes);
        for (User entity : entities) {
            dataContext.child(entity.getKey().toString()).removeValue();
        }
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

        Query query = buildQuery(dataContext, childNodes, value);
        final QueryCompleteListener<User> finalQueryCompleteListener = onQueryComplete;

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<User> foundUsers = new ArrayList<User>();

                for (DataSnapshot record : dataSnapshot.getChildren()) {
                    User foundUser = record.getValue(User.class);
                    foundUser.setKey(record.getKey());

                    foundUsers.add(foundUser);
                }

                finalQueryCompleteListener.onQueryComplete(foundUsers);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting User failed, log a message
                Log.w("Users", "Users.find:onCancelled", databaseError.toException());
            }
        });
    }
}
