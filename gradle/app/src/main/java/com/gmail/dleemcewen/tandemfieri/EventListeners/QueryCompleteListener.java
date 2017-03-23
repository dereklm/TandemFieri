package com.gmail.dleemcewen.tandemfieri.EventListeners;

import com.gmail.dleemcewen.tandemfieri.Abstracts.Entity;

import java.util.ArrayList;
import java.util.EventListener;

/**
 * QueryCompleteListener defines the interface for the event that
 * fires when queries are completed
 */

public interface QueryCompleteListener<T extends Entity> extends EventListener {
    void onQueryComplete(ArrayList<T> entities);
}
