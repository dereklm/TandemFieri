package com.gmail.dleemcewen.tandemfieri;

import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static android.R.id.edit;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static com.gmail.dleemcewen.tandemfieri.R.id.phone;

public class AlmostDoneActivity extends AppCompatActivity{

    public String firstName, lastName, address, city, state, zip, phoneNumber, email;
    public Button createButton;
    public EditText username, password, confirmPassword;
    public User newUser;
    private DatabaseReference mDatabase;
    public RadioGroup radioFilter;
    public RadioButton radioDining, radioRestaurant, radioDriver;
    FirebaseAuth user = FirebaseAuth.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_almost_done);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        radioFilter = (RadioGroup) findViewById(R.id.radioFilter);

        radioDining = (RadioButton) findViewById(R.id.diningRadio);
        radioRestaurant = (RadioButton) findViewById(R.id.restaurantRadio);
        radioDriver = (RadioButton) findViewById(R.id.driverRadio);

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        confirmPassword = (EditText) findViewById(R.id.confrimPassword);

        createButton = (Button) findViewById(R.id.createButton);

        firstName = getIntent().getStringExtra("firstName");
        lastName = getIntent().getStringExtra("lastName");
        address = getIntent().getStringExtra("address");
        city = getIntent().getStringExtra("city");
        state = getIntent().getStringExtra("state");
        zip = getIntent().getStringExtra("zip");
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        email = getIntent().getStringExtra("email");


        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createUser();
            }

        });

    }


    public void createUser (){
        user.createUserWithEmailAndPassword(email, password.toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    FirebaseUser user = task.getResult().getUser();
                    newUser = new User(firstName, lastName, address, city, state, zip, phoneNumber, email, username.getText().toString(), user.getUid());
                    if (radioDining.isChecked() == true) {
                        mDatabase.child("User").child("Diner").push().setValue(newUser);
                    } else if (radioRestaurant.isChecked() == true){
                        mDatabase.child("User").child("Restaurant").push().setValue(newUser);
                    } else if (radioDriver.isChecked() == true){
                        mDatabase.child("User").child("Driver").push().setValue(newUser);
                    }
                }
            }
        });
    }
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user.createUserWithEmailAndPassword("apettitt@uco.edu", password.toString());
            }
        });

    }
}
