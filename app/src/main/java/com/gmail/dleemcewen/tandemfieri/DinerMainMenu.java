package com.gmail.dleemcewen.tandemfieri;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.dleemcewen.tandemfieri.Adapters.DinerRestaurantsListAdapter;
import com.gmail.dleemcewen.tandemfieri.Entities.NotificationMessage;
import com.gmail.dleemcewen.tandemfieri.Entities.Rating;
import com.gmail.dleemcewen.tandemfieri.Entities.Restaurant;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.Enums.OrderEnum;
import com.gmail.dleemcewen.tandemfieri.Formatters.DateFormatter;
import com.gmail.dleemcewen.tandemfieri.Logging.LogWriter;
import com.gmail.dleemcewen.tandemfieri.Repositories.NotificationMessages;
import com.gmail.dleemcewen.tandemfieri.Repositories.Ratings;
import com.gmail.dleemcewen.tandemfieri.Tasks.TaskResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.os.Build.VERSION_CODES.M;
import static com.gmail.dleemcewen.tandemfieri.DinerMapActivity.MY_PERMISSIONS_REQUEST_LOCATION;

public class DinerMainMenu extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener {
    protected static final String TAG = "DinerMainMenu";

    private NotificationMessages<NotificationMessage> notificationsRepository;
    private Ratings<Rating> ratingsRepository;
    private int notificationId;
    private String driverId;
    public boolean skipRating;
    User user;
    ListView listview;
    List<Restaurant> restaurantsList;
    DatabaseReference mDatabase;
    static MenuItem deliveryOption;
    private String controlString;

