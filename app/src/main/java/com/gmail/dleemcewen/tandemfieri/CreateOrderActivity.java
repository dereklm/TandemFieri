package com.gmail.dleemcewen.tandemfieri;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ExpandableListView;

import com.gmail.dleemcewen.tandemfieri.Adapters.OrderListAdapter;
import com.gmail.dleemcewen.tandemfieri.Entities.Restaurant;
import com.gmail.dleemcewen.tandemfieri.menubuilder.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class CreateOrderActivity extends AppCompatActivity {

    private ExpandableListView itemsList;
    private OrderListAdapter ola;
    private List<MenuItem> menuItems;
    private Restaurant restaurant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_order);

        menuItems = new ArrayList<>();
        restaurant = (Restaurant) getIntent().getSerializableExtra("Restaurant");
        menuItems.addAll(restaurant.getMenu().getSubItems());


        itemsList = (ExpandableListView) findViewById(R.id.items_list);

        ola = new OrderListAdapter(CreateOrderActivity.this, menuItems);
        itemsList.setAdapter(ola);
    }
}
