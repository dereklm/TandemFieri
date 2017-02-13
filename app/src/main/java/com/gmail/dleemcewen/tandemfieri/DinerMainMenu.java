package com.gmail.dleemcewen.tandemfieri;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.google.firebase.auth.FirebaseAuth;

public class DinerMainMenu extends AppCompatActivity {

    Button outButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diner_main_menu);

        User user = new User();
        Bundle bundle = new Bundle();
        bundle = this.getIntent().getExtras();
        user = (User) bundle.getSerializable("User");

        Toast.makeText(getApplicationContext(),"The user is " + user.getEmail(), Toast.LENGTH_LONG).show();

        outButton = (Button)findViewById(R.id.sign_out_button);

        outButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                finish();
            }
        });
    }//end onCreate
}//end Activity
