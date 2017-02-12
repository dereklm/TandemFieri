package com.gmail.dleemcewen.tandemfieri;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.google.firebase.auth.FirebaseAuth;

public class RestaurantMainMenu extends AppCompatActivity {

    Button outButton;
    Button manageRestaurants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_main_menu);


        Bundle bundle = new Bundle();
        bundle = this.getIntent().getExtras();
        final User user = (User) bundle.getSerializable("User");

        Toast.makeText(getApplicationContext(),"The user is " + user.getEmail(), Toast.LENGTH_LONG).show();


        outButton = (Button)findViewById(R.id.sign_out_button);

        outButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                finish();
            }
        });


        //TODO: remove this, just for testing
        manageRestaurants = (Button)findViewById(R.id.manageRestaurants);
        manageRestaurants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: undo this!  just for testing....
                Bundle bundle = new Bundle();
                Intent intent = new Intent(RestaurantMainMenu.this, ManageRestaurants.class);
                bundle.putSerializable("User", user);
                intent.putExtras(bundle);
                startActivity(intent);

            }
        });
    }
}
