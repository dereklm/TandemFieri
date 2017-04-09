package com.gmail.dleemcewen.tandemfieri;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.gmail.dleemcewen.tandemfieri.Entities.Restaurant;
import com.gmail.dleemcewen.tandemfieri.Events.ActivityEvent;
import com.gmail.dleemcewen.tandemfieri.Utility.Conversions;
import com.gmail.dleemcewen.tandemfieri.Utility.MapUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shawnlin.numberpicker.NumberPicker;

import org.greenrobot.eventbus.EventBus;

public class RestaurantMapActivity extends Activity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {

    private GoogleMap mMap;
    private LatLng latLng;
    private Circle circle;
    private MapFragment mapFragment;

    private NumberPicker numberPicker;
    private BootstrapButton update;
    private Restaurant restaurant;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_map);

        findControlReferences();
        initialize();
        bindEventHandlers();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        googleMap.setOnMapLoadedCallback(this);
    }

    @Override
    public void onMapLoaded() {
        MapUtil.moveCamera(mMap
                , latLng
                , Conversions.milesToMeters(restaurant.getDeliveryRadius())
                , getBaseContext().getResources().getInteger(R.integer.defaultMapPadding));

        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(restaurant.getName()));

        circle = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(Conversions.milesToMeters(restaurant.getDeliveryRadius()))
                .strokeColor(Color.BLUE));

        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    private void bindEventHandlers() {
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabase = FirebaseDatabase.getInstance().getReference()
                        .child("Restaurant")
                        .child(restaurant.getId());

                mDatabase.child("deliveryRadius")
                        .setValue(restaurant.getDeliveryRadius()
                                ,new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError error, DatabaseReference reference) {
                                       if (error != null) {
                                           Toast.makeText(getApplicationContext(), "Unable to save change, try again later!", Toast.LENGTH_LONG).show();
                                       } else {
                                           Toast.makeText(getApplicationContext(), "Delivery area saved!", Toast.LENGTH_LONG).show();
                                           EventBus.getDefault()
                                                   .post(new ActivityEvent(ActivityEvent.Result.REFRESH_RESTAURANT_LIST));
                                       }
                                   }
                                });
            }
        });

        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if (mMap != null) {
                    restaurant.setDeliveryRadius(newVal);
                    circle.setRadius(Conversions.milesToMeters(newVal));
                    MapUtil.moveCamera(mMap
                            , latLng
                            , Conversions.milesToMeters(restaurant.getDeliveryRadius())
                            , getBaseContext().getResources().getInteger(R.integer.defaultMapPadding));
                } else {
                    picker.setValue(restaurant.getDeliveryRadius());
                }
            }
        });
    }

    private void initialize() {
        Bundle bundle = this.getIntent().getExtras();
        restaurant = (Restaurant) bundle.getSerializable("restaurant");

        latLng = new LatLng(restaurant.getLatitude(), restaurant.getLongitude());

        numberPicker.setValue(restaurant.getDeliveryRadius());
        mapFragment.getMapAsync(RestaurantMapActivity.this);
    }

    private void findControlReferences() {
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        numberPicker = (NumberPicker) findViewById(R.id.radius);
        update = (BootstrapButton) findViewById(R.id.update);
    }
}