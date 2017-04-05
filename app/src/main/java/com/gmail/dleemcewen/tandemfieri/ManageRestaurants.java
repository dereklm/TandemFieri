package com.gmail.dleemcewen.tandemfieri;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.gmail.dleemcewen.tandemfieri.Adapters.ManageRestaurantExpandableListAdapter;
import com.gmail.dleemcewen.tandemfieri.Entities.Restaurant;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.Events.ActivityEvent;
import com.gmail.dleemcewen.tandemfieri.Logging.LogWriter;
import com.gmail.dleemcewen.tandemfieri.Repositories.Restaurants;
import com.gmail.dleemcewen.tandemfieri.Tasks.TaskResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ManageRestaurants extends AppCompatActivity {
    private TextView header;
    private BootstrapButton addRestaurant;
    private ExpandableListView restaurantsList;
    private ManageRestaurantExpandableListAdapter listAdapter;
    private Restaurants<Restaurant> restaurants;
    private User currentUser;
    private Context context;
    private static final int CREATE_RESTAURANT = 1;
    private static final int UPDATE_RESTAURANT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_restaurants);

        initialize();
        findControlReferences();
        bindEventHandlers();
        retrieveData();
        finalizeLayout();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == CREATE_RESTAURANT || requestCode == UPDATE_RESTAURANT)
            && resultCode == RESULT_OK) {
            //A new restaurant was added or a restaurant was updated
            retrieveData();
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    /**
     * initialize all necessary variables
     */
    private void initialize() {
        context = this;
        restaurants = new Restaurants<>(context);

        Bundle bundle = this.getIntent().getExtras();
        currentUser = (User)bundle.getSerializable("User");
        LogWriter.log(getApplicationContext(), Level.INFO, "The user is " + currentUser.getEmail());

        if(currentUser.getAuthUserID() != null){
            LogWriter.log(getApplicationContext(), Level.INFO, "The id is " + currentUser.getAuthUserID());
        }else{
            LogWriter.log(getApplicationContext(), Level.INFO, "The id is null");
        }

        EventBus.getDefault().register(this);
    }

    /**
     * find all control references
     */
    private void findControlReferences() {
        header = (TextView)findViewById(R.id.header);
        addRestaurant = (BootstrapButton) findViewById(R.id.addRestaurant);
        restaurantsList = (ExpandableListView)findViewById(R.id.restaurantsList);
    }

    /**
     * bind required event handlers
     */
    private void bindEventHandlers() {
        addRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageRestaurants.this, CreateRestaurant.class);
                intent.putExtra("ownerId", currentUser.getAuthUserID());
                startActivityForResult(intent, CREATE_RESTAURANT);
            }
        });
    }

    /**
     * retrieve data
     */
    private void retrieveData() {
        //find all the restaurants where the ownerid matches the current user id
        restaurants
            .find("ownerId = '" + currentUser.getAuthUserID() + "'")
            .addOnCompleteListener(ManageRestaurants.this, new OnCompleteListener<TaskResult<Restaurant>>() {
                @Override
                public void onComplete(@NonNull Task<TaskResult<Restaurant>> task) {
                    List<Restaurant> entities = task.getResult().getResults();
                    listAdapter = new ManageRestaurantExpandableListAdapter((Activity)context, entities, buildExpandableChildData(entities), currentUser);
                    restaurantsList.setAdapter(listAdapter);
                }
            });
    }

    /**
     * buildExpandableChildData builds the data that is associated with the expandable child entries
     * @param entities indicates a list of restaurants returned by the retrieveData method
     * @return Map of the expandable child data
     */
    private Map<String, List<Restaurant>> buildExpandableChildData(List<Restaurant> entities) {
        HashMap<String, List<Restaurant>> childData = new HashMap<>();
        for (Restaurant entity : entities) {
            childData.put(entity.getKey(), Arrays.asList(entity));
        }

        return childData;
    }

    /**
     * perform any final layout updates
     */
    private void finalizeLayout() {
        //set header value
        header.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
    }

    @Subscribe
    public void onEvent(ActivityEvent event) {
        if (event.result == ActivityEvent.Result.REFRESH_RESTAURANT_LIST) {
            retrieveData();
        }
    }
}