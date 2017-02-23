package com.gmail.dleemcewen.tandemfieri;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;

import com.gmail.dleemcewen.tandemfieri.Adapters.DriverRatingsListAdapter;
import com.gmail.dleemcewen.tandemfieri.Comparators.RatingsByDriverInAscendingOrderComparator;
import com.gmail.dleemcewen.tandemfieri.Entities.Rating;
import com.gmail.dleemcewen.tandemfieri.Entities.Restaurant;
import com.gmail.dleemcewen.tandemfieri.EventListeners.QueryCompleteListener;
import com.gmail.dleemcewen.tandemfieri.Repositories.Ratings;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class DriverRatings extends AppCompatActivity {
    private EditText startDate;
    private EditText endDate;
    private Button viewDriverRatings;
    private ListView ratingsList;
    private Ratings<Rating> ratingsRepository;
    private Restaurant restaurant;
    private Calendar calendar;
    private Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_ratings);

        initialize();
        findControlReferences();
        bindEventHandlers();
    }

    /**
     * initialize all necessary variables
     */
    private void initialize() {
        resources = this.getResources();
        ratingsRepository = new Ratings<>(this);
        calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        Bundle bundle = new Bundle();
        bundle = this.getIntent().getExtras();
        restaurant = (Restaurant)bundle.getSerializable("Restaurant");
        restaurant.setKey(bundle.getString("key"));
    }

    /**
     * find all control references
     */
    private void findControlReferences() {
        startDate = (EditText)findViewById(R.id.startDate);
        endDate = (EditText)findViewById(R.id.endDate);
        viewDriverRatings = (Button)findViewById(R.id.viewDriverRatings);
        ratingsList = (ListView)findViewById(R.id.driverRatingsList);
    }

    /**
     * bind required event handlers
     */
    private void bindEventHandlers() {
        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(DriverRatings.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        startDate.setText(
                                String.valueOf(month + 1) + "/" + String.valueOf(dayOfMonth) + "/" + String.valueOf(year));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

                dialog.show();
            }
        });

        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(DriverRatings.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String formattedEndDate = String.valueOf(month + 1) + "/" + String.valueOf(dayOfMonth) + "/" + String.valueOf(year);
                        if (isEndDateAfterStartDate(startDate.getText().toString(), formattedEndDate)) {
                            endDate.setText(
                                    String.valueOf(month + 1) + "/" + String.valueOf(dayOfMonth) + "/" + String.valueOf(year));
                        } else {
                            AlertDialog.Builder invalidDateDialog  = new AlertDialog.Builder(DriverRatings.this);
                            invalidDateDialog
                                    .setMessage(resources.getString(R.string.invalidEndDate));
                            invalidDateDialog
                                    .setTitle(resources.getString(R.string.driverRatingsActivityTitle));
                            invalidDateDialog.setCancelable(false);
                            invalidDateDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            invalidDateDialog.setNegativeButton(R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            invalidDateDialog
                                    .create()
                                    .show();
                        }
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

                dialog.show();
            }
        });

        viewDriverRatings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retrieveData();
            }
        });
    }

    /**
     * retrieve data
     */
    private void retrieveData() {
        final List<Rating> matchedRatingsList = new ArrayList<>();

        if (startDate.getText().toString().equals("") && endDate.getText().toString().equals("")) {
            //no start and end date range has been provided, so get all of the ratings for the current restaurant
            ratingsRepository.find(Arrays.asList("restaurantId"), Arrays.asList(restaurant.getKey()), new QueryCompleteListener<Rating>() {
                @Override
                public void onQueryComplete(ArrayList<Rating> entities) {
                    matchedRatingsList.addAll(entities);
                    buildRatings(matchedRatingsList);
                }
            });
        } else {
            //get all the ratings that are in the start and end date range
            ratingsRepository.find(
                Arrays.asList("date"),
                Arrays.asList(startDate.getText().toString(), endDate.getText().toString()),
                new QueryCompleteListener<Rating>() {
                    @Override
                    public void onQueryComplete(ArrayList<Rating> entities) {
                        //all ratings for date range could include ratings for other restaurants
                        //so filter out all entries except those for the current restaurant
                        for (Rating rating : entities) {
                            if (rating.getRestaurantId().equals(restaurant.getKey())) {
                                matchedRatingsList.add(rating);
                            }
                        }

                        buildRatings(matchedRatingsList);
                    }
            });
        }
    }

    /**
     * buildRatings builds the ratings for the matched ratings
     * @param matchedRatingsList indicates the list of matched ratings
     */
    private void buildRatings(List<Rating> matchedRatingsList) {
        //arraylist to store driver and average rating
        ArrayList<Map.Entry<String, Double>> driverRatingsList = new ArrayList<>();

        //Sort ratingslist by driver in ascending order
        Collections.sort(matchedRatingsList, new RatingsByDriverInAscendingOrderComparator());

        //Ensure the method is working with a distinct list of drivers
        //matching to the drivers in the matched ratings list
        List<Map.Entry<String, String>> distinctDrivers =
                buildDistinctListOfDrivers(restaurant.getDrivers(), matchedRatingsList);

        //Calculate each driver's average rating
        for (Map.Entry<String, String> driver : distinctDrivers) {
            double averageRating = buildDriverAverageRating(driver.getKey(), matchedRatingsList);
            Map.Entry<String, Double> driverRating =
                    new AbstractMap.SimpleEntry<>(driver.getValue(), averageRating);
            driverRatingsList.add(driverRating);
        }

        DriverRatingsListAdapter adapter = new DriverRatingsListAdapter(this, driverRatingsList);
        ratingsList.setAdapter(adapter);
    }

    /**
     * buildDistinctListOfDrivers builds a distinct list of drivers for the current restaurant
     * @param drivers indicates the drivers associated with the current restaurant
     * @return a distinct list of the driver ids and names
     */
    private List<Map.Entry<String, String>> buildDistinctListOfDrivers(
            Map<String, String> drivers, List<Rating> matchedRatingsList) {
        List<Map.Entry<String, String>> distinctListOfDrivers = new ArrayList<>();

        if (drivers != null && !drivers.isEmpty()) {
            Set<String> distinctDriverIds =  buildDistinctListOfDriverIds(matchedRatingsList);
            for (String driverId : distinctDriverIds) {
                Map.Entry<String, String> distinctDriver = null;
                for (Map.Entry<String, String> driver : drivers.entrySet()) {
                    if (distinctDriver == null && driver.getKey().equals(driverId)) {
                        distinctDriver = new AbstractMap.SimpleEntry<>(driverId, driver.getValue());
                    }
                }

                distinctListOfDrivers.add(distinctDriver);
            }
        }

        return distinctListOfDrivers;
    }

    /**
     * buildDistinctListOfDriverIds returns a list of distinct driver ids
     * that are also in the matched ratings list
     * @return list of distinct driver ids
     */
    private Set<String> buildDistinctListOfDriverIds(List<Rating> matchedRatingsList) {
        Set<String> distinctDriverIds = new HashSet<>();
        for (Rating rating : matchedRatingsList) {
            for (Map.Entry<String, String> driver : restaurant.getDrivers().entrySet()) {
                if (rating.getDriverId().equals(driver.getKey())) {
                    distinctDriverIds.add(driver.getKey());
                }
            }
        }

        return distinctDriverIds;
    }

    /**
     * buildDriverAverageRating builds the average rating for the provided driverid
     * @param driverId uniquely identifies the driver to build the average rating for
     * @param matchedRatingsList indicates the list of ratings that match the query criteria
     * @return average rating for the provided driverid
     */
    private double buildDriverAverageRating(String driverId, List<Rating> matchedRatingsList) {
        double totalRating = 0;
        int numberOfRatings = 0;
        double averageRating = 0;

        for (Rating rating : matchedRatingsList) {
            if (rating.getDriverId().equals(driverId)) {
                totalRating += rating.getRating();
                numberOfRatings++;
            }
        }

        if (numberOfRatings > 0) {
            averageRating = totalRating / numberOfRatings;
        }

        return averageRating;
    }

    /**
     * isEndDateAfterStartDate checks to see if the provided end date occurs after the
     * provided start date
     * @param startDate indicates the start date
     * @param endDate indicates the end date
     * @return true or false
     */
    private boolean isEndDateAfterStartDate(String startDate, String endDate) {
        DateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        Date startingDate = new Date();
        Date endingDate = new Date();

        try {
            startingDate = format.parse(startDate);
            endingDate = format.parse(endDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return endingDate.after(startingDate);
    }
}
