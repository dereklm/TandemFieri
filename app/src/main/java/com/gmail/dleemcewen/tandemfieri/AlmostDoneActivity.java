package com.gmail.dleemcewen.tandemfieri;

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
    FirebaseAuth user = FirebaseAuth.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_almost_done);

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
                user.createUserWithEmailAndPassword("apettitt@uco.edu", password.toString());
            }
        });

    }
}
