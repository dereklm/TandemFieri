package com.gmail.dleemcewen.tandemfieri;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.api.defaults.DefaultBootstrapBrand;
import com.gmail.dleemcewen.tandemfieri.Constants.NotificationConstants;
import com.gmail.dleemcewen.tandemfieri.Entities.NotificationMessage;
import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.Entities.OrderItem;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.Enums.OrderEnum;
import com.gmail.dleemcewen.tandemfieri.Utility.General;
import com.gmail.dleemcewen.tandemfieri.Repositories.NotificationMessages;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class DriverDeliveryActivity extends AppCompatActivity implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener {

    protected static final String TAG = "DriverDeliveryActivity";
    protected static final int REQUEST_CHECK_SETTINGS = 0X1;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    protected LocationSettingsRequest mLocationSettingsRequest;
    protected Location mCurrentLocation, customerLocation;

    protected boolean mRequestingLocationUpdates;

    private DatabaseReference mDatabase, mDatabaseOwner;

    private BootstrapButton navigateButton, completeButton, cancelButton;
    private TextView subTotal, tax, total, restaurantName, orderDate;
    private ListView viewOrderItems;
    private NotificationMessages<NotificationMessage> notifications;

    private String customerID, ownerId;
    private Double lat, lon;
    private Order order;
    private User user;

    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_delivery);

        notifications = new NotificationMessages<>(DriverDeliveryActivity.this);
      
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
        initialize();
        findControlReferences();
        bindEventListeners();
        bindEventHandlers();
        updateLayout();

        mRequestingLocationUpdates = true;
        startLocationUpdates();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (requestCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");

                        mRequestingLocationUpdates = false;
                        break;
                }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected  void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (mCurrentLocation == null) {
            try {
                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            } catch (SecurityException e) {
                Log.i(TAG, "No permission:" +  e.getMessage().toString());
            }
        }

        if (mRequestingLocationUpdates) {
            Log.i(TAG, "In onConnected(), starting location updates");
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection suspended");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "Location changed.");

        if (mRequestingLocationUpdates) {
            updateLocation(location);
            updateUI();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    protected void startLocationUpdates() {
        LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, mLocationSettingsRequest
            ).setResultCallback((new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult locationSettingsResult) {
                    final Status status = locationSettingsResult.getStatus();

                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            Log.i(TAG, "All location settings are satisfied.");

                            try {
                                LocationServices.FusedLocationApi.requestLocationUpdates(
                                        mGoogleApiClient, mLocationRequest, DriverDeliveryActivity.this);
                            } catch (SecurityException e) {
                                Log.i(TAG, "No permission:" +  e.getMessage().toString());
                            }
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            Log.i(TAG, "Location settings are not satisfied. Attempting to change settings.");

                            try {
                                status.startResolutionForResult(DriverDeliveryActivity.this, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                Log.i(TAG, "PendingIntent unable to execute request.");
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            String errorMessage = "Location settings are inadequate, and cannot be fixed here. Fix in settings";

                            Log.e(TAG, errorMessage);
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();

                            mRequestingLocationUpdates = false;

                            finish();
                    }
                }
        }));
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this
            ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                mRequestingLocationUpdates = false;
            }
        });
    }

    private void initialize() {
        customerID = getIntent().getStringExtra("customerId");
        order = (Order) getIntent().getSerializableExtra("Order");
        user = (User) getIntent().getSerializableExtra("User");

        lat = Double.parseDouble(order.getLatitude());
        lon = Double.parseDouble(order.getLongitude());

        customerLocation = new Location("");
        customerLocation.setLongitude(lon);
        customerLocation.setLatitude(lat);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabaseOwner = FirebaseDatabase.getInstance().getReference().child("Delivery").child(user.getAuthUserID()).child("Order").child(order.getCustomerId()).child(order.getOrderId());
    }

    private void findControlReferences() {
        subTotal = (TextView)findViewById(R.id.subTotal);
        tax = (TextView)findViewById(R.id.tax);
        total = (TextView)findViewById(R.id.total);
        restaurantName = (TextView)findViewById(R.id.restaurant_name);
        orderDate = (TextView)findViewById(R.id.date);
        viewOrderItems = (ListView)findViewById(R.id.cart_items);

        navigateButton = (BootstrapButton) findViewById(R.id.navigateButton);
        completeButton = (BootstrapButton) findViewById(R.id.completeButton);
        cancelButton = (BootstrapButton) findViewById(R.id.cancelButton);
    }

    private void bindEventListeners() {
        mDatabaseOwner.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ownerId = (String) dataSnapshot.child("OwnerId").getValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void bindEventHandlers() {
        navigateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("google.navigation:q=" + order.getLatitude() + "," + order.getLongitude()));
                startActivity(intent);
            }
        });


        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAtLocation()){
                    completeOrderDialog();
                } else {
                    Toast.makeText(getApplicationContext(), "You are not there yet", Toast.LENGTH_LONG).show();
                }
            }
        });


        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                Toast.makeText(getApplicationContext(),"Ok I guess you won't get paid", Toast.LENGTH_LONG).show();

                                mDatabase.child("Order").child(ownerId).child(order.getOrderId()).child("Assigned").removeValue();
                                mDatabase.child("Order").child(ownerId).child(order.getOrderId()).child("status").setValue(OrderEnum.CREATING);
                                mDatabase.child("Delivery").child(user.getAuthUserID()).child("Order").child(order.getCustomerId()).child(order.getOrderId()).removeValue();
                                mDatabase.child("Delivery").child(user.getAuthUserID()).child("currentOrderId").removeValue();
                                mDatabase.child("Delivery Location").child(order.getCustomerId()).removeValue();

                                finish();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                Toast.makeText(context, "Good! Now finish your job!", Toast.LENGTH_LONG).show();
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
            }
        });
    }

    private void updateLayout() {
        completeButton.setClickable(false);

        ArrayAdapter<OrderItem> adapter = new ArrayAdapter<OrderItem>(
                getApplicationContext(),
                R.layout.view_order_items,
                order.getItems());
        viewOrderItems.setAdapter(adapter);

        restaurantName.setText(order.getRestaurantName());
        subTotal.setText("Subtotal: $" + String.format(Locale.US, "%.2f", order.getSubTotal()));
        tax.setText("Tax: $" + String.format(Locale.US, "%.2f", order.getTax()));
        total.setText("Total: $" + String.format(Locale.US, "%.2f", order.getTotal()));
        orderDate.setText("Date of order: " + order.dateToString());

        completeButton.setBootstrapBrand(DefaultBootstrapBrand.REGULAR);
    }

    private void updateLocation(Location location) {
        if (location != null) {
            mCurrentLocation = location;
            mDatabase.child("Delivery Location").child(customerID).child("Latitude").setValue(mCurrentLocation.getLatitude()).equals("Latitude");
            mDatabase.child("Delivery Location").child(customerID).child("Longitude").setValue(mCurrentLocation.getLongitude()).equals("Longitude");
        }
    }

    private void updateUI() {
        if (mCurrentLocation != null) {
            if (isAtLocation()){
                Toast.makeText(context, "You are here", Toast.LENGTH_LONG).show();
                completeButton.setClickable(true);
                completeButton.setBootstrapBrand(DefaultBootstrapBrand.SUCCESS);

                stopLocationUpdates();
            }
        }
    }

    private boolean isAtLocation() {
        if (mCurrentLocation != null && customerLocation != null) {
            if ((mCurrentLocation.distanceTo(customerLocation) * 0.000621371) < 0.1){
                return true;
            }
        }

        return false;
    }

    private void completeOrderDialog() {
        LayoutInflater li = LayoutInflater.from(context);
        View view = li.inflate(R.layout.dialog_driver_complete_deliver, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(view);

        final EditText comments = (EditText) view.findViewById(R.id.et_comments);

        alertDialogBuilder
                .setTitle(getString(R.string.complete_delivery))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                order.setStatus(OrderEnum.COMPLETE);

                                if (!General.isEditTextEmpty(comments)) {
                                    order.setDriverComment(comments.getText().toString());
                                }

                                //reemoved to keep track of passed orders
                                //mDatabase.child("Delivery").child(user.getAuthUserID()).child("Order").child(order.getCustomerId()).child(order.getOrderId()).removeValue();
                                mDatabase.child("Delivery").child(user.getAuthUserID()).child("Order").child(order.getCustomerId()).child(order.getOrderId()).child("status").setValue(OrderEnum.COMPLETE);
                                //mDatabase.child("Delivery").child(user.getAuthUserID()).child("currentOrderId").removeValue();
                                mDatabase.child("Delivery Location").child(order.getCustomerId()).removeValue();
                                mDatabase.child("Order").child(ownerId).child(order.getOrderId()).setValue(order);

                                //send notification to diner for driver rating
                                sendNotificationToDiner(order, user);

                                finish();
                                Toast.makeText(getApplicationContext(), "Order completed.", Toast.LENGTH_LONG).show();
                            }
                        })
                .setNegativeButton(getString(R.string.no),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void sendNotificationToDiner(Order order, User user) {
        order.setStatus(OrderEnum.COMPLETE);
        notifications.sendNotification(NotificationConstants.Action.ADDED, order, user.getAuthUserID());
    }
}
