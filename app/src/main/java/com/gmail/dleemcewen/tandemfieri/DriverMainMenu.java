package com.gmail.dleemcewen.tandemfieri;

import android.os.Bundle;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.google.firebase.auth.FirebaseAuth;

public class DriverMainMenu extends AppCompatActivity {

    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_main_menu);

        User user = new User();

        Bundle bundle = new Bundle();
        bundle = this.getIntent().getExtras();
        user = (User) bundle.getSerializable("User");

        Toast.makeText(getApplicationContext(),"The user is " + user.getEmail(), Toast.LENGTH_LONG).show();

    }//end onCreate

    //create menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.driver_menu, menu);
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
        Bundle driverBundle = new Bundle();
        Intent intent = new Intent(DriverMainMenu.this, EditAccountActivity.class);
        driverBundle.putSerializable("User", user);
        intent.putExtras(driverBundle);
        intent.putExtra("UserType", "Driver");
        startActivity(intent);
    }

    //called when user selects edit password from the drop down menu
    private void editPassword(){
        //need to send user type so that the user can be located in the database
        Bundle driverBundle = new Bundle();
        Intent intent = new Intent(DriverMainMenu.this, EditPasswordActivity.class);
        driverBundle.putSerializable("User", user);
        intent.putExtras(driverBundle);
        intent.putExtra("UserType", "Driver");
        startActivity(intent);
    }
}//end Activity
