package com.gmail.dleemcewen.tandemfieri;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.gmail.dleemcewen.tandemfieri.Entities.Restaurant;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.Logging.LogWriter;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.os.Build.VERSION_CODES.M;
import static com.gmail.dleemcewen.tandemfieri.DinerMapActivity.MY_PERMISSIONS_REQUEST_LOCATION;

public class DinerMainMenu extends AppCompatActivity {
    User user;
    ListView listview;
    List<Restaurant> restaurantsList;
    DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diner_main_menu);

        getHandles();
        initialize();
        retrieveData();

        LogWriter.log(getApplicationContext(), Level.INFO, "The user is " + user.getEmail());
    }//end onCreate

    private void getHandles(){
        listview = (ListView) findViewById(R.id.diner_listview);
    }

    private void initialize(){
        Bundle bundle = this.getIntent().getExtras();
        user = (User) bundle.getSerializable("User");
        restaurantsList = new ArrayList<>();
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                openMenu((Restaurant) parent.getItemAtPosition(position));
            }
        });
    }

    private void openMenu(Restaurant r){
        Bundle restaurantBundle = new Bundle();
        Intent intent = new Intent(DinerMainMenu.this, LookAtMenuActivity.class);
        restaurantBundle.putSerializable("Restaurant", r);
        intent.putExtras(restaurantBundle);
        startActivity(intent);
    }

    //create menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.diner_menu, menu);
        return true;
    }

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
            case R.id.payment:
                Bundle bundle = new Bundle();
                bundle.putSerializable("User", user);

                Intent intent = new Intent(DinerMainMenu.this, FakePayment.class);
                intent.putExtras(bundle);
                startActivity(intent);

                return true;
            case R.id.rateRestaurant:
                rateRestaurant();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        Bundle dinerBundle = new Bundle();
        Intent intent = new Intent(DinerMainMenu.this, DinerMapActivity.class);
        Bundle userBundle = new Bundle();
        userBundle.putSerializable("User", user);
        intent.putExtras(userBundle);
        startActivity(intent);
    }

    private void launchDelivery(){
        //need to send user type so that the user can be located in the database
        Bundle dinerBundle = new Bundle();
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

                    //everything to do with restaurant list coode here
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        Restaurant r = child.getValue(Restaurant.class);
                        if(restaurantNearby(r)) {
                            restaurantsList.add(r);
                            //LogWriter.log(getApplicationContext(), Level.INFO, "The restaurant  is " + r.toString());
                        }
                    }

                    ArrayAdapter<Restaurant> adapter = new ArrayAdapter<>(
                            getApplicationContext(),
                            R.layout.diner_mainmenu_item_view,
                            restaurantsList);

                    listview.setAdapter(adapter);

                }//end on data change

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });//end listener
    }//end retrieve data

    private boolean restaurantNearby(Restaurant r){
        Geocoder coder = new Geocoder(getApplicationContext());
        List<android.location.Address> address = null;
        LatLng latlng;
        Location tempLocation;
        Location currentLocation = getUserLocation();

        String streetAddress = r.getStreet() + "," + r.getCity() + "," + r.getState() + "," + r.getZipcode();
        try {
            address = coder.getFromLocationName(streetAddress, 1);
            if (address.size() == 0) {

            } else {

                android.location.Address location = address.get(0);
                tempLocation = new Location("");


                tempLocation.set(currentLocation);
                tempLocation.setLatitude(location.getLatitude());
                tempLocation.setLongitude(location.getLongitude());
                float tempDistance = (currentLocation.distanceTo(tempLocation));
                tempDistance *= 0.000621371;
                if (r.getDeliveryRadius() != null) {
                    if ((int) tempDistance < r.getDeliveryRadius()) {
                        double tempdouble = (double) tempDistance;
                        tempdouble *= 100;
                        tempdouble = Math.round(tempdouble);
                        tempdouble /= 100;
                        return true;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
       return false;
    }//end restaurant nearby

    private Location getUserLocation(){
        Location result = new Location("");

        LocationManager locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if(locManager == null){
            Intent intent = new Intent(this, DinerMainMenu.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        boolean network_enabled = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

       if(network_enabled){

           result = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        }

        if (android.os.Build.VERSION.SDK_INT >= M) {
            checkLocationPermission();
        }
        return result;
    }
}//end Activity
