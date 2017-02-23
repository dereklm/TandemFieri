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
import com.gmail.dleemcewen.tandemfieri.Logging.LogWriter;
import com.gmail.dleemcewen.tandemfieri.Repositories.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.google.firebase.database.ValueEventListener;

import java.util.logging.Level;

import static com.gmail.dleemcewen.tandemfieri.Validator.Validator.isValid;

public class EditAccountActivity extends AppCompatActivity {

    private User currentUser;
    private User changedUser;
    private String type = "";
    private String uid = "";
    private boolean emailIsDuplicated, userIsValid;


    private DatabaseReference mDatabase;
    private FirebaseUser fireuser;

    private EditText firstName, lastName, address, city, state, zip, phoneNumber, email;
    private Button saveButton, cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);

        currentUser = new User();
        Bundle bundle = this.getIntent().getExtras();
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
            LogWriter.log(getApplicationContext(), Level.INFO, "The user is " + currentUser.getEmail());
        }
        else{
            LogWriter.log(getApplicationContext(), Level.WARNING, "The user is null");
            finish();
        }
        LogWriter.log(getApplicationContext(), Level.INFO, "The user key is " + currentUser.getKey());
        LogWriter.log(getApplicationContext(), Level.INFO, "The user id is " + uid);

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

        //set text in fields using user's current information
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

        //saves information to the database once validated
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //collect information
                getChangedUserInformation();
                userIsValid = validInput();
                //emailIsDuplicated = false;
                //Is email changed?
                if(!currentUser.getEmail().equals(changedUser.getEmail())){
                    //Toast.makeText(getApplicationContext(),"new Email has been entered" , Toast.LENGTH_LONG).show();
                    //yes - is email valid?

                        //yes - check for duplicate email
                        //Toast.makeText(getApplicationContext(),"new email is valid." , Toast.LENGTH_LONG).show();
                        DatabaseReference rodb = FirebaseDatabase.getInstance().getReference().child("User");

                        VEListener listener = new VEListener();
                        rodb.addListenerForSingleValueEvent(listener);
                    }


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
        changedUser.setAuthUserID(currentUser.getAuthUserID());
    }

    public boolean validInput(){
        boolean result = false; //return value

        boolean firstNameValid = isValid(firstName, FormConstants.REG_EX_FIRSTNAME, FormConstants.ERROR_TAG_FIRSTNAME);
        boolean lastNameValid = isValid(lastName, FormConstants.REG_EX_LASTNAME, FormConstants.ERROR_TAG_LASTNAME);
        boolean addressValid = isValid(address, FormConstants.REG_EX_ADDRESS, FormConstants.ERROR_TAG_ADDRESS);
        boolean cityValid = isValid(city, FormConstants.REG_EX_CITY, FormConstants.ERROR_TAG_CITY);
        boolean stateValid = isValid(state, FormConstants.REG_EX_STATE, FormConstants.ERROR_TAG_STATE);
        boolean emailValid = isValid(email, FormConstants.REG_EX_EMAIL, FormConstants.ERROR_TAG_EMAIL);
        boolean phoneNumberValid = isValid(phoneNumber, FormConstants.REG_EX_PHONE, FormConstants.ERROR_TAG_PHONE);
        boolean zipValid = isValid(zip, FormConstants.REG_EX_ZIP, FormConstants.ERROR_TAG_ZIP);

        if (    firstNameValid      &&
                lastNameValid       &&
                addressValid        &&
                cityValid           &&
                stateValid          &&
                zipValid            &&
                phoneNumberValid    &&
                emailValid) {

            result = true;
        }
        return result;
    }


    public class VEListener implements ValueEventListener {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            emailIsDuplicated = false;
            String msg = "Email has been changed.";
            DataSnapshot ro = (DataSnapshot) dataSnapshot.child("Restaurant");
            for (DataSnapshot ps : ro.getChildren()) {
                User u = ps.getValue(User.class);
                //Toast.makeText(getApplicationContext(), u.getEmail(), Toast.LENGTH_SHORT).show();
                if (u.getEmail().equals(changedUser.getEmail())) {
                    Toast.makeText(getApplicationContext(), "In u.getEmail == changeduser email", Toast.LENGTH_SHORT).show();
                    emailIsDuplicated = true;
                    msg = "Invalid email.";
                }
            }
            DataSnapshot diner = (DataSnapshot) dataSnapshot.child("Diner");
            for (DataSnapshot ps : diner.getChildren()) {
                User u = ps.getValue(User.class);
                //Toast.makeText(getApplicationContext(), u.getEmail(), Toast.LENGTH_SHORT).show();
                if (u.getEmail().equals(changedUser.getEmail())) {
                    Toast.makeText(getApplicationContext(), "In u.getEmail == changeduser email", Toast.LENGTH_SHORT).show();
                    emailIsDuplicated = true;
                    msg = "Invalid email.";
                }
            }
            DataSnapshot driver = (DataSnapshot) dataSnapshot.child("Driver");
            for (DataSnapshot ps : driver.getChildren()) {
                User u = ps.getValue(User.class);
                Toast.makeText(getApplicationContext(), u.getEmail(), Toast.LENGTH_SHORT).show();
                if (u.getEmail().equals(changedUser.getEmail())) {
                    //Toast.makeText(getApplicationContext(), "In u.getEmail == changeduser email", Toast.LENGTH_SHORT).show();
                    emailIsDuplicated = true;
                    msg = "Invalid email.";
                }
            }
            //is all info valid?
            if(userIsValid && !emailIsDuplicated){
                //Toast.makeText(getApplicationContext(),"About to save" , Toast.LENGTH_LONG).show();
                saveUserToDatabase();
                Toast.makeText(getApplicationContext(),msg , Toast.LENGTH_LONG).show();
                //send new user info to the proper menu activity
                Bundle bundle1 = new Bundle();
                Intent intent = null;

                if (type.equals("Restaurant"))
                    intent = new Intent(EditAccountActivity.this, RestaurantMainMenu.class);
                else if (type.equals("Diner"))
                    intent = new Intent(EditAccountActivity.this, DinerMainMenu.class);
                else if (type.equals("Driver"))
                    intent = new Intent(EditAccountActivity.this, DriverMainMenu.class);

                bundle1.putSerializable("User", changedUser);
                intent.putExtras(bundle1);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }else{
                //something is not valid
                if(emailIsDuplicated)
                    Toast.makeText(getApplicationContext(),msg , Toast.LENGTH_LONG).show();
            }

        }

        @Override
        public void onCancelled(DatabaseError firebaseError) {
            //System.out.println("The read failed: " + firebaseError.getMessage());
        }

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
                            LogWriter.log(getApplicationContext(), Level.INFO, "User email address updated.");
                        }
                    }
                });
    }

}//end activity
