package com.gmail.dleemcewen.tandemfieri;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.gmail.dleemcewen.tandemfieri.Entities.NotificationMessage;
import com.gmail.dleemcewen.tandemfieri.Entities.Restaurant;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.Logging.LogWriter;
import com.gmail.dleemcewen.tandemfieri.Repositories.Restaurants;
import com.gmail.dleemcewen.tandemfieri.Repositories.Users;
import com.gmail.dleemcewen.tandemfieri.Tasks.TaskResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.logging.Level;

public class DinerMainMenu extends AppCompatActivity {
    User user;
    private Restaurants<Restaurant> restaurantsRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diner_main_menu);

        Bundle bundle = new Bundle();
        bundle = this.getIntent().getExtras();
        user = (User) bundle.getSerializable("User");

        restaurantsRepository = new Restaurants<>(DinerMainMenu.this);

        LogWriter.log(getApplicationContext(), Level.INFO, "The user is " + user.getEmail());
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
                launchMap();
                return true;
            case R.id.sendSimulatedNotification:
                //TODO: remove this after ordering and payment processing are in place
                //this is just for testing and demo purposes
                simulateCompletedOrder();
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
        //Bundle dinerBundle = new Bundle();
        Intent intent = new Intent(DinerMainMenu.this, DinerMapActivity.class);
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
        restaurantsRepository
            .find("id = 26804931-17e3-403a-ab75-a43e96e86814")
            .addOnCompleteListener(new OnCompleteListener<TaskResult<Restaurant>>() {
                @Override
                public void onComplete(@NonNull Task<TaskResult<Restaurant>> task) {
                    Restaurant testRestaurant = task.getResult().getResults().get(0);

                    restaurantsRepository
                        .update(testRestaurant);
                }
            });
    }
}//end Activity
