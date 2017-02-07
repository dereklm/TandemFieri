package com.gmail.dleemcewen.tandemfieri.Repositories;

import android.support.annotation.NonNull;
import android.util.Log;

import com.gmail.dleemcewen.tandemfieri.Abstracts.Entity;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.EventListeners.QueryCompleteListener;
import com.gmail.dleemcewen.tandemfieri.Interfaces.Repository;
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

public class Users<T extends Entity> implements Repository<User> {
    private DatabaseReference dataContext;

    /**
     * add a single user to the database
     * @param entity indicates the user to add
     */
    @Override
    public void add(User entity) {
        dataContext = FirebaseDatabase.getInstance().getReference(entity.getClass().getSimpleName());
        dataContext.child(entity.getKey().toString()).setValue(entity);
    }

    /**
     * add multiple users to the database
     * @param entities indicates a list of users to add
     */
    @Override
    public void add(ArrayList<User> entities) {
        dataContext = FirebaseDatabase.getInstance().getReference(entities.get(0).getClass().getSimpleName());
        for (User entity : entities) {
            dataContext.child(entity.getKey().toString()).setValue(entity);
        }
    }

    /**
     * updates the specified user
     * @param entity indicates the user to update with the new values
     */
    @Override
    public void update(User entity) {
        dataContext = FirebaseDatabase.getInstance().getReference(entity.getClass().getSimpleName());
        dataContext.child(entity.getKey().toString()).setValue(entity);
    }

    /**
     * remove a single user from the database
     * @param entity indicates the user to remove
     */
    @Override
    public void remove(User entity) {
        dataContext = FirebaseDatabase.getInstance().getReference(entity.getClass().getSimpleName());
        dataContext.child(entity.getKey().toString()).removeValue();
    }

    /**
     * remove multiple users from the database
     * @param entities indicates a list of users to remove
     */
    @Override
    public void remove(ArrayList<User> entities) {
        dataContext = FirebaseDatabase.getInstance().getReference(entities.get(0).getClass().getSimpleName());
        for (User entity : entities) {
            dataContext.child(entity.getKey().toString()).removeValue();
        }
    }

    /**
     * find users from the database
     * @param searchFields indicates the searchfields to use
     * @param searchValues indicates the searchvalues to search for
     * @param onQueryComplete identifies the QueryCompleteListener to push results back to
     */
    @Override
    public void find(List<String> searchFields, List<String> searchValues, QueryCompleteListener<User> onQueryComplete) {
        search(searchFields, searchValues, onQueryComplete);
    }

    /**
     * find users from the database
     * @param searchFields indicates the searchfields to use
     * @param searchValues indicates the searchvalues to search for
     * @return Task containing the results of the find that can be chained to other tasks
     */
    @Override
    public Task<ArrayList<User>> find(List<String> searchFields, List<String> searchValues) {
        final List<String> searchFieldsReference = searchFields;
        final List<String> searchValuesReference = searchValues;

        return Tasks.<Void>forResult(null)
            .continueWithTask(new Continuation<Void, Task<ArrayList<User>>>() {
                @Override
                public Task<ArrayList<User>> then(@NonNull Task<Void> task) throws Exception {
                    final TaskCompletionSource<ArrayList<User>> taskCompletionSource = new TaskCompletionSource<ArrayList<User>>();

                    search(searchFieldsReference, searchValuesReference, new QueryCompleteListener<User>() {
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
     * @param searchFields indicates the searchfields to use
     * @param searchValues indicates the searchvalues to search for
     * @param onQueryComplete identifies the QueryCompleteListener to push results back to
     */
    private void search(List<String> searchFields, List<String> searchValues, QueryCompleteListener<User> onQueryComplete) {
        DatabaseReference dataContext = FirebaseDatabase.getInstance().getReference(User.class.getSimpleName());
        Query query = dataContext.orderByChild(searchFields.get(0)).equalTo(searchValues.get(0));

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
