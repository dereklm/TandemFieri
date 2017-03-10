package com.gmail.dleemcewen.tandemfieri;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.gmail.dleemcewen.tandemfieri.Abstracts.Entity;
import com.gmail.dleemcewen.tandemfieri.Constants.NotificationConstants;
import com.gmail.dleemcewen.tandemfieri.Entities.NotificationMessage;
import com.gmail.dleemcewen.tandemfieri.Entities.Restaurant;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.Logging.LogWriter;
import com.gmail.dleemcewen.tandemfieri.Repositories.NotificationMessages;
import com.gmail.dleemcewen.tandemfieri.Repositories.Restaurants;
import com.gmail.dleemcewen.tandemfieri.Repositories.Users;
import com.gmail.dleemcewen.tandemfieri.Tasks.TaskResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.logging.Level;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.os.Build.VERSION_CODES.M;
import static com.gmail.dleemcewen.tandemfieri.DinerMapActivity.MY_PERMISSIONS_REQUEST_LOCATION;

public class DinerMainMenu extends AppCompatActivity {
    User user;
    private Button rateRestaurant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diner_main_menu);

        Bundle bundle = new Bundle();
        bundle = this.getIntent().getExtras();
        user = (User) bundle.getSerializable("User");

        LogWriter.log(getApplicationContext(), Level.INFO, "The user is " + user.getEmail());

        findControlReferences();
        bindEventHandlers();
    }//end onCreate

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
                if (android.os.Build.VERSION.SDK_INT >= M) {
                    if(checkLocationPermission() == true){
                        launchMap();
                    }else{
                    }
                }
                return true;
            case R.id.delivery:
                launchDelivery();
                return true;
            case R.id.sendSimulatedNotification:
                //TODO: remove this after ordering and payment processing are in place
                //this is just for testing and demo purposes
                simulateCompletedOrder();
                return true;
            case R.id.payment:
                Bundle bundle = new Bundle();
                bundle.putSerializable("User", user);

                Intent intent = new Intent(DinerMainMenu.this, FakePayment.class);
                intent.putExtras(bundle);
                startActivity(intent);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * find all control references
     */
    private void findControlReferences() {
        rateRestaurant = (Button)findViewById(R.id.rateRestaurant);
    }

    /**
     * bind required event handlers
     */
    private void bindEventHandlers() {
        rateRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RestaurantRatings.class);
                startActivity(intent);
            }
        });
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
        //Bundle dinerBundle = new Bundle();
        Intent intent = new Intent(DinerMainMenu.this, DinerMapActivity.class);
        //dinerBundle.putSerializable("User", user);
        //intent.putExtras(dinerBundle);
        //intent.putExtra("UserType", "Diner");
        startActivity(intent);
    }

    private void launchDelivery(){
        //need to send user type so that the user can be located in the database
        //Bundle dinerBundle = new Bundle();
        Intent intent = new Intent(DinerMainMenu.this, DeliveryMapActivity.class);
        //dinerBundle.putSerializable("User", user);
        //intent.putExtras(dinerBundle);
        //intent.putExtra("UserType", "Diner");
        startActivity(intent);
    }

    /**
     * simulate a completed order
     */
    private void simulateCompletedOrder() {
        //After the order entity and repository are in place, this will be handled from there
        //since they aren't available yet, instead produce a notification that will appear
        //to go to a restaurant

        Restaurants<Restaurant> restaurantsRepository = new Restaurants<>(DinerMainMenu.this);
        final NotificationMessages<NotificationMessage> notificationsRepository = new NotificationMessages<>(DinerMainMenu.this);

        restaurantsRepository
            .find("id = '26804931-17e3-403a-ab75-a43e96e86814'")
            .addOnCompleteListener(new OnCompleteListener<TaskResult<Restaurant>>() {
                @Override
                public void onComplete(@NonNull Task<TaskResult<Restaurant>> task) {
                    Restaurant testRestaurant = task.getResult().getResults().get(0);

                    notificationsRepository.sendNotification(NotificationConstants.Action.ADDED, testRestaurant);
                }
            });
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
}//end Activity
