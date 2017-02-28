package com.gmail.dleemcewen.tandemfieri.MenuBuilder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.gmail.dleemcewen.tandemfieri.R;
import com.gmail.dleemcewen.tandemfieri.Utilities.MoneyTextWatcher;

public class MenuItemEditActivity extends AppCompatActivity {

    private EditText price;
    private EditText name;
    private MenuCatagory parentCat;
    private boolean editing;
    private MenuCompenet item;
    private Button options;
    private boolean isCat;
    private TextView priceLable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_item_edit);

        Bundle bundle = this.getIntent().getExtras();
        parentCat = (MenuCatagory) bundle.getSerializable("parent");
        item = (MenuItem)  bundle.getSerializable("item");

        editing = item!=null;



        price = (EditText)findViewById(R.id.price);
        name = (EditText)findViewById(R.id.itemName);
        priceLable = (TextView) findViewById(R.id.priceLable);

        if(editing){
            RadioGroup radios =(RadioGroup) findViewById(R.id.Catigory);
            radios.setVisibility(View.INVISIBLE);
            MenuItem temp = (MenuItem) item;
            name.setText(temp.getName());
            price.setText(temp.getBasePrice()+"");
            isCat=false;
        }

        isCat=false;
        ((RadioButton)findViewById(R.id.itemRadio)).setChecked(true);

        price.addTextChangedListener(new MoneyTextWatcher(price));

        options = (Button) findViewById(R.id.options);
        Button save = (Button) findViewById(R.id.saveItem);

        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open option activity
                /* pushed to next sprint
                MenuItem temp;
                if(item!=null) temp = (MenuItem) item;
                else temp = new MenuItem();
                temp.setBasePrice(Double.parseDouble(price.getText().toString()));
                temp.setName(name.getText().toString());
                item = temp;*/
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editing){
                    if(!isCat) {
                        MenuItem temp = (MenuItem) item;
                        temp.setBasePrice(Double.parseDouble(price.getText().toString().substring(1)));
                        temp.setName(name.getText().toString());
                        for(MenuCompenet mc : parentCat.getSubItems()){
                            if(mc.name.equals(item.getName())){
                                item=mc;
                            }
                        }
                        parentCat.getSubItems().remove(item);
                        parentCat.getSubItems().add(temp);
                    }
                    else{
                        item.setName(name.getText().toString());
                    }
                }
                else{
                    if(!isCat) {
                        MenuItem temp = new MenuItem();
                        temp.setBasePrice(Double.parseDouble(price.getText().toString().substring(1)));
                        temp.setName(name.getText().toString());
                        parentCat.getSubItems().add(temp);
                    }
                    else{
                        parentCat.getSubCategories().add(new MenuCatagory(name.getText().toString()));
                    }
                }
                Intent resultIntent = new Intent();
                resultIntent.putExtra("current",parentCat);
                setResult(Activity.RESULT_OK,resultIntent);
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
}
