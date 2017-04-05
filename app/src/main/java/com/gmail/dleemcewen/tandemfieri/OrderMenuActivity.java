package com.gmail.dleemcewen.tandemfieri;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.gmail.dleemcewen.tandemfieri.menubuilder.ItemOption;
import com.gmail.dleemcewen.tandemfieri.menubuilder.MenuCatagory;
import com.gmail.dleemcewen.tandemfieri.menubuilder.MenuItem;
import com.gmail.dleemcewen.tandemfieri.menubuilder.OptionSelection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class OrderMenuActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
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
    private String latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_menu);

        Bundle bundle = this.getIntent().getExtras();
        restaurant = (Restaurant) getIntent().getSerializableExtra("Restaurant");
        user = (User) getIntent().getSerializableExtra("User");
        latitude = bundle.getString("Latitude");
        longitude = bundle.getString("Longitude");



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
            for (ItemOption optionGroup : item.getOptions()) {
                items.get(items.size() - 1).addOptionGroup(optionGroup);
                for (OptionSelection selection : optionGroup.getSelections()) {
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

        orderItemAdapter = new OrderItemAdapter(OrderMenuActivity.this, this, items);
        expandableListView.setAdapter(orderItemAdapter);
        expandableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    OrderItem item = (OrderItem) orderItemAdapter.getGroup(groupPosition);

                    for (OrderItemOptionGroup group : item.getOptionGroups()) {
                        // if option group has no child selected and it is required, notify user.
                        if (group.isRequired() && !group.isHasChildSelected()) {
                            Toast.makeText(getApplicationContext(),
                                    "Must select an item for option group " + group.getName(),
                                    Toast.LENGTH_SHORT).show();
                            return true;
                        }
                    }

                    // ensure passed item only passes options that are selected.
                    Iterator<OrderItemOptionGroup> groupIterator = item.getOptionGroups().iterator();
                    while (groupIterator.hasNext()) {
                        OrderItemOptionGroup group = groupIterator.next();

                        // if option group has no child selected remove it.
                        if (!group.isHasChildSelected()) {
                            groupIterator.remove();
                        } else {
                            // group has options selected, if the options are false remove them.
                            Iterator<OrderItemOption> optionIterator = group.getOptions().iterator();
                            while (optionIterator.hasNext()) {
                                if (!optionIterator.next().isSelected()) optionIterator.remove();
                            }
                        }
                    }
                    order.addItem(item);

                    Toast.makeText(getApplicationContext(),
                            "Item added to cart.",
                            Toast.LENGTH_SHORT).show();

                    return true;
                }

                return false;
            }
        });

        goToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderMenuActivity.this, CartActivity.class);
                intent.putExtra("cart", order);
                intent.putExtra("restaurantId", restaurant.getId());
                intent.putExtra("ownerId", restaurant.getOwnerId());
                intent.putExtra("deliveryCharge", restaurant.getCharge());
                intent.putExtra("restaurantName", restaurant.getName());
                intent.putExtra("Latitude", latitude);
                intent.putExtra("Longitude", longitude);
                intent.putExtra("braintreeID", user.getBraintreeId());
                startActivityForResult(intent, 1);
            }
        });
    }

    public void setupCategories(MenuCatagory category) {
        if (category.getSubCategories().size() > 0)
            for(MenuCatagory c : category.getSubCategories()) {
                setupCategories(c);
            }

        List<OrderItem> categoryItems = new ArrayList<>();
        for (MenuItem item : category.getSubItems()) {
            categoryItems.add(new OrderItem(item));
            for (ItemOption optionGroup : item.getOptions()) {
                categoryItems.get(categoryItems.size() - 1).addOptionGroup(optionGroup);
                for (OptionSelection selection : optionGroup.getSelections()) {
                    categoryItems.get(categoryItems.size() - 1).getOptionGroups().
                            get(categoryItems.get(categoryItems.size() - 1).
                            getOptionGroups().size() - 1).addOption(new OrderItemOption(selection));
                }
            }
        }

        menuCategories.put(category.getName(), categoryItems);
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetSelections();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_CANCELED) order = new Order();
            else order = (Order) data.getSerializableExtra("order");
        }
    }

    private void resetSelections() {
        for (OrderItem item : order.getItems()) {
            for (OrderItemOptionGroup group : item.getOptionGroups()) {
                group.setHasChildSelected(false);
                for (OrderItemOption option : group.getOptions()) {
                    option.setSelected(false);
                }
            }
        }
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
}
