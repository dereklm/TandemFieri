package com.gmail.dleemcewen.tandemfieri;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditAccountActivity extends AppCompatActivity {

    private User currentUser;
    private User changedUser;
    private String type = "";
    private String uid = "";

    private DatabaseReference mDatabase;
    private FirebaseUser fireuser;

    private EditText firstName, lastName, address, city, state, zip, phoneNumber, email;
    private Button saveButton, cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);

        currentUser = new User();
        Bundle bundle = new Bundle();
        bundle = this.getIntent().getExtras();
        currentUser = (User) bundle.getSerializable("User");
        type = this.getIntent().getStringExtra("UserType");


        /*For the code I currently have, the key stored in the User object is not the same as the id in the database.
        Therefore I must use "uid = FirebaseAuth.getInstance().getCurrentUser().getUid()" to access the correct
        user in the database.*/
        fireuser = FirebaseAuth.getInstance().getCurrentUser();
        if (fireuser != null) {
            // User is signed in
            uid = fireuser.getUid();
        } else {
            // No user is signed in
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("Exit me", true);
            startActivity(intent);
            finish();
        }


        //reference in database to the current user
        mDatabase = FirebaseDatabase.getInstance().getReference().child("User").child(type).child(uid);

        if(currentUser != null) {
            Toast.makeText(getApplicationContext(), "The user is " + currentUser.getEmail(), Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getApplicationContext(), "The user is null" , Toast.LENGTH_LONG).show();
            finish();
        }
        //Toast.makeText(getApplicationContext(),"The user key is " + currentUser.getKey(), Toast.LENGTH_LONG).show();
        //Toast.makeText(getApplicationContext(),"The user id is " + uid, Toast.LENGTH_LONG).show();

        //get handles to view
        firstName = (EditText) findViewById(R.id.firstName);
        lastName = (EditText) findViewById(R.id.lastName);
        address = (EditText) findViewById(R.id.address);
        city = (EditText) findViewById(R.id.city);
        state = (EditText) findViewById(R.id.state);
        zip = (EditText) findViewById(R.id.zip);
        phoneNumber = (EditText) findViewById(R.id.phone);
        email = (EditText) findViewById(R.id.email);
        saveButton = (Button) findViewById(R.id.save_Button);
        cancelButton = (Button) findViewById(R.id.cancel_Button);

        //set text in fields
        firstName.setText(currentUser.getFirstName());
        lastName.setText(currentUser.getLastName());
        address.setText(currentUser.getAddress());
        city.setText(currentUser.getCity());
        state.setText(currentUser.getState());
        zip.setText(currentUser.getZip());
        phoneNumber.setText(currentUser.getPhoneNumber());
        email.setText(currentUser.getEmail());

        //program button listeners
        //cancels the page and returns to previous page
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //saves information to the database
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getChangedUserInformation();
                saveUserToDatabase();

                //send new user info to the proper menu activity
                Bundle bundle = new Bundle();
                Intent intent = null;

                if(type.equals("Restaurant"))
                    intent = new Intent(EditAccountActivity.this, RestaurantMainMenu.class);
                else if(type.equals("Diner"))
                    intent = new Intent(EditAccountActivity.this, DinerMainMenu.class);
                else if(type.equals("Driver"))
                    intent = new Intent(EditAccountActivity.this, DriverMainMenu.class);

                bundle.putSerializable("User", changedUser);
                intent.putExtras(bundle);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }//end onCreate

    public void getChangedUserInformation(){
        changedUser = new User();
        changedUser.setFirstName(firstName.getText().toString());
        changedUser.setLastName(lastName.getText().toString());
        changedUser.setAddress(address.getText().toString());
        changedUser.setCity(city.getText().toString());
        changedUser.setState(state.getText().toString());
        changedUser.setZip(zip.getText().toString());
        changedUser.setPhoneNumber(phoneNumber.getText().toString());
        changedUser.setEmail(email.getText().toString());
    }

    //edits the current user's info in the database with the new User information
    public void saveUserToDatabase(){
        mDatabase.child("firstName").setValue(changedUser.getFirstName());
        mDatabase.child("lastName").setValue(changedUser.getLastName());
        mDatabase.child("address").setValue(changedUser.getAddress());
        mDatabase.child("city").setValue(changedUser.getCity());
        mDatabase.child("state").setValue(changedUser.getState());
        mDatabase.child("zip").setValue(changedUser.getZip());
        mDatabase.child("phoneNumber").setValue(changedUser.getPhoneNumber());
        mDatabase.child("email").setValue(changedUser.getEmail());

        //set the new email in the authentication table
        fireuser.updateEmail(changedUser.getEmail())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                           // Log.d(TAG, "User email address updated.");
                        }
                    }
                });
    }

}//end activity
