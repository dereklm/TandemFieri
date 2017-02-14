package com.gmail.dleemcewen.tandemfieri;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.shawnlin.numberpicker.NumberPicker;

public class RestaurantMapActivity extends Activity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MapFragment mapFragment;
    private NumberPicker numberPicker;
    private Button update;

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

        LatLng latLng = new LatLng(35.664806, -97.557015);
        CameraPosition camera = new CameraPosition.Builder()
                .target(latLng)
                .zoom(14)
                .build();
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Test"));

        mMap.addCircle(new CircleOptions()
            .center(latLng)
            .radius(2000)
            .strokeColor(Color.BLUE));

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(camera));

        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    private void bindEventHandlers() {
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //todo
            }
        });
    }

    private void initialize() {
        mapFragment.getMapAsync(this);
    }

    private void findControlReferences() {
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        numberPicker = (NumberPicker) findViewById(R.id.radius);
        update = (Button) findViewById(R.id.update);
    }
}