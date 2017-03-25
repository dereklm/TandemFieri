package com.gmail.dleemcewen.tandemfieri;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.Entities.OrderItem;
import com.gmail.dleemcewen.tandemfieri.Entities.Restaurant;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.Enums.OrderEnum;
import com.gmail.dleemcewen.tandemfieri.Json.AddressGeocode.AddressGeocode;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.loopj.android.http.AsyncHttpClient;

import java.util.ArrayList;
import java.util.Locale;

import static android.os.Build.VERSION_CODES.M;
import static com.google.android.gms.location.LocationServices.FusedLocationApi;

public class DriverDeliveryActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation, currentLocation, customerLocation;
    LocationRequest mLocationRequest;
    DatabaseReference mDatabase, mDatabaseRemoval, mDatabaseOwner;
    private AsyncHttpClient client;
    public int i, j;
    public ArrayList<Restaurant> restaurants, tempRestaurants;
    public AddressGeocode address;
    public boolean wait = false;
    public Marker[] markers;
    public Location tempLocation;
    public String customerID, ownerId;
    public Button navigateButton, completeButton, cancelButton;
    private Order order;
    private Double lat, lon;
    private User user;
    private TextView subTotal, tax, total, restaurantName, orderDate;
    private ListView viewOrderItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_delivery);

        subTotal = (TextView)findViewById(R.id.subTotal);
        tax = (TextView)findViewById(R.id.tax);
        total = (TextView)findViewById(R.id.total);
        restaurantName = (TextView)findViewById(R.id.restaurant_name);
        orderDate = (TextView)findViewById(R.id.date);
        viewOrderItems = (ListView)findViewById(R.id.cart_items);

        navigateButton = (Button) findViewById(R.id.navigateButton);
        completeButton = (Button) findViewById(R.id.completeButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);

        //completeButton.setBackgroundColor(Color.);

        customerID = getIntent().getStringExtra("customerId");
        order = (Order) getIntent().getSerializableExtra("Order");
        user = (User) getIntent().getSerializableExtra("User");

        lat = Double.parseDouble(order.getLatitude());
        lon = Double.parseDouble(order.getLongitude());

        customerLocation = new Location("");
        customerLocation.setLongitude(lon);
        customerLocation.setLatitude(lat);



        navigateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("google.navigation:q="+order.getLatitude() +"," +order.getLongitude()));
                startActivity(intent);
            }
        });




        if (isLocationEnabled(getApplicationContext()) == false){
            finish();
            Toast.makeText(getApplicationContext(),"Location services must be turned on", Toast.LENGTH_LONG).show();
        }

        currentLocation = new Location("");


        LocationManager locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if(locManager == null){
            Intent intent = new Intent(this, DriverMainMenu.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        boolean network_enabled = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Location location;

        if(network_enabled){

            currentLocation = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        }

        if (android.os.Build.VERSION.SDK_INT >= M) {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mDatabase = FirebaseDatabase.getInstance().getReference()
                .child("Delivery Location");
        mDatabaseRemoval = FirebaseDatabase.getInstance().getReference();

        mDatabaseOwner = FirebaseDatabase.getInstance().getReference().child("Delivery").child(user.getAuthUserID()).child("Order").child(order.getCustomerId()).child(order.getOrderId());

        mDatabaseOwner.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ownerId = (String) dataSnapshot.child("OwnerId").getValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase.child(customerID).child("Latitude").setValue(currentLocation.getLatitude()).equals("Latitude");
        mDatabase.child(customerID).child("Longitude").setValue(currentLocation.getLongitude()).equals("Longitude");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (isLocationEnabled(getApplicationContext()) == false){
                    finish();
                    Toast.makeText(getApplicationContext(),"Location services must be turned on", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Toast.makeText(getApplicationContext(), ""+(currentLocation.distanceTo(customerLocation) * 0.000621371), Toast.LENGTH_LONG).show();
        if((currentLocation.distanceTo(customerLocation) * 0.000621371) < 0.1){
            Toast.makeText(getApplicationContext(), "You are here", Toast.LENGTH_LONG).show();
            completeButton.setClickable(true);
            completeButton.setBackgroundColor(Color.parseColor("Green"));
        }


        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((currentLocation.distanceTo(customerLocation) * 0.000621371) > 0.1){
                    Toast.makeText(getApplicationContext(), "You are not there yet", Toast.LENGTH_LONG).show();
                }else {
                    mDatabaseRemoval.child("Delivery").child(user.getAuthUserID()).child("Order").child(order.getCustomerId()).child(order.getOrderId()).removeValue();
                    mDatabaseRemoval.child("Delivery").child(user.getAuthUserID()).child("currentOrderId").removeValue();
                    mDatabaseRemoval.child("Delivery Location").child(order.getCustomerId()).child("Latitude").removeValue();
                    mDatabaseRemoval.child("Delivery Location").child(order.getCustomerId()).child("Longitude").removeValue();
                    mDatabaseRemoval.child("Order").child(ownerId).child(order.getOrderId()).child("status").setValue(OrderEnum.COMPLETE);
                    finish();
                    Toast.makeText(getApplicationContext(), "Finish the delivery yah dingus", Toast.LENGTH_LONG).show();
                }
            }
        });


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
                                mDatabaseRemoval.child("Order").child(ownerId).child(order.getOrderId()).child("Assigned").removeValue();
                                mDatabaseRemoval.child("Delivery").child(user.getAuthUserID()).child("Order").child(order.getCustomerId()).child(order.getOrderId()).removeValue();
                                mDatabaseRemoval.child("Delivery").child(user.getAuthUserID()).child("currentOrderId").removeValue();
                                mDatabaseRemoval.child("Delivery Location").child(order.getCustomerId()).child("Latitude").removeValue();
                                mDatabaseRemoval.child("Delivery Location").child(order.getCustomerId()).child("Longitude").removeValue();
                                finish();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                Toast.makeText(getApplicationContext(),"Good! Now finish your job!", Toast.LENGTH_LONG).show();
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(DriverDeliveryActivity.this);
                builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
            }
        });
    }



    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        if (isLocationEnabled(getApplicationContext()) == false){
            finish();
            Toast.makeText(getApplicationContext(),"Location services must be turned on", Toast.LENGTH_LONG).show();
        }

        currentLocation = location;
        mLastLocation = location;


        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        mDatabase.child("Latitude").setValue(currentLocation.getLatitude()).equals("Latitude");
        mDatabase.child("Longtitude").setValue(currentLocation.getLongitude()).equals("Longitude");

        //stop location updates
        if (mGoogleApiClient != null) {
            FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        try {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Asking user if explanation is needed
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_LOCATION);


                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
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
        try {
            switch (requestCode) {
                case MY_PERMISSIONS_REQUEST_LOCATION: {
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                        if (ContextCompat.checkSelfPermission(this,
                                android.Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {

                            if (mGoogleApiClient == null) {
                                buildGoogleApiClient();
                            }

                        }

                    } else {

                        Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                    }
                    return;
                }


            }
        } catch (Exception e){
            Intent intent = new Intent(this, DinerMainMenu.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }






    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        if (isLocationEnabled(getApplicationContext()) == false){
            finish();
            Toast.makeText(getApplicationContext(),"Location services must be turned on", Toast.LENGTH_LONG).show();
        }
    }



}