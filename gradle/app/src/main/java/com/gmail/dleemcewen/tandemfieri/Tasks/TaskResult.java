package com.gmail.dleemcewen.tandemfieri.Tasks;

import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

/**
 * TaskResult defines an object that contains the results of a task
 */
public class TaskResult<T> {
    private String action;
    private List<T> results;
    private DatabaseError error;

    /**
     * Default constructor
     * @param action indicates the type of action
     * @param results indicates the results of the task
     * @param error indicates a database error
     */
    public TaskResult(String action, List<T> results, DatabaseError error) {
        this.action = action;
        this.results = results;
        this.error = error;
    }

    /**
     * Optional constructor
     * @param error indicates a database error
     */
    public TaskResult(DatabaseError error) {
        results = new ArrayList<>();
        this.error = error;
    }

    /**
     * getAction returns the action from the task
     * @return action
     */
    public String getAction() {
        return action;
    }

    /**
     * getResults returns a list of the results from the task(s)
     * @return list of results of type T
     */
    public List<T> getResults() {
        return results;
    }

    /**
     * getError returns the database error that occurred in the task(s)
     * @return database error that occurred in the task(s)
     */
    public DatabaseError getError() {
        return error;
    }
}
