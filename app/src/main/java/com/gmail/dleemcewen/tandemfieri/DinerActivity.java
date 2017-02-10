package com.gmail.dleemcewen.tandemfieri;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.gmail.dleemcewen.tandemfieri.Entities.User;

public class DinerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diner);

        User user = new User();
        Bundle bundle = new Bundle();
        bundle = this.getIntent().getExtras();
        user = (User) bundle.getSerializable("User");

        Toast.makeText(getApplicationContext(),"The user is" + user.getEmail(), Toast.LENGTH_LONG).show();
    }
}
