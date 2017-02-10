package com.gmail.dleemcewen.tandemfieri;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    public TextView createAccount;
    public EditText email, password;
    public Button signInButton;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authenticatorListener;
    private DatabaseReference dBase;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createAccount = (TextView) findViewById(R.id.createAccount);
        signInButton = (Button) findViewById(R.id.signInButton);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);

        dBase = FirebaseDatabase.getInstance().getReference().child("User");

        user = new User();
        mAuth = FirebaseAuth.getInstance();
        authenticatorListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("AUTH", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("AUTH", "onAuthStateChanged:signed_out");
                }
            }
        };

        createAccount.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CreateAccountActivity.class);
                startActivity(intent);
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast
                                    .makeText(getApplicationContext(), task.getResult().getUser().getEmail() +" was successfully signed in", Toast.LENGTH_LONG)
                                    .show();

                            //TODO:  this needs to be moved to the restaurant owner main menu when that is ready
                            // right now it is just here for testing the CreateRestaurant activity
                            Intent intent = new Intent(MainActivity.this, CreateRestaurant.class);
                            intent.putExtra("ownerId", task.getResult().getUser().getUid());
                            dBase.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    user = dataSnapshot.child("Diner").child(mAuth.getCurrentUser().getUid()).getValue(User.class);
                                }



                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                            //Toast.makeText(getApplicationContext(),"Does this work" + user.getEmail(),Toast.LENGTH_LONG).show();
                            Intent diner = new Intent(MainActivity.this, DinerActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("User", user);
                            diner.putExtras(bundle);
                            startActivity(diner);
                        } else {
                            Toast
                                    .makeText(getApplicationContext(), "Sign in was not successful", Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });


            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authenticatorListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(authenticatorListener);
    }
}
