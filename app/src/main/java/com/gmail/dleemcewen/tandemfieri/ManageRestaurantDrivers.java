package com.gmail.dleemcewen.tandemfieri;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.gmail.dleemcewen.tandemfieri.Adapters.AssignRestaurantDriversListAdapter;
import com.gmail.dleemcewen.tandemfieri.Adapters.ManageRestaurantDriversListAdapter;
import com.gmail.dleemcewen.tandemfieri.Entities.Restaurant;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.Logging.LogWriter;
import com.gmail.dleemcewen.tandemfieri.Repositories.Users;
import com.gmail.dleemcewen.tandemfieri.Tasks.TaskResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ManageRestaurantDrivers extends AppCompatActivity {
    private Context context;
    private Resources resources;
    private TextView restaurantName;
    private TextView driversCurrentlyAssignedToRestaurant;
    private BootstrapButton addDrivers;
    private ListView restaurantDriverList;
    private ManageRestaurantDriversListAdapter listAdapter;
    private Users<User> users;
    private Restaurant restaurant;

    private ArrayList<User> unassignedDrivers;
    private ArrayList<User> driversToAssignToRestaurant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_restaurant_drivers);

        initialize();
        findControlReferences();
        bindEventHandlers();
        retrieveData();
        finalizeLayout();
    }

    /**
     * initialize all necessary variables
     */
    private void initialize() {
        context = this;
        resources = this.getResources();

        Bundle bundle = this.getIntent().getExtras();
        restaurant = (Restaurant)bundle.getSerializable("Restaurant");

        users = new Users<>(context);
        unassignedDrivers = new ArrayList<>();
        driversToAssignToRestaurant = new ArrayList<>();

        LogWriter.log(getApplicationContext(),
            Level.FINE,
            "Managing restaurant " + restaurant.getName() + "(" + restaurant.getKey() + ")");
    }

    /**
     * find all control references
     */
    private void findControlReferences() {
        restaurantName = (TextView)findViewById(R.id.restaurantName);
        driversCurrentlyAssignedToRestaurant = (TextView)findViewById(R.id.driversCurrentlyAssignedToRestaurant);
        addDrivers = (BootstrapButton)findViewById(R.id.addDrivers);
        restaurantDriverList = (ListView)findViewById(R.id.restaurantDriverList);
    }

    /**
     * bind required event handlers
     */
    private void bindEventHandlers() {
        addDrivers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder dialogTitleBuilder = new StringBuilder();
                dialogTitleBuilder.append(resources.getString(R.string.availableDrivers));
                dialogTitleBuilder.append(" ");
                dialogTitleBuilder.append(restaurant.getName());

                //Inflate custom view
                LayoutInflater inflater = getLayoutInflater();
                View dialogLayout = inflater.inflate(R.layout.assign_restaurant_drivers, null);

                //Find listview in dialogLayout and assign data
                ListView availableRestaurantDriversList = (ListView)dialogLayout.findViewById(R.id.availableRestaurantDriversList);
                AssignRestaurantDriversListAdapter assignDriversListAdapter =
                        new AssignRestaurantDriversListAdapter((Activity)context, unassignedDrivers);
                availableRestaurantDriversList.setAdapter(assignDriversListAdapter);
                availableRestaurantDriversList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        TextView selectedDriver = (TextView)view.findViewById(android.R.id.text1);

                        if (!driversToAssignToRestaurant.contains(unassignedDrivers.get(position))) {
                            selectedDriver.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                            selectedDriver.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark));
                            driversToAssignToRestaurant.add(unassignedDrivers.get(position));
                        } else {
                            selectedDriver.setTextColor(ContextCompat.getColor(context, android.R.color.black));
                            selectedDriver.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
                            driversToAssignToRestaurant.remove(unassignedDrivers.get(position));
                        }
                    }
                });

                //Build alert dialog with custom view
                AlertDialog.Builder assignDriversDialog  = new AlertDialog.Builder(context);
                assignDriversDialog.setView(dialogLayout);
                assignDriversDialog
                        .setTitle(dialogTitleBuilder.toString());
                assignDriversDialog.setCancelable(false);
                assignDriversDialog.setPositiveButton(
                        resources.getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                for (User driverToAssignToRestaurant : driversToAssignToRestaurant) {
                                    driverToAssignToRestaurant.setRestaurantId(restaurant.getKey());
                                    users.update(driverToAssignToRestaurant, new String[] {"Driver"});
                                }

                                //clear list of driverToAssignToRestaurant
                                driversToAssignToRestaurant.clear();

                                //refresh list of assigned drivers
                                getAssignedRestaurantDrivers();

                                dialog.cancel();
                            }
                        });
                assignDriversDialog.setNegativeButton(
                        resources.getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                assignDriversDialog
                        .show();
            }
        });
    }

    /**
     * retrieve data
     */
    private void retrieveData() {
        //find all the users where the restaurant id matches the current restaurant id
        getAssignedRestaurantDrivers();
    }

    /**
     * perform any final layout updates
     */
    private void finalizeLayout() {
        //set restaurant name value
        restaurantName.setText(restaurant.getName());
        underlineText(driversCurrentlyAssignedToRestaurant);
    }

    /**
     * getAssignedRestaurantDrivers retrieves the drivers that are assigned to the restaurant
     */
    public void getAssignedRestaurantDrivers() {
        //find all the users where the restaurant id matches the current restaurant id
        users
            .atNode("Driver")
            .find("restaurantId = '" + restaurant.getKey() + "'")
            .addOnCompleteListener(ManageRestaurantDrivers.this, new OnCompleteListener<TaskResult<User>>() {
                @Override
                public void onComplete(@NonNull Task<TaskResult<User>> task) {
                    List<User> entities = task.getResult().getResults();
                    listAdapter = new ManageRestaurantDriversListAdapter((Activity)context, entities);
                    restaurantDriverList.setAdapter(listAdapter);

                    getUnassignedDrivers();
                }
            });
    }

    /**
     * getUnassignedDrivers retrieves all of the drivers that are not currently assigned to a restaurant
     */
    private void getUnassignedDrivers() {
        users
            .atNode("Driver")
            .find()
            .addOnCompleteListener(ManageRestaurantDrivers.this, new OnCompleteListener<TaskResult<User>>() {
                @Override
                public void onComplete(@NonNull Task<TaskResult<User>> task) {
                    List<User> entities = task.getResult().getResults();
                    unassignedDrivers.clear();

                    for (User entity : entities) {
                        if (entity.getRestaurantId() == null || entity.getRestaurantId().equals("")) {
                            unassignedDrivers.add(entity);
                        }
                    }
                }
            });
    }

    /**
     * underline the text in the provided textview
     * @param textViewControl identifies the textview control containing the text to be underlined
     */
    private void underlineText(TextView textViewControl) {
        String textToUnderline = textViewControl.getText().toString();
        SpannableString content = new SpannableString(textToUnderline);
        content.setSpan(new UnderlineSpan(), 0, textToUnderline.length(), 0);
        textViewControl.setText(content);
    }
}
