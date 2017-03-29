package com.gmail.dleemcewen.tandemfieri;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.dleemcewen.tandemfieri.Entities.Day;
import com.gmail.dleemcewen.tandemfieri.Entities.DeliveryHours;
import com.gmail.dleemcewen.tandemfieri.Entities.Restaurant;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.Formatters.DateFormatter;
import com.gmail.dleemcewen.tandemfieri.Json.AddressGeocode.AddressGeocode;
import com.gmail.dleemcewen.tandemfieri.Repositories.RestaurantHours;
import com.gmail.dleemcewen.tandemfieri.Tasks.TaskResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.os.Build.VERSION_CODES.M;
import static com.google.android.gms.location.LocationServices.FusedLocationApi;
import static com.paypal.android.sdk.onetouch.core.metadata.ah.d;
import static com.paypal.android.sdk.onetouch.core.metadata.ah.r;

public class DinerMapActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    protected static final String TAG = "DinerMapActivity";

    private User user;
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation, currentLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    DatabaseReference mDatabase;

    public int i, j, k;
    public ArrayList<Restaurant> restaurants, tempRestaurants;
    public AddressGeocode address;
    public boolean wait = false;
    public Marker[] markers;
    public Location tempLocation;
    private String controlString;
    private Date currentDate;
    private RestaurantHours<DeliveryHours> restaurantHours;
    private boolean bool;
    private Restaurant theRestaurant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diner_map);

        currentDate = new Date();
        restaurantHours =  new RestaurantHours<>(DinerMapActivity.this);

        if (isLocationEnabled(getApplicationContext()) == false){
            finish();
            Toast.makeText(getApplicationContext(),"Location services must be turned on", Toast.LENGTH_LONG).show();
        }
        restaurants = new ArrayList<>();
        tempRestaurants = new ArrayList<>();
        currentLocation = new Location("");
        Bundle bundle = this.getIntent().getExtras();
        user = (User) bundle.getSerializable("User");
        controlString = bundle.getString("OpenClosed");

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

        if (network_enabled){
            currentLocation = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (android.os.Build.VERSION.SDK_INT >= M) {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mDatabase = FirebaseDatabase.getInstance().getReference()
                .child("Restaurant");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (isLocationEnabled(getApplicationContext()) == false){
                    finish();
                    Toast.makeText(getApplicationContext(),"Location services must be turned on", Toast.LENGTH_LONG).show();
                }
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    restaurants.add(child.getValue(Restaurant.class));
                }

                markers = new Marker[restaurants.size()];
                for (int j = 0; j < restaurants.size(); j++) {
                    initialize(restaurants.get(j));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        try {
            if (android.os.Build.VERSION.SDK_INT >= M) {
                if (ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    buildGoogleApiClient();
                    mMap.setMyLocationEnabled(true);
                }
            } else {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } catch (Exception e){
            Intent intent = new Intent(this, DinerMainMenu.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (isLocationEnabled(getApplicationContext()) == false){
                    finish();
                    Toast.makeText(getApplicationContext(),"Location services must be turned on", Toast.LENGTH_LONG).show();
                }
                try {
                    for (int k = 0; k < restaurants.size(); k++) {
                        if (marker.getTag().equals(restaurants.get(k).getId())) {

                        }
                    }
                } catch (Exception e) {

                }
                return false;
            }
        });

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                try {
                    for (k = 0; k < restaurants.size(); k++) {
                        if (marker.getTag().equals(restaurants.get(k).getId())) {
                            Toast.makeText(getApplicationContext(), "" + restaurants.get(k).getName(), Toast.LENGTH_SHORT).show();
                            theRestaurant = restaurants.get(k);
                        }
                    }
                            final int dayOfWeek = getDayOfWeekFromCurrentDate();


                            restaurantHours
                                    .find("restaurantId = '" + theRestaurant.getId() + "'")
                                    .addOnCompleteListener(DinerMapActivity.this, new OnCompleteListener<TaskResult<DeliveryHours>>() {
                                        @Override
                                        public void onComplete(@NonNull Task<TaskResult<DeliveryHours>> task) {
                                            List<DeliveryHours> deliveryHours = task.getResult().getResults();
                                            if (!deliveryHours.isEmpty() && !deliveryHours.get(0).getDays().isEmpty()) {
                                                StringBuilder hoursTextBuilder = new StringBuilder();

                                                Day dayHours = deliveryHours.get(0).getDays().get(dayOfWeek);
                                                if (dayHours.getOpen()) {
                                                    if (dayHours.compareOpenTimeWithCurrentTime(currentDate)) {
                                                        if (dayHours.compareClosedTimeWithCurrentTime(currentDate)) {
                                                            controlString = "OPEN";
                                                            Intent intent = new Intent(DinerMapActivity.this, LookAtMenuActivity.class);
                                                            Bundle bundle = new Bundle();
                                                            bundle.putString("Latitude", ""+currentLocation.getLatitude());
                                                            bundle.putString("Longitude", ""+currentLocation.getLongitude());
                                                            bundle.putSerializable("Restaurant",  theRestaurant);
                                                            bundle.putSerializable("User", user);
                                                            bundle.putString("OpenClosed", controlString);
                                                            intent.putExtras(bundle);

                                                            startActivity(intent);
                                                        } else {
                                                            controlString = "CLOSED";
                                                            Intent intent = new Intent(DinerMapActivity.this, LookAtMenuActivity.class);
                                                            Bundle bundle = new Bundle();
                                                            bundle.putString("Latitude", ""+currentLocation.getLatitude());
                                                            bundle.putString("Longitude", ""+currentLocation.getLongitude());
                                                            bundle.putSerializable("Restaurant",  theRestaurant);
                                                            bundle.putSerializable("User", user);
                                                            bundle.putString("OpenClosed", controlString);
                                                            intent.putExtras(bundle);

                                                            startActivity(intent);
                                                            //setRestaurantHoursText(hoursTextBuilder.toString(), restaurantOpenClosed, Color.argb(255, 128, 128, 128));
                                                        }
                                                    } else {
                                                        controlString = "CLOSED";
                                                        Intent intent = new Intent(DinerMapActivity.this, LookAtMenuActivity.class);
                                                        Bundle bundle = new Bundle();
                                                        bundle.putString("Latitude", ""+currentLocation.getLatitude());
                                                        bundle.putString("Longitude", ""+currentLocation.getLongitude());
                                                        bundle.putSerializable("Restaurant", theRestaurant);
                                                        bundle.putSerializable("User", user);
                                                        bundle.putString("OpenClosed", controlString);
                                                        intent.putExtras(bundle);

                                                        startActivity(intent);
                                                        //setRestaurantHoursText(hoursTextBuilder.toString(), restaurantOpenClosed, Color.argb(255, 128, 128, 128));
                                                    }
                                                } else {
                                                    controlString = "CLOSED";
                                                    Intent intent = new Intent(DinerMapActivity.this, LookAtMenuActivity.class);
                                                    Bundle bundle = new Bundle();
                                                    bundle.putString("Latitude", ""+currentLocation.getLatitude());
                                                    bundle.putString("Longitude", ""+currentLocation.getLongitude());
                                                    bundle.putSerializable("Restaurant",  theRestaurant);
                                                    bundle.putSerializable("User", user);
                                                    bundle.putString("OpenClosed", controlString);
                                                    intent.putExtras(bundle);

                                                    startActivity(intent);
                                                    //setRestaurantHoursText(hoursTextBuilder.toString(), restaurantOpenClosed, Color.argb(255, 128, 128, 128));
                                                }
                                            } else {
                                                controlString = "CLOSED";
                                                Intent intent = new Intent(DinerMapActivity.this, LookAtMenuActivity.class);
                                                Bundle bundle = new Bundle();
                                                bundle.putString("Latitude", ""+currentLocation.getLatitude());
                                                bundle.putString("Longitude", ""+currentLocation.getLongitude());
                                                bundle.putSerializable("Restaurant", theRestaurant);
                                                bundle.putSerializable("User", user);
                                                bundle.putString("OpenClosed", controlString);
                                                intent.putExtras(bundle);

                                                startActivity(intent);
                                                //setRestaurantHoursText(hoursTextBuilder.toString(), restaurantOpenClosed, Color.argb(255, 128, 128, 128));
                                            }

                                        }

                                    });


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                Context context = getApplicationContext();
                LinearLayout info = new LinearLayout(context);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(context);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(context);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
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
        Log.v(TAG, "Connected");

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        }

        markers = new Marker[restaurants.size()];
        for (int j = 0; j < restaurants.size(); j++) {
            initialize(restaurants.get(j));
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
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        markers = new Marker[restaurants.size()];
        for (int j = 0; j < restaurants.size(); j++) {
            initialize(restaurants.get(j));
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

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
                            mMap.setMyLocationEnabled(true);
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

    private void initialize(Restaurant restaurant) {
        if (restaurant.getLatitude() != null
                && restaurant.getLongitude() != null
                && restaurant.getMenu() != null
                && restaurant.getId() != null
                && currentLocation != null) {

            Location tempLocation = new Location("");
            tempLocation.setLatitude(restaurant.getLatitude());
            tempLocation.setLongitude(restaurant.getLongitude());

            float tempDistance = (currentLocation.distanceTo(tempLocation));

            tempDistance *= 0.000621371;

            if(restaurant.getDeliveryRadius() != null) {
                if ((int) tempDistance < restaurant.getDeliveryRadius()) {
                    double tempdouble = (double) tempDistance;
                    tempdouble *= 100;
                    tempdouble = Math.round(tempdouble);
                    tempdouble /= 100;
                    markers[j] = (mMap.addMarker(new MarkerOptions().position(new LatLng(restaurant.getLatitude(), restaurant.getLongitude())).title(restaurant.getName())));
                    markers[j].setSnippet("Restaurant Type: " + restaurant.getRestaurantType() + "\n" + "Delivery Charge: $" + restaurant.getCharge() + "\n" + " Distance Away: " + tempdouble);
                    markers[j].setTag(restaurant.getId());
                }
            }
        }
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }



    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT){
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

    private boolean getRestaurantHours(String restaurantId) {
        final int dayOfWeek = getDayOfWeekFromCurrentDate();


        restaurantHours
                .find("restaurantId = '" + restaurantId + "'")
                .addOnCompleteListener(DinerMapActivity.this, new OnCompleteListener<TaskResult<DeliveryHours>>() {
                    @Override
                    public void onComplete(@NonNull Task<TaskResult<DeliveryHours>> task) {
                        List<DeliveryHours> deliveryHours = task.getResult().getResults();
                        if (!deliveryHours.isEmpty() && !deliveryHours.get(0).getDays().isEmpty()) {
                            StringBuilder hoursTextBuilder = new StringBuilder();

                            Day dayHours = deliveryHours.get(0).getDays().get(dayOfWeek);
                            if (dayHours.getOpen()) {
                                if (dayHours.compareOpenTimeWithCurrentTime(currentDate)) {
                                    if (dayHours.compareClosedTimeWithCurrentTime(currentDate)) {
                                        hoursTextBuilder.append("CURRENTLY OPEN. ");
                                        hoursTextBuilder.append("Open today from ");
                                        hoursTextBuilder.append(DateFormatter.convertMilitaryTimeToStandardString(dayHours.getHourOpen()));
                                        hoursTextBuilder.append(" to ");
                                        hoursTextBuilder.append(DateFormatter.convertMilitaryTimeToStandardString(dayHours.getHourClosed()));
                                        hoursTextBuilder.append(".");
                                        bool = true;
                                        //setRestaurantHoursText(hoursTextBuilder.toString(), restaurantOpenClosed, Color.BLUE);
                                    } else {
                                        hoursTextBuilder.append("CLOSED. ");
                                        hoursTextBuilder.append("Open today from ");
                                        hoursTextBuilder.append(DateFormatter.convertMilitaryTimeToStandardString(dayHours.getHourOpen()));
                                        hoursTextBuilder.append(" to ");
                                        hoursTextBuilder.append(DateFormatter.convertMilitaryTimeToStandardString(dayHours.getHourClosed()));
                                        hoursTextBuilder.append(".");
                                        bool = false;
                                        //setRestaurantHoursText(hoursTextBuilder.toString(), restaurantOpenClosed, Color.argb(255, 128, 128, 128));
                                    }
                                } else {
                                    hoursTextBuilder.append("CURRENTLY CLOSED. ");
                                    hoursTextBuilder.append("Open today from ");
                                    hoursTextBuilder.append(DateFormatter.convertMilitaryTimeToStandardString(dayHours.getHourOpen()));
                                    hoursTextBuilder.append(" to ");
                                    hoursTextBuilder.append(DateFormatter.convertMilitaryTimeToStandardString(dayHours.getHourClosed()));
                                    hoursTextBuilder.append(".");
                                    bool = false;
                                   //setRestaurantHoursText(hoursTextBuilder.toString(), restaurantOpenClosed, Color.argb(255, 128, 128, 128));
                                }
                            } else {
                                hoursTextBuilder.append("CLOSED. ");
                                hoursTextBuilder.append("Not open today.");
                                bool = false;
                                //setRestaurantHoursText(hoursTextBuilder.toString(), restaurantOpenClosed, Color.argb(255, 128, 128, 128));
                            }
                        } else {
                            bool = false;
                            //setRestaurantHoursText("CLOSED. No delivery hours have been set for today.", restaurantOpenClosed, Color.argb(255, 128, 128, 128));
                        }

                    }

                });
        return bool;
    }

    private int getDayOfWeekFromCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);

        //have to subtract 1 because when the days of the week were created they were created as a
        //zero-based list, whereas calendar uses a 1-based list
        return calendar.get(Calendar.DAY_OF_WEEK) - 1;
    }

}