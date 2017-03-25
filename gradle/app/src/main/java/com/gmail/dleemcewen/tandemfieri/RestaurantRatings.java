package com.gmail.dleemcewen.tandemfieri;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.gmail.dleemcewen.tandemfieri.Adapters.RestaurantRatingsListAdapter;
import com.gmail.dleemcewen.tandemfieri.Entities.Restaurant;
import com.gmail.dleemcewen.tandemfieri.Repositories.Restaurants;
import com.gmail.dleemcewen.tandemfieri.Tasks.TaskResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

public class RestaurantRatings extends AppCompatActivity {
    private Restaurants<Restaurant> restaurants;
    private Context context;
    private ListView rateableRestaurantsList;
    private RestaurantRatingsListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_ratings);

        initialize();
        findControlReferences();
        bindEventHandlers();
        retrieveData();
    }

    /**
     * initialize all necessary variables
     */
    private void initialize() {
        context = this;
        restaurants = new Restaurants<>(context);
    }

    /**
     * find all control references
     */
    private void findControlReferences() {
        rateableRestaurantsList = (ListView)findViewById(R.id.rateableRestaurantsList);
    }

    /**
     * bind required event handlers
     */
    private void bindEventHandlers() {
    }

    /**
     * retrieve data
     */
    private void retrieveData() {
        //find all the restaurants
        //TODO: possibly limit this to restaurants the user has ordered from - have to query orders and then get restaurants
        restaurants
            .find()
            .addOnCompleteListener(RestaurantRatings.this, new OnCompleteListener<TaskResult<Restaurant>>() {
                @Override
                public void onComplete(@NonNull Task<TaskResult<Restaurant>> task) {
                    List<Restaurant> entities = task.getResult().getResults();
                    listAdapter = new RestaurantRatingsListAdapter((Activity)context, entities);
                    rateableRestaurantsList.setAdapter(listAdapter);
                }
            });
    }
}