    protected GoogleApiClient mMap;
    protected Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diner_main_menu);

        getHandles();
        initialize();
        getNotificationOrderDataToDisplayForDriverRating(notificationId, skipRating);
        //retrieveData();
        resendExistingDinerNotifications();

        LogWriter.log(getApplicationContext(), Level.INFO, "The user is " + user.getEmail());
    }//end onCreate


    @Override
    protected void onStart() {
        super.onStart();

        if (mMap != null) {
            mMap.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mMap.isConnected()) {
            mMap.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.v(TAG, "Map Connected");

        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mMap);
        } catch (SecurityException e) {
            Log.i(TAG, "SecurityException: " + e.getMessage());
        }

        if (mLastLocation != null) {
            restaurantsList.clear();
            retrieveData();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: " + result.getErrorMessage());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection suspended");
    }

    private void getHandles(){
        listview = (ListView) findViewById(R.id.diner_listview);
    }

    private void initialize(){
        notificationId = 0;
        notificationsRepository = new NotificationMessages<>(DinerMainMenu.this);
        ratingsRepository = new Ratings<>(DinerMainMenu.this);

        Bundle bundle = this.getIntent().getExtras();
        user = (User) bundle.getSerializable("User");
        notificationId = bundle.getInt("notificationId");
        driverId = bundle.getString("driverId");
        skipRating = bundle.getBoolean("skipRating", false);

        restaurantsList = new ArrayList<>();
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                    TextView textView = (TextView) view.findViewById(R.id.restaurantOpenClosed);
                    controlString = textView.getText().toString();

                    openMenu((Restaurant) parent.getItemAtPosition(position), controlString);
            }
        });

        verifyLocationSettings();
    }

    private void openMenu(Restaurant r, String controlString){

        Bundle restaurantBundle = new Bundle();
        Intent intent = new Intent(DinerMainMenu.this, LookAtMenuActivity.class);
        restaurantBundle.putSerializable("Restaurant", r);
        restaurantBundle.putString("OpenClosed", controlString);
        restaurantBundle.putSerializable("User", user);
        restaurantBundle.putString("Latitude", String.valueOf(mLastLocation.getLatitude()));
        restaurantBundle.putString("Longitude", String.valueOf(mLastLocation.getLongitude()));
        intent.putExtras(restaurantBundle);
        startActivity(intent);
    }

    //create menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.diner_menu, menu);
        deliveryOption = menu.findItem(R.id.delivery);
        return true;
    }

    public static MenuItem getDeliveryMenuItem(){return deliveryOption;}

    //determine which menu option was selected and call that option's action method
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.sign_out:
                signOut();
                return true;
            case R.id.edit_personal_info:
                editPersonalInformation();
                return true;
            case R.id.edit_password:
                editPassword();
                return true;
            case R.id.map:
                launchMap();
                return true;
            case R.id.delivery:
                launchDelivery();
                return true;
            case R.id.order_history:
                displayOrderHistory();
                return true;
            case R.id.rateRestaurant:
                rateRestaurant();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void displayOrderHistory(){
        Bundle dinerBundle = new Bundle();
        Intent intent = new Intent(DinerMainMenu.this, DinerOrderHistoryActivity.class);
        dinerBundle.putSerializable("User", user);
        intent.putExtras(dinerBundle);
        startActivity(intent);
    }

    //called when user selects sign out from the drop down menu
    private void signOut(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("Exit me", true);
        startActivity(intent);
        finish();
    }

    //called when user selects edit information from the drop down menu
    private  void editPersonalInformation(){
        //need to send user type so that the user can be located in the database
        Bundle dinerBundle = new Bundle();
        Intent intent = new Intent(DinerMainMenu.this, EditAccountActivity.class);
        dinerBundle.putSerializable("User", user);
        intent.putExtras(dinerBundle);
        intent.putExtra("UserType", "Diner");
        startActivity(intent);
    }

    //called when user selects edit password from the drop down menu
    private void editPassword(){
        //need to send user type so that the user can be located in the database
        Bundle dinerBundle = new Bundle();
        Intent intent = new Intent(DinerMainMenu.this, EditPasswordActivity.class);
        dinerBundle.putSerializable("User", user);
        intent.putExtras(dinerBundle);
        intent.putExtra("UserType", "Diner");
        startActivity(intent);
    }

    private void launchMap(){
        //need to send user type so that the user can be located in the database
        Intent intent = new Intent(DinerMainMenu.this, DinerMapActivity.class);
        Bundle userBundle = new Bundle();
        userBundle.putSerializable("User", user);
        userBundle.putString("OpenClosed", controlString);
        intent.putExtras(userBundle);
        startActivity(intent);
    }

    private void launchDelivery(){
        //need to send user type so that the user can be located in the database
        Intent intent = new Intent(DinerMainMenu.this, DeliveryMapActivity.class);
        Bundle userBundle = new Bundle();
        userBundle.putSerializable("User", user);
        intent.putExtras(userBundle);
        startActivity(intent);
    }

    private void rateRestaurant() {
        Intent rateRestaurantIntent = new Intent(getApplicationContext(), RestaurantRatings.class);
        startActivity(rateRestaurantIntent);
    }

    /**
     * getNotificationOrderDataToDisplayForDriverRating gets the order data from the notification to use
     * to be able to display and collect a driver rating
     */
    private void getNotificationOrderDataToDisplayForDriverRating(final int notificationId, final boolean skipRating) {
        if (notificationId != 0) {
            NotificationManager notificationManager =
                    (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);

            notificationManager.cancel(notificationId);

            //run notification query
            new Thread(new Runnable() {
                @Override
                public void run() {
                    notificationsRepository
                            .find("notificationId = '" + notificationId + "'")
                            .addOnCompleteListener(DinerMainMenu.this, new OnCompleteListener<TaskResult<NotificationMessage>>() {
                                @Override
                                public void onComplete(@NonNull Task<TaskResult<NotificationMessage>> task) {
                                    List<NotificationMessage> messages = task.getResult().getResults();
                                    if (!messages.isEmpty()) {
                                        notificationsRepository.remove(messages.get(0));

                                        if (!skipRating) {
                                            HashMap orderData = (HashMap) messages.get(0).getData();
                                            showDriverRatingDialog(orderData);
                                        }
                                    }
                                }
                            });

                }
            }).start();
        }
    }

    /**
     * showDriverRatingDialog shows the driver rating dialog
     */
    private void showDriverRatingDialog(final HashMap orderData) {
        Resources resources = DinerMainMenu.this.getResources();

        StringBuilder dialogTitleBuilder = new StringBuilder();
        dialogTitleBuilder.append("Rate your driver");

        LayoutInflater layoutInflater = (LayoutInflater) DinerMainMenu.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //Inflate custom view
        View dialogLayout = layoutInflater.inflate(R.layout.assign_driver_rating, null);

        //Find ratingsbar in dialoglayout
        final RatingBar driverRatingBar = (RatingBar)dialogLayout.findViewById(R.id.driverRatingBar);
        final TextView driverRatingText = (TextView)dialogLayout.findViewById(R.id.driverRatingText);

        driverRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                driverRatingText.setText(String.valueOf(rating));
            }
        });

        //Build alert dialog with custom view
        AlertDialog.Builder rateDriverDialog  = new AlertDialog.Builder(DinerMainMenu.this);
        rateDriverDialog.setView(dialogLayout);
        rateDriverDialog
                .setTitle(dialogTitleBuilder.toString());
        rateDriverDialog.setCancelable(false);
        rateDriverDialog.setPositiveButton(
                resources.getString(R.string.save),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Rating newDriverRating = new Rating();
                        newDriverRating.setDate(DateFormatter.toTimeStamp(new Date()).toString());
                        newDriverRating.setRating(driverRatingBar.getRating());
                        newDriverRating.setRestaurantId(orderData.get("restaurantId").toString());
                        newDriverRating.setOrderId(orderData.get("orderId").toString());
                        newDriverRating.setDriverId(driverId);

                        ratingsRepository.add(newDriverRating);

                        dialog.cancel();
                    }
                });

        rateDriverDialog.setNegativeButton(
                resources.getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        rateDriverDialog
                .show();

    }

    /**
     * resendExistingDinerNotifications re-sends existing diner notifications that are notifications
     * for the current diner
     */
    private void resendExistingDinerNotifications() {
        //no need to sent notifications if we are actually arriving at the dinermainmenu
        //from a notification
        if (notificationId == 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    notificationsRepository
                        .find("notificationType = 'Order'")
                        .addOnCompleteListener(DinerMainMenu.this, new OnCompleteListener<TaskResult<NotificationMessage>>() {
                            @Override
                            public void onComplete(@NonNull Task<TaskResult<NotificationMessage>> task) {
                                List<NotificationMessage> messages = task.getResult().getResults();
                                if (!messages.isEmpty()) {
                                    for (NotificationMessage message : messages) {
                                        HashMap data = (HashMap)message.getData();

                                        if (data.get("customerId").equals(user.getAuthUserID())
                                                && data.get("status").equals(OrderEnum.COMPLETE.toString())) {
                                            notificationsRepository.resendNotification(message);
                                        }
                                    }
                                }
                            }
                        });
                }
            }).start();
        }
    }

    public boolean checkLocationPermission() {
        try {
            if (ContextCompat.checkSelfPermission(this,
                    ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Asking user if explanation is needed
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        ACCESS_FINE_LOCATION)) {

                    ActivityCompat.requestPermissions(this,
                            new String[]{ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_LOCATION);


                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_LOCATION);
                }

                return false;
            } else {
                return true;
            }
        } catch (Exception e){
            Intent intent = new Intent(this, DinerMainMenu.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchMap();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Toast.makeText(getApplicationContext(), "Some features will be unobtainable", Toast.LENGTH_LONG).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    /*Retrieves restaurants within delivery range*/
    private void retrieveData(){

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Restaurant");

        mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    restaurantsList.clear();

                    //everything to do with restaurant list code here
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        Restaurant r = child.getValue(Restaurant.class);
                        if(restaurantNearby(r)) {
                            restaurantsList.add(r);
                        }
                    }

                    DinerRestaurantsListAdapter adapter =
                        new DinerRestaurantsListAdapter(DinerMainMenu.this, restaurantsList);

                    listview.setAdapter(adapter);

                }//end on data change

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });//end listener
    }//end retrieve data

    private boolean restaurantNearby(Restaurant r){
        if (r.getLatitude() != null
                && r.getLongitude() != null
                && r.getMenu() != null
                && r.getId() != null) {

            Location tempLocation = new Location("");
            tempLocation.setLatitude(r.getLatitude());
            tempLocation.setLongitude(r.getLongitude());

            float tempDistance = (mLastLocation.distanceTo(tempLocation));

            tempDistance *= 0.000621371;

            Log.v(TAG + ":GEOCODE", String.valueOf(mLastLocation.getLatitude()));
            Log.v(TAG + ":GEOCODE", String.valueOf(tempLocation.getLatitude()));
            Log.v(TAG + ":GEOCODE", r.getName());

            if(r.getDeliveryRadius() != null) {
                if ((int) tempDistance < r.getDeliveryRadius()) {
                    return true;
                }
            }
        }

       return false;
    }//end restaurant nearby

    private void verifyLocationSettings() {
        boolean locationPermission = true;

        LocationManager locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if(locManager == null){
            Intent intent = new Intent(this, DinerMainMenu.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        boolean network_enabled = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!network_enabled) {
            network_enabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }

        if (android.os.Build.VERSION.SDK_INT >= M) {
            checkLocationPermission();
        }

        Log.v(TAG, "Location Permission: " + String.valueOf(locationPermission));
        Log.v(TAG, "Network enabled: " + String.valueOf(network_enabled));

        if (locationPermission && network_enabled) {
            Log.v(TAG, "BUILD CLIENT");
            buildGoogleApiClient();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mMap = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mMap.connect();
    }

    @Override
    public void onResume(){
        super.onResume();
        if(!restaurantsList.isEmpty()){
            restaurantsList.clear();
            retrieveData();
        }
    }
}//end Activity