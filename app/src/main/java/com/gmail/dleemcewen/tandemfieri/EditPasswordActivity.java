package com.gmail.dleemcewen.tandemfieri;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.Logging.LogWriter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.logging.Level;

public class EditPasswordActivity extends AppCompatActivity {

    private User currentUser;
    private String type = "";
    private String uid = "";

    private FirebaseUser fireuser;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;

    private EditText password, confirmPswd, oldPassword;
    private BootstrapButton saveButton, cancelButton;

    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);

        Bundle bundle = this.getIntent().getExtras();
        currentUser = (User) bundle.getSerializable("User");
        type = this.getIntent().getStringExtra("UserType");

        /*For the code I currently have, the key stored in the User object is not the same as the id in the database.
        Therefore I must use "uid = FirebaseAuth.getInstance().getCurrentUser().getUid()" to access the correct
        user in the database.*/
        mAuth = FirebaseAuth.getInstance();
        fireuser = mAuth.getCurrentUser();
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

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    //Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    //Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(EditPasswordActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("Exit me", true);
                    startActivity(intent);
                    finish();
                }
            }
        };

        //Toast.makeText(getApplicationContext(),"The user is " + currentUser.getEmail(), Toast.LENGTH_LONG).show();

        //get handles to the view
        oldPassword = (EditText)findViewById(R.id.old_password);
        password = (EditText)findViewById(R.id.password);
        confirmPswd = (EditText)findViewById(R.id.confirmPassword);
        saveButton = (BootstrapButton) findViewById(R.id.saveButton);
        cancelButton = (BootstrapButton)findViewById(R.id.cancelButton);

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
                if (SystemClock.elapsedRealtime() - mLastClickTime < 2000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                if (isValidPassword(oldPassword.getText().toString(),
                        password.getText().toString(),
                        confirmPswd.getText().toString())) {

                    //Verify that the oldpassword is correct by logging in
                    mAuth
                        .signInWithEmailAndPassword(fireuser.getEmail(), oldPassword.getText().toString())
                        .addOnCompleteListener(EditPasswordActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Password changed.", Toast.LENGTH_SHORT).show();

                                    savePassword(password.getText().toString());
                                    finish();
                                } else {
                                    String msg = "The current password did not match the existing password.  The password was not changed.";
                                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                                    resetPasswordEntries();
                                }
                            }
                        });
                } else {
                    resetPasswordEntries();
                }//end onClick
            }
        });

    }//end onCreate

    private void resetPasswordEntries() {
        oldPassword.setText("");
        password.setText("");
        confirmPswd.setText("");

        setFocusToCurrentPassword();
    }

    private void setFocusToCurrentPassword() {
        oldPassword.requestFocus();
    }

    //validates the password
    public boolean isValidPassword(String oldPassword, String newPassword, String confirmPassword){

        boolean result = true;
        String msg = "";

        if (!oldPassword.matches(".*\\w.*")) {
            msg = "Your current password is required.";
            result = false;
        } else if (!newPassword.equals(confirmPassword)) {
            msg = "Your password confirmation must be the same as your new password.";
            result = false;
        }
        else if (!newPassword.matches(".*\\w.*")) {
            msg = "Please use characters and not only whitespace.";
            result = false;
        }
        else if (newPassword.length() < 6) {
            msg = "Please enter a password with 6 or more characters.";
            result = false;
        }
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        return result;
    }

    //updates password in authentication
    public void savePassword(String newPassword){

        fireuser.updatePassword(newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            LogWriter.log(getApplicationContext(), Level.INFO, "User password updated.");
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}//end activity
