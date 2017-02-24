package com.gmail.dleemcewen.tandemfieri;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import static com.gmail.dleemcewen.tandemfieri.R.id.phone;

public class CreateAccountActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public Button nextButton;

    public EditText firstName, lastName, address, city, zip, phoneNumber, email;
    private String state = "";
    private Spinner states;
    private static CreateAccountActivity activityInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        activityInstance = this;

        nextButton = (Button)findViewById(R.id.nextButton);

        firstName = (EditText) findViewById(R.id.firstName);
        lastName = (EditText) findViewById(R.id.lastName);
        address = (EditText) findViewById(R.id.address);
        city = (EditText) findViewById(R.id.city);
        states = (Spinner) findViewById(R.id.state);
        zip = (EditText) findViewById(R.id.zip);
        phoneNumber = (EditText) findViewById(phone);
        email = (EditText) findViewById(R.id.email);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CreateAccountActivity.this, AlmostDoneActivity.class);
                intent.putExtra("firstName", firstName.getText().toString());
                intent.putExtra("lastName", lastName.getText().toString());
                intent.putExtra("address", address.getText().toString());
                intent.putExtra("city", city.getText().toString());
                intent.putExtra("state", state);
                intent.putExtra("zip", zip.getText().toString());
                intent.putExtra("phoneNumber", phoneNumber.getText().toString());
                intent.putExtra("email", email.getText().toString());
                if(email.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "Do not leave email blank", Toast.LENGTH_LONG).show();
                } else if(!email.getText().toString().contains("@")){
                    Toast.makeText(getApplicationContext(), "Email must contain an @", Toast.LENGTH_LONG).show();
                }
                startActivity(intent);
            }
        });

        states.setOnItemSelectedListener(activityInstance);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.states, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        states.setAdapter(adapter);
    }

    public static CreateAccountActivity getInstance() {
        return activityInstance;
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
}
