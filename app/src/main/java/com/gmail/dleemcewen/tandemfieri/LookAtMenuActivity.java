package com.gmail.dleemcewen.tandemfieri;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.gmail.dleemcewen.tandemfieri.Adapters.OrderItemAdapter;
import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.Entities.OrderItem;
import com.gmail.dleemcewen.tandemfieri.Entities.OrderItemOption;
import com.gmail.dleemcewen.tandemfieri.Entities.OrderItemOptionGroup;
import com.gmail.dleemcewen.tandemfieri.Entities.Restaurant;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.menubuilder.MenuCatagory;
import com.gmail.dleemcewen.tandemfieri.menubuilder.MenuItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static com.gmail.dleemcewen.tandemfieri.R.id.orderLaunch;

public class LookAtMenuActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    User user;
    private Order order;
    private OrderItemAdapter orderItemAdapter;
    private List<OrderItem> items;
    private List<MenuItem> menuItems;
    private List<String> categories;
    private Restaurant restaurant;
    private ExpandableListView expandableListView;
    private HashMap<String, List<OrderItem>> menuCategories;
    private BootstrapButton goToCart;
    private Spinner categorySpinner;
    private ArrayAdapter<String> spinnerAdapter;
    private String latitude, longitude, controlString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_look_at_menu);

        Bundle bundle = this.getIntent().getExtras();
        restaurant = (Restaurant) getIntent().getSerializableExtra("Restaurant");
        user = (User) getIntent().getSerializableExtra("User");
        latitude = bundle.getString("Latitude");
        longitude = bundle.getString("Longitude");
        controlString = bundle.getString("OpenClosed");


        menuCategories = new HashMap<>();
        menuItems = new ArrayList<>();

        menuItems.addAll(restaurant.getMenu().getSubItems());
        items = new ArrayList<>();
        expandableListView = (ExpandableListView) findViewById(R.id.menu_items);
        goToCart = (BootstrapButton) findViewById(R.id.cart);
        categorySpinner = (Spinner) findViewById(R.id.category_spinner);
        order = new Order();


        // base items.
        for (MenuItem item : menuItems) {
            items.add(new OrderItem(item));
            for (com.gmail.dleemcewen.tandemfieri.menubuilder.ItemOption optionGroup : item.getOptions()) {
                items.get(items.size() - 1).addOptionGroup(optionGroup);
                for (com.gmail.dleemcewen.tandemfieri.menubuilder.OptionSelection selection : optionGroup.getSelections()) {
                    items.get(items.size() - 1).getOptionGroups().get(items.get(items.size() - 1).
                            getOptionGroups().size() - 1).addOption(new OrderItemOption(selection));
                }
            }
        }

        // put base items as a category.
        menuCategories.put("Base", items);
        // build other categories.
        if (restaurant.getMenu().getSubCategories().size() > 0) {
            setupCategories(restaurant.getMenu().getSubCategories().get(0));
        }

        // Create spinner adapter and add all categorires to it.
        categories = new ArrayList<>(menuCategories.keySet());
        spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(spinnerAdapter);
        for (String s : categories) {
            spinnerAdapter.add(s);
        }
        categorySpinner.setOnItemSelectedListener(this);
        spinnerAdapter.notifyDataSetChanged();

        orderItemAdapter = new OrderItemAdapter(LookAtMenuActivity.this, this, items);
        expandableListView.setAdapter(orderItemAdapter);

    }

    public void setupCategories(MenuCatagory category) {
        if (category.getSubCategories().size() > 0)
            for(MenuCatagory c : category.getSubCategories()) {
                setupCategories(c);
            }

        List<OrderItem> categoryItems = new ArrayList<>();
        for (MenuItem item : category.getSubItems()) {
            categoryItems.add(new OrderItem(item));
            for (com.gmail.dleemcewen.tandemfieri.menubuilder.ItemOption optionGroup : item.getOptions()) {
                categoryItems.get(categoryItems.size() - 1).addOptionGroup(optionGroup);
                for (com.gmail.dleemcewen.tandemfieri.menubuilder.OptionSelection selection : optionGroup.getSelections()) {
                    categoryItems.get(categoryItems.size() - 1).getOptionGroups().
                            get(categoryItems.get(categoryItems.size() - 1).
                                    getOptionGroups().size() - 1).addOption(new OrderItemOption(selection));
                }
            }
        }

        menuCategories.put(category.getName(), categoryItems);
    }



    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        orderItemAdapter.setItems(menuCategories.get(categories.get(position)));
        orderItemAdapter.notifyDataSetChanged();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //nothing
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.look_at_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case orderLaunch:
                if(controlString.contains("CLOSED")){
                    Toast.makeText(getApplicationContext(), "Sorry m8 the restaurant is not open go to Taco Bell", Toast.LENGTH_LONG).show();
                }else {
                    Intent orderLaunch = new Intent(LookAtMenuActivity.this, OrderMenuActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("Latitude", latitude);
                    bundle.putString("Longitude", longitude);
                    orderLaunch.putExtras(this.getIntent().getExtras());
                    orderLaunch.putExtras(bundle);
                    startActivity(orderLaunch);
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}




