package com.gmail.dleemcewen.tandemfieri.menubuilder;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.gmail.dleemcewen.tandemfieri.R;
import com.gmail.dleemcewen.tandemfieri.Utilities.MoneyTextWatcher;

public class OptionSelectionEditActivity extends AppCompatActivity {

    private MenuIterator iterator = MenuIterator.create();
    private EditText price;
    private EditText name;
    private EditText desc;
    private boolean editing;
    private OptionSelection selection;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option_selection_edit);

        selection = iterator.getCurrentSelection();

        editing = selection!=null;
        context=this;

        price = (EditText)findViewById(R.id.price);
        name = (EditText)findViewById(R.id.itemName);
        desc = (EditText)findViewById(R.id.desc);

        if(editing){
            name.setText(selection.getSelectionName());
            price.setText(String.format("$%.2f",selection.getAddedPrice()));
            desc.setText(selection.getDescription());
        }
        else{
            TextView title = (TextView) findViewById(R.id.title);
            title.setText("Add Selection");
        }

        price.addTextChangedListener(new MoneyTextWatcher(price));

        BootstrapButton save = (BootstrapButton) findViewById(R.id.saveItem);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editing) {
                    if (price.getText().toString().isEmpty()) {
                        Toast
                                .makeText(context, "Price can not be empty", Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    String toConvert = price.getText().toString().replace("$", "");
                    toConvert = toConvert.replace(",", "");
                    selection.setAddedPrice(Double.parseDouble(toConvert));
                    selection.setSelectionName(name.getText().toString());
                    selection.setDescription(desc.getText().toString());
                } else {
                    selection = new OptionSelection();
                    if (price.getText().toString().isEmpty()) {
                        Toast
                                .makeText(context, "Price can not be empty", Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    String toConvert = price.getText().toString().replace("$", "");
                    toConvert = toConvert.replace(",", "");
                    selection.setAddedPrice(Double.parseDouble(toConvert));
                    selection.setSelectionName(name.getText().toString());
                    selection.setDescription(desc.getText().toString());
                    ItemOption parrent = iterator.getCurrentOption();
                    parrent.getSelections().add(selection);
                }
                finish();
            }
        });
    }
}
