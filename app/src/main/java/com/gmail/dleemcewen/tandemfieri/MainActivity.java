package com.gmail.dleemcewen.tandemfieri;

import android.content.Intent;
import android.content.res.Resources;
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

import static android.widget.Toast.makeText;

public class MainActivity extends AppCompatActivity {

    public TextView createAccount;
    public EditText email, password;
    public Button signInButton;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authenticatorListener;
    private DatabaseReference dBase;
    private User user;
    private Resources resources;

    private boolean verifiedEmailNotRequiredForLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resources = getResources();
        verifiedEmailNotRequiredForLogin = resources.getBoolean(R.bool.verified_email_not_required_for_login);
        //this if statement is used when the user clicks the sign out option from the drop down menu
        //it closed all open activities and then the main activity.
        if( getIntent().getBooleanExtra("Exit me", false)){
            finish();
            return; // add this to prevent from doing unnecessary stuffs
        }

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

        createAccount.setOnClickListener(new View.OnClickListener() {

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
                            if (verifiedEmailNotRequiredForLogin || task.getResult().getUser().isEmailVerified()) {
                                Toast.makeText(getApplicationContext(), task.getResult().getUser().getEmail() + " was successfully signed in", Toast.LENGTH_LONG)
                                      .show();

                                dBase.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        navigateToMenu(dataSnapshot);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            } else {
                                Toast.makeText(getApplicationContext(), "That user is not verified, check email for verification link.", Toast.LENGTH_LONG)
                                        .show();
                            }
                        } else {
                            Toast
                                    .makeText(getApplicationContext(), "Sign in was not successful. Check login details please.", Toast.LENGTH_LONG)
                                    .show();
                        }//end if task.successful
                    }//end onComplete
                });//end sign in


            }//end on click
        });//end sign in button
    }//end onCreate

    public void navigateToMenu(DataSnapshot dataSnapshot) {

        Bundle bundle = new Bundle();
                           
        User diner = dataSnapshot.child("Diner").child(mAuth.getCurrentUser().getUid()).getValue(User.class);
        User driver = dataSnapshot.child("Driver").child(mAuth.getCurrentUser().getUid()).getValue(User.class);
        User restaurantOwner = dataSnapshot.child("Restaurant").child(mAuth.getCurrentUser().getUid()).getValue(User.class);

        Intent intent = null;

        if(diner != null){
            intent = new Intent(MainActivity.this, DinerMainMenu.class);
            bundle.putSerializable("User", diner);
        }else if(driver != null){
            intent = new Intent(MainActivity.this, DriverMainMenu.class);
            bundle.putSerializable("User", driver);
        }else if(restaurantOwner != null){
            intent = new Intent(MainActivity.this, RestaurantMainMenu.class);
            bundle.putSerializable("User", restaurantOwner);
        }

        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void clear(){
        email.setText("");
        password.setText("");
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authenticatorListener);
    }

    @Override
    protected void onStop() {
        mAuth.removeAuthStateListener(authenticatorListener);
        super.onStop();
    }

    @Override
    protected void onPause(){
        mAuth.removeAuthStateListener(authenticatorListener);
        clear();
        super.onPause();

    }

    @Override
    protected void onResume(){
        super.onResume();
        mAuth.addAuthStateListener(authenticatorListener);
    }
}//end activity


    /* example finding user and navigating to appropriate main menu with repository */
    /*private void signInTest()
    {
        // this would be a variable accessible from the entire activity
        final Users<User> usersRepo = new Users<User>();

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

                            final String uid = task.getResult().getUser().getUid();

                            usersRepo.find(Arrays.asList("Diner"), uid, new QueryCompleteListener<User>() {
                                Bundle bundle = new Bundle();
                                Intent intent = null;

                                @Override
                                public void onQueryComplete(ArrayList<User> entities) {
                                    if (entities.isEmpty()) {
                                        usersRepo.find(Arrays.asList("Driver"), uid, new QueryCompleteListener<User>() {
                                            @Override
                                            public void onQueryComplete(ArrayList<User> entities) {
                                                if (entities.isEmpty()) {
                                                    usersRepo.find(Arrays.asList("Restaurant"), uid, new QueryCompleteListener<User>() {
                                                        @Override
                                                        public void onQueryComplete(ArrayList<User> entities) {
                                                            if (entities.isEmpty()) {
                                                                //no user found
                                                                Toast
                                                                        .makeText(getApplicationContext(), "Invalid user.  The authorities have been notified.", Toast.LENGTH_LONG)
                                                                        .show();

                                                            } else {
                                                                //found restaurant
                                                                intent = new Intent(MainActivity.this, RestaurantMainMenu.class);
                                                                bundle.putSerializable("User", entities.get(0));
                                                                intent.putExtras(bundle);
                                                                startActivity(intent);
                                                            } //end restaurant find
                                                        }
                                                    });
                                                } else {
                                                    //found driver
                                                    intent = new Intent(MainActivity.this, DriverMainMenu.class);
                                                    bundle.putSerializable("User", entities.get(0));
                                                    intent.putExtras(bundle);
                                                    startActivity(intent);

                                                }  //end driver find
                                            }
                                        });
                                    } else {
                                        //found diner!
                                        intent = new Intent(MainActivity.this, DinerMainMenu.class);
                                        bundle.putSerializable("User", entities.get(0));
                                        intent.putExtras(bundle);
                                        startActivity(intent);
                                    }   // end diner find
                                }
                            });

                        } else {
                            Toast
                                    .makeText(getApplicationContext(), "Sign in was not successful", Toast.LENGTH_LONG)
                                    .show();
                        }//end if task.successful
                    }//end onComplete
                });//end sign in


            }//end on click
        });//end sign in button
 */
