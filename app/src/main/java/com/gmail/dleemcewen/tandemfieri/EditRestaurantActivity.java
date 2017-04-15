package com.gmail.dleemcewen.tandemfieri;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.gmail.dleemcewen.tandemfieri.Constants.AddressConstants;
import com.gmail.dleemcewen.tandemfieri.Entities.Restaurant;
import com.gmail.dleemcewen.tandemfieri.Formatters.StringFormatter;
import com.gmail.dleemcewen.tandemfieri.Logging.LogWriter;
import com.gmail.dleemcewen.tandemfieri.Repositories.Restaurants;
import com.gmail.dleemcewen.tandemfieri.Tasks.TaskResult;
import com.gmail.dleemcewen.tandemfieri.Utility.FetchAddressIntentService;
import com.gmail.dleemcewen.tandemfieri.Utility.MapUtil;
import com.gmail.dleemcewen.tandemfieri.Validator.Validator;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

public class EditRestaurantActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ActivityCompat.OnRequestPermissionsResultCallback{
    private Resources resources;
    private Restaurants<Restaurant> restaurantsRepository;
    private Restaurant restaurant;
    private TextView title;
    private TextView restaurantTypeTitle;
    private TextView address;
    private TextView delivery;
    private EditText restaurantName;
    private EditText street;
    private EditText city;
    private Spinner states;
    private Spinner restaurantTypes;
    private EditText zipCode;
    private EditText deliveryCharge;
    private String state;
    private BootstrapButton deliveryHours;
    private BootstrapButton updateRestaurant;
    private BootstrapButton cancelUpdateRestaurant;
    private String restaurantType;

