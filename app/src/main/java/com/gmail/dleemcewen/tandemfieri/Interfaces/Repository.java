package com.gmail.dleemcewen.tandemfieri.Interfaces;

import com.gmail.dleemcewen.tandemfieri.Abstracts.Entity;
import com.gmail.dleemcewen.tandemfieri.EventListeners.QueryCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository defines the interface for all repository objects
 */

public interface Repository<T extends Entity> {
    void add(T entity);
    void add(ArrayList<T> entities);
    void update(T entity);
    void remove(T entity);
    void remove(ArrayList<T> entities);
    void find(List<String> searchFields, List<String> searchValues, QueryCompleteListener<T> onQueryComplete);
    Task<ArrayList<T>> find(List<String> searchFields, List<String> searchValues);
}
