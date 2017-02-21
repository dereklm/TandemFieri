package com.gmail.dleemcewen.tandemfieri;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import static com.gmail.dleemcewen.tandemfieri.R.id.phone;
import static com.gmail.dleemcewen.tandemfieri.Validator.Validator.isValid;

public class CreateAccountActivity extends AppCompatActivity {

    public Button nextButton;

    public EditText firstName, lastName, address, city, state, zip, phoneNumber, email;
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
        state = (EditText) findViewById(R.id.state);
        zip = (EditText) findViewById(R.id.zip);
        phoneNumber = (EditText) findViewById(phone);
        email = (EditText) findViewById(R.id.email);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean firstNameValid = isValid(firstName, FormConstants.REG_EX_FIRSTNAME, FormConstants.ERROR_TAG_FIRSTNAME);
                boolean lastNameValid = isValid(lastName, FormConstants.REG_EX_LASTNAME, FormConstants.ERROR_TAG_LASTNAME);
                boolean addressValid = isValid(address, FormConstants.REG_EX_ADDRESS, FormConstants.ERROR_TAG_ADDRESS);
                boolean cityValid = isValid(city, FormConstants.REG_EX_CITY, FormConstants.ERROR_TAG_CITY);
                boolean stateValid = isValid(state, FormConstants.REG_EX_STATE, FormConstants.ERROR_TAG_STATE);
                boolean emailValid = isValid(email, FormConstants.REG_EX_EMAIL, FormConstants.ERROR_TAG_EMAIL);
                boolean phoneNumberValid = isValid(phoneNumber, FormConstants.REG_EX_PHONE, FormConstants.ERROR_TAG_PHONE);
                boolean zipValid = isValid(zip, FormConstants.REG_EX_ZIP, FormConstants.ERROR_TAG_ZIP);

                if (    firstNameValid      &&
                        lastNameValid       &&
                        addressValid        &&
                        cityValid           &&
                        stateValid          &&
                        zipValid            &&
                        phoneNumberValid    &&
                        emailValid ) {

                    Intent intent = new Intent(CreateAccountActivity.this, AlmostDoneActivity.class);
                    intent.putExtra("firstName", firstName.getText().toString());
                    intent.putExtra("lastName", lastName.getText().toString());
                    intent.putExtra("address", address.getText().toString());
                    intent.putExtra("city", city.getText().toString());
                    intent.putExtra("state", state.getText().toString());
                    intent.putExtra("zip", zip.getText().toString());
                    intent.putExtra("phoneNumber", phoneNumber.getText().toString());
                    intent.putExtra("email", email.getText().toString());

                    startActivity(intent);
                }
            }
        });
    }

    public static CreateAccountActivity getInstance() {
        return activityInstance;
    }
}