    private BootstrapButton myLocation;
    protected Location location;
    private AddressResultReceiver mResultReceiver;
    private GoogleApiClient googleApiClient;
    private boolean verifiedAddr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_restaurant);

        initialize();
        findControlReferences();
        bindEventHandlers();
        retrieveData();
        finalizeLayout();
    }

    /**
     * initialize all necessary variables
     */
    private void initialize() {
        resources = this.getResources();
        restaurantsRepository = new Restaurants<>(this);
        state = "";
        restaurantType = "";

        Bundle bundle = getIntent().getExtras();
        restaurant = (Restaurant)bundle.getSerializable("Restaurant");

        LogWriter.log(getApplicationContext(), Level.FINE, "The restaurant key is " + restaurant.getKey());
    }

    /**
     * find all control references
     */
    private void findControlReferences() {
        title = (TextView)findViewById(R.id.title);
        restaurantTypeTitle = (TextView)findViewById(R.id.restaurantType);
        address = (TextView)findViewById(R.id.address);
        delivery = (TextView)findViewById(R.id.delivery);
        restaurantName = (EditText)findViewById(R.id.restaurantName);
        street = (EditText)findViewById(R.id.street);
        city = (EditText)findViewById(R.id.city);
        states = (Spinner)findViewById(R.id.state);
        zipCode = (EditText)findViewById(R.id.zipcode);
        deliveryCharge = (EditText)findViewById(R.id.deliveryCharge);
        deliveryHours = (BootstrapButton)findViewById(R.id.deliveryHours);
        updateRestaurant = (BootstrapButton)findViewById(R.id.createRestaurant);
        cancelUpdateRestaurant = (BootstrapButton)findViewById(R.id.cancelRestaurant);
        restaurantTypes = (Spinner) findViewById(R.id.restaurantTypeSpinner);
        myLocation = (BootstrapButton) findViewById(R.id.location_button);
    }

    /**
     * bind required event handlers
     */
    private void bindEventHandlers() {
        states.setOnItemSelectedListener(this);
        restaurantTypes.setOnItemSelectedListener(this);

        deliveryHours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditRestaurantActivity.this, CreateDeliveryHoursActivity.class);
                intent.putExtra("restId", restaurant.getKey());
                intent.putExtra("editOrCreate", "edit");
                finish();
                startActivity(intent);
            }
        });
        
        updateRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkForValidData()) {
                    //update restaurant values
                    restaurant = updateRestaurantValues();

                    //update the restaurant record
                    //and then check the return value to ensure the restaurant was updated successfully
                    restaurantsRepository
                        .update(restaurant)
                        .addOnCompleteListener(EditRestaurantActivity.this, new OnCompleteListener<TaskResult<Restaurant>>() {
                            @Override
                            public void onComplete(@NonNull Task<TaskResult<Restaurant>> task) {
                                StringBuilder toastMessage = new StringBuilder();
                                if (task.isSuccessful()) {
                                    toastMessage.append("Restaurant updated successfully");
                                } else {
                                    toastMessage.append("An error occurred while updating the restaurant.  Please check your network connection and try again.");
                                }

                                Toast
                                    .makeText(EditRestaurantActivity.this, toastMessage.toString(), Toast.LENGTH_LONG)
                                    .show();

                                //Only go back to the manage restaurants screen if the restaurant was updated successfully...
                                if (task.isSuccessful()) {
                                    Intent intent=new Intent();
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            }
                        });
                }
            }
        });

        cancelUpdateRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        myLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getApplicationContext()
                        , android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(EditRestaurantActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            1);
                }

                if (MapUtil.isLocationEnabled(getApplicationContext())) {
                    try {
                        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

                        if (googleApiClient.isConnected() && location != null) {
                            if (mResultReceiver == null)
                                mResultReceiver = new EditRestaurantActivity.AddressResultReceiver(new Handler());
                            Intent addressIntent = new Intent(getApplicationContext(), FetchAddressIntentService.class);
                            addressIntent.putExtra(AddressConstants.RECEIVER, mResultReceiver);
                            addressIntent.putExtra(AddressConstants.LOCATION_DATA_EXTRA, location);
                            startService(addressIntent);
                        }
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please enable location service.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * retrieve data
     */
    private void retrieveData() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.states, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        states.setAdapter(adapter);

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.restaurantTypeArray, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        restaurantTypes.setAdapter(typeAdapter);

        restaurantTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                // An item was selected.
                restaurantType = parent.getItemAtPosition(pos).toString();
                restaurant.setRestaurantType(restaurantType);

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * perform any final layout updates
     */
    private void finalizeLayout() {
        //set title
        title.setText(resources.getString(R.string.updateRestaurant));

        restaurantName.setText(restaurant.getName());
        street.setText(restaurant.getStreet());
        city.setText(restaurant.getCity());
        zipCode.setText(restaurant.getZipcode());
        deliveryCharge.setText(restaurant.getCharge().toString());

        // Find the user's state in the array of states
        String[] arrayOfStates = getResources().getStringArray(R.array.states);
        int positionOfUserState = Arrays.asList(arrayOfStates).indexOf(StringFormatter.toProperCase(restaurant.getState()));
        states.setSelection(positionOfUserState);

        // Find the user's state in the array of restaurant types
        String[] arrayOfTypes = getResources().getStringArray(R.array.restaurantTypeArray);
        int positionOfType = Arrays.asList(arrayOfTypes).indexOf(StringFormatter.toProperCase(restaurant.getRestaurantType()));
        restaurantTypes.setSelection(positionOfType);

        //set button text
        updateRestaurant.setText(resources.getString(R.string.updateButton));

        underlineText(title);
        underlineText(restaurantTypeTitle);
        underlineText(address);
        underlineText(delivery);
    }

    /**
     * underline the text in the provided textview
     * @param textViewControl identifies the textview control containing the text to be underlined
     */
    private void underlineText(TextView textViewControl) {
        String textToUnderline = textViewControl.getText().toString();
        SpannableString content = new SpannableString(textToUnderline);
        content.setSpan(new UnderlineSpan(), 0, textToUnderline.length(), 0);
        textViewControl.setText(content);
    }

    /**
     * update existing restaurant entity values
     * @return updated restaurant entity
     */
    private Restaurant updateRestaurantValues() {
        //Update existing restaurant entity values
        restaurant.setName(restaurantName.getText().toString());
        restaurant.setStreet(street.getText().toString());
        restaurant.setCity(city.getText().toString());
        restaurant.setState(state);
        restaurant.setZipcode(zipCode.getText().toString());
        restaurant.setCharge(Double.valueOf(deliveryCharge.getText().toString()));
        restaurant.setRestaurantType(restaurantType);

        return restaurant;
    }

    /**
     * checkForValidData checks to ensure that the information entered in to the edit restaurant
     * view is valid
     * @return true or false
     */
    private boolean checkForValidData() {
        ArrayList<Boolean> validations = new ArrayList<>();

        validations.add(Validator.isValid(restaurantName, getString(R.string.nameRequired)));
        validations.add(Validator.isValid(street, FormConstants.REG_EX_ADDRESS, FormConstants.ERROR_TAG_ADDRESS));
        validations.add(Validator.isValid(city, FormConstants.REG_EX_CITY, FormConstants.ERROR_TAG_CITY));
        validations.add(Validator.isValid(zipCode, FormConstants.REG_EX_ZIP, FormConstants.ERROR_TAG_ZIP));
        validations.add(Validator.isValid(deliveryCharge, getString(R.string.deliveryChargeRequired)));
        validations.add(Validator.isValid(deliveryCharge, FormConstants.REG_EX_MONETARY,
                getString(R.string.deliveryChargeGreaterThanZero)));

        return !validations.toString().contains("false");
    }

    /**
     * <p>Callback method to be invoked when an item in this view has been
     * selected. This callback is invoked only when the newly selected
     * position is different from the previously selected position or if
     * there was no selected item.</p>
     * <p>
     * Impelmenters can call getItemAtPosition(position) if they need to access the
     * data associated with the selected item.
     *
     * @param parent   The AdapterView where the selection happened
     * @param view     The view within the AdapterView that was clicked
     * @param position The position of the view in the adapter
     * @param id       The row id of the item that is selected
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        state = (String)parent.getItemAtPosition(position);
    }

    /**
     * Callback method to be invoked when the selection disappears from this
     * view. The selection can disappear for instance when touch is activated
     * or when the adapter becomes empty.
     *
     * @param parent The AdapterView that now contains no selected item.
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //not implemented
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this
                , android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);

            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                    } catch (SecurityException se) {
                        se.printStackTrace();
                    }
                } else {
                }
                verifiedAddr = MapUtil.verifyAddress(getApplicationContext(), street, city, state, zipCode);
                return;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            final Address addressOutput = resultData.getParcelable(AddressConstants.RESULT_DATA_KEY);
            if (addressOutput == null) {
                Toast.makeText(getApplicationContext(), "Couldn't Retrieve Location.", Toast.LENGTH_SHORT).show();
            }

            String[] statesArray = getResources().getStringArray(R.array.states);
            street.setText(addressOutput.getAddressLine(0));
            for (int i = 0; i < statesArray.length; i++) {
                if (addressOutput.getAdminArea().trim().equals(statesArray[i]))
                    states.setSelection(i);
            }
            city.setText(addressOutput.getLocality());
            zipCode.setText(addressOutput.getPostalCode());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }
}
