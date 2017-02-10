package com.gmail.dleemcewen.tandemfieri.Abstracts;

import android.text.TextUtils;

import com.gmail.dleemcewen.tandemfieri.EventListeners.QueryCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository defines the abstract class that all repositories extend
 */
public abstract class Repository<T extends Entity> {
    private DatabaseReference dataContext;

    /**
     * add a single entity to the database
     * @param entity indicates the entity to add
     * @param childNodes indicates the variable number of string arguments that identify the
     *                         child nodes that identify the location of the desired data
     */
    public void add(T entity, String... childNodes) {
        dataContext = getDataContext(entity.getClass().getSimpleName(), childNodes);
        dataContext.child(entity.getKey().toString()).setValue(entity);
    }

    /**
     * add multiple entities to the database
     * @param entities indicates a list of entities to add
     * @param childNodes indicates the variable number of string arguments that identify the
     *                         child nodes that identify the location of the desired data
     */
    public void add(ArrayList<T> entities, String... childNodes) {
        dataContext = getDataContext(entities.get(0).getClass().getSimpleName(), childNodes);
        for (T entity : entities) {
            dataContext.child(entity.getKey().toString()).setValue(entity);
        }
    }

    /**
     * updates the specified entity
     * @param entity indicates the entity to update with the new values
     * @param childNodes indicates the variable number of string arguments that identify the
     *                         child nodes that identify the location of the desired data
     */
    public void update(T entity, String... childNodes) {
        dataContext = getDataContext(entity.getClass().getSimpleName(), childNodes);
        dataContext.child(entity.getKey().toString()).setValue(entity);
    }

    /**
     * remove a single entity from the database
     * @param entity indicates the entity to remove
     * @param childNodes indicates the variable number of string arguments that identify the
     *                         child nodes that identify the location of the desired data
     */
    public void remove(T entity, String... childNodes) {
        dataContext = getDataContext(entity.getClass().getSimpleName(), childNodes);
        dataContext.child(entity.getKey().toString()).removeValue();
    }

    /**
     * remove multiple entities from the database
     * @param entities indicates a list of entities to remove
     * @param childNodes indicates the variable number of string arguments that identify the
     *                         child nodes that identify the location of the desired data
     */
    public void remove(ArrayList<T> entities, String... childNodes) {
        dataContext = getDataContext(entities.get(0).getClass().getSimpleName(), childNodes);
        for (T entity : entities) {
            dataContext.child(entity.getKey().toString()).removeValue();
        }
    }

    /**
     * find entities from the database
     * @param childNodes identifies the list of string arguments that indicates the
     *                         child node(s) that identify the location of the desired data
     * @param value indicates the data value to search for
     * @param onQueryComplete identifies the QueryCompleteListener to push results back to
     */
    public abstract void find(List<String> childNodes, String value, QueryCompleteListener<T> onQueryComplete);

    /**
     * find entities from the database
     * @param childNodes identifies the list of string arguments that indicates the
     *                         child node(s) that identify the location of the desired data
     * @param value indicates the data value to search for
     * @return Task containing the results of the find that can be chained to other tasks
     */
    public abstract Task<ArrayList<T>> find(List<String> childNodes, String value);

    /**
     * getDataContext gets a new data context pointing to the appropriate entity
     * at the appropriate nested child node
     * @param entityName indicates the name of the entity
     * @param childNodes indicates the list of child nodes that indicate the location
     *                         of the desired data (optional)
     * @return new Firebase database reference
     */
    protected DatabaseReference getDataContext(String entityName, String... childNodes) {
        DatabaseReference dataContext = FirebaseDatabase.getInstance().getReference(entityName);
        for (String childNode : childNodes) {
            dataContext = dataContext.child(childNode);
        }

        return dataContext;
    }

    /**
     * buildQuery builds the appropriate query based on the provided childNodes and value
     * @param dataContext indicates the data context
     * @param childNodes indicates the list of child nodes to query (optional)
     * @param value indicates the value to search for (optional)
     * @return query that can be executed by firebase to find desired records
     */
    protected Query buildQuery(DatabaseReference dataContext, List<String> childNodes, String value) {
        Query query;

        String childNodesPath = "";
        if (!childNodes.isEmpty()) {
            childNodesPath = TextUtils.join("/", childNodes);
        }

        if (!childNodesPath.equals("")) {
            query = dataContext.orderByChild(childNodesPath);
        } else {
            query = dataContext.orderByKey();
        }

        if (!value.equals("")) {
            query = query.equalTo(value);
        }

        return query;
    }
}

