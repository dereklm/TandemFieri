package com.gmail.dleemcewen.tandemfieri;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.gmail.dleemcewen.tandemfieri.Entities.Restaurant;
import com.gmail.dleemcewen.tandemfieri.Events.ActivityEvent;
import com.gmail.dleemcewen.tandemfieri.Json.AddressGeocode.AddressGeocode;
import com.gmail.dleemcewen.tandemfieri.Json.AddressGeocode.Location;
import com.gmail.dleemcewen.tandemfieri.Logging.LogWriter;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.shawnlin.numberpicker.NumberPicker;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.logging.Level;

import cz.msebera.android.httpclient.Header;

public class RestaurantMapActivity extends Activity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng latLng;
    private Circle circle;
    private MapFragment mapFragment;

    private NumberPicker numberPicker;
    private Button update;
    private Restaurant restaurant;

    private AsyncHttpClient client;

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
                        .child(restaurant.getRestaurantKey());

                mDatabase.child("deliveryRadius")
                        .setValue(restaurant.getDeliveryRadius()
                                ,new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError error, DatabaseReference reference) {
                                       if (error != null) {
                                           LogWriter.log(getApplicationContext(), Level.SEVERE, "Unable to save change, try again later!");
                                       } else {
                                           LogWriter.log(getApplicationContext(), Level.SEVERE, "Delivery area saved!");
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
        String url;

        Bundle bundle = this.getIntent().getExtras();
        restaurant = (Restaurant) bundle.getSerializable("restaurant");

        url = "address="
                + restaurant.getStreet()
                + ","
                + restaurant.getCity()
                + ","
                + restaurant.getState()
                + ","
                + restaurant.getZipcode()
                + "&" + getString(R.string.google_api_key);

        client = new AsyncHttpClient();
        client.get(this
                , getString(R.string.googleDecoderBaseURL) + url
                , new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int status, Header[] headers, JSONObject resp) {
                        String json = resp.toString();

                        GsonBuilder builder = new GsonBuilder();
                        Gson gson = builder.create();

                        AddressGeocode address = gson.fromJson(json, AddressGeocode.class);

                        if (!address.status.equals("OK")) {
                            latLng = null;
                        } else {
                            Location location = address.results.get(0).geometry.location;
                            latLng = new LatLng(location.lat, location.lng);
                        }

                        asyncReqComplete();
                    }

                    @Override
                    public void onFailure(int status, Header[] headers, String res, Throwable t) {
                        latLng = null;
                        asyncReqComplete();
                    }
                });
    }

    private void findControlReferences() {
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        numberPicker = (NumberPicker) findViewById(R.id.radius);
        update = (Button) findViewById(R.id.update);
    }

    private void asyncReqComplete() {
        if (latLng == null) {
            LogWriter.log(getApplicationContext(), Level.SEVERE, "Unable to locate address.");
        } else {
            numberPicker.setValue(restaurant.getDeliveryRadius());
            mapFragment.getMapAsync(RestaurantMapActivity.this);
        }
    }
}