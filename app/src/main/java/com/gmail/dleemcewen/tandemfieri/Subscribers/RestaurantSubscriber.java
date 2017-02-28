package com.gmail.dleemcewen.tandemfieri.Subscribers;

import android.content.Context;

import com.gmail.dleemcewen.tandemfieri.Interfaces.ISubscriber;
import com.gmail.dleemcewen.tandemfieri.Logging.LogWriter;

import java.util.Map;
import java.util.logging.Level;

/**
 * RestaurantSubscriber
 */

public class RestaurantSubscriber implements ISubscriber {
    private Context context;

    public RestaurantSubscriber(Context context) {
       this.context = context;
    }

    @Override
    public void update() {
        LogWriter.log(context, Level.FINE, "Recieved notification!");
    }

    @Override
    public String getType() {
        return "Restaurant";
    }
}
