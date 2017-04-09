package com.gmail.dleemcewen.tandemfieri.menubuilder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.gmail.dleemcewen.tandemfieri.R;

public class OptionAddActivity extends AppCompatActivity {

    private MenuIterator iterator = MenuIterator.create();

    private CheckBox requiredCheck;
    private EditText name;
    private boolean editing;
    private ItemOption option;
    private BootstrapButton selections;
    private boolean isAnd;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option_add);

        option = iterator.getCurrentOption();

        editing = option != null;
        context = this;

        name = (EditText) findViewById(R.id.itemName);
        requiredCheck = (CheckBox) findViewById(R.id.requiredCheck);


        if (editing) {
            name.setText(option.getOptionName());
            isAnd=option.isAndRelationship();
            ((RadioButton) findViewById(R.id.andRadio)).setChecked(isAnd);
            ((RadioButton) findViewById(R.id.orRadio)).setChecked(!isAnd);
            requiredCheck.setChecked(option.isRequired());
        } else {
            TextView title = (TextView) findViewById(R.id.title);
            isAnd = true;
            ((RadioButton) findViewById(R.id.andRadio)).setChecked(isAnd);
            title.setText("Add Option");
        }


        selections = (BootstrapButton) findViewById(R.id.selections);
        BootstrapButton save = (BootstrapButton) findViewById(R.id.saveItem);

        selections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open Selections activity
                if (!editing) {
                    ItemOption temp = new ItemOption(name.getText().toString(), requiredCheck.isChecked(), ((RadioButton) findViewById(R.id.andRadio)).isChecked());
                    option = temp;
                    MenuItem parrent = (MenuItem) iterator.getCurrentComponent();
                    parrent.getOptions().add(option);
                    editing = true;
                }
                iterator.setCurrentOption(option);
                Intent intent = new Intent(context, OptionSelectionListActivity.class);
                startActivity(intent);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editing) {
                    option.setOptionName(name.getText().toString());
                    option.setRequired(requiredCheck.isChecked());
                    option.setAndRelationship(((RadioButton) findViewById(R.id.andRadio)).isChecked());
                } else {
                    ItemOption temp = new ItemOption(name.getText().toString(), requiredCheck.isChecked(), ((RadioButton) findViewById(R.id.andRadio)).isChecked());
                    option = temp;
                    MenuItem parrent = (MenuItem) iterator.getCurrentComponent();
                    parrent.getOptions().add(option);
                }
                finish();
            }
        });
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.andRadio:
                isAnd = true;
                break;
            case R.id.orRadio:
                isAnd = false;
                break;
        }
    }
}

