package com.gmail.dleemcewen.tandemfieri.menubuilder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.gmail.dleemcewen.tandemfieri.R;
import com.gmail.dleemcewen.tandemfieri.Utilities.MoneyTextWatcher;

public class MenuItemEditActivity extends AppCompatActivity {

    private MenuIterator iterator = MenuIterator.create();

    private EditText price;
    private EditText name;
    private boolean editing;
    private MenuCompenet item;
    private BootstrapButton options;
    private boolean isCat;
    private TextView priceLable;
    private boolean added = false;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_item_edit);

        item = iterator.getCurrentComponent();

        editing = item!=null;
        context=this;



        price = (EditText)findViewById(R.id.price);
        name = (EditText)findViewById(R.id.itemName);
        priceLable = (TextView) findViewById(R.id.priceLable);

        if(editing){
            RadioGroup radios =(RadioGroup) findViewById(R.id.Catigory);
            radios.setVisibility(View.INVISIBLE);
            MenuItem temp = (MenuItem) item;
            name.setText(temp.getName());
            price.setText(String.format("$%.2f",temp.getBasePrice()));
            isCat=false;
        }
        else{
            TextView title = (TextView) findViewById(R.id.title);
            title.setText("Add Item");
        }

        isCat=false;
        ((RadioButton)findViewById(R.id.itemRadio)).setChecked(true);

        price.addTextChangedListener(new MoneyTextWatcher(price));

        options = (BootstrapButton) findViewById(R.id.options);
        BootstrapButton save = (BootstrapButton) findViewById(R.id.saveItem);

        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open option activity
                if (!editing) {
                    MenuItem temp = new MenuItem();
                    if (price.getText().toString().isEmpty()) {
                        Toast
                                .makeText(MenuItemEditActivity.this, "Price can not be empty", Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    String toConvert = price.getText().toString().replace("$", "");
                    toConvert = toConvert.replace(",", "");
                    temp.setBasePrice(Double.parseDouble(toConvert));
                    temp.setName(name.getText().toString());
                    added = true;
                    item = temp;
                    iterator.backtrack();
                    MenuCatagory parrent = (MenuCatagory) iterator.getCurrentComponent();
                    parrent.getSubItems().add((MenuItem) item);
                    editing = true;
                    iterator.setCurrentComponent(item);
                }
                Intent intent = new Intent(context,MenuOptionsActivity.class);
                startActivity(intent);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editing){
                    if(!isCat) {
                        MenuItem temp = (MenuItem) item;
                        if(price.getText().toString().isEmpty()) {
                            Toast
                                    .makeText(MenuItemEditActivity.this, "Price can not be empty", Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }
                        String toConvert = price.getText().toString().replace("$","");
                        toConvert = toConvert.replace(",","");
                        temp.setBasePrice(Double.parseDouble(toConvert));
                        temp.setName(name.getText().toString());
                    }
                    else{
                        item.setName(name.getText().toString());
                    }
                }
                else{
                    if(!isCat) {
                        MenuItem temp = new MenuItem();
                        if(price.getText().toString().isEmpty()) {
                            Toast
                                    .makeText(MenuItemEditActivity.this, "Price can not be empty", Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }
                        String toConvert = price.getText().toString().replace("$","");
                        toConvert = toConvert.replace(",","");
                        temp.setBasePrice(Double.parseDouble(toConvert));
                        temp.setName(name.getText().toString());
                        added=true;
                        item=temp;
                        iterator.backtrack();
                        MenuCatagory parrent = (MenuCatagory) iterator.getCurrentComponent();
                        parrent.getSubItems().add((MenuItem) item);
                    }
                    else{
                        iterator.backtrack();
                        item = (new MenuCatagory(name.getText().toString()));
                        MenuCatagory temp = (MenuCatagory) iterator.getCurrentComponent();
                        temp.getSubCategories().add((MenuCatagory) item);
                        added=true;
                    }
                }
                Intent resultIntent = new Intent();
                finish();
            }
        });
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.categoryRadio:
                if (checked)
                    price.setVisibility(View.INVISIBLE);
                    options.setVisibility(View.INVISIBLE);
                    priceLable.setVisibility(View.INVISIBLE);
                    isCat=true;
                    break;
            case R.id.itemRadio:
                if (checked)
                    price.setVisibility(View.VISIBLE);
                    options.setVisibility(View.VISIBLE);
                    priceLable.setVisibility(View.VISIBLE);
                    isCat=false;
                    break;
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(isFinishing()) {

            if (!added) {
                iterator.backtrack();
            }
        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        this.finish();
    }
}
