package com.gmail.dleemcewen.tandemfieri;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ExpandableListView;

import com.gmail.dleemcewen.tandemfieri.Adapters.OrderItemAdapter;
import com.gmail.dleemcewen.tandemfieri.Entities.OrderItem;
import com.gmail.dleemcewen.tandemfieri.Entities.OrderItemOption;
import com.gmail.dleemcewen.tandemfieri.Entities.Restaurant;
import com.gmail.dleemcewen.tandemfieri.menubuilder.ItemOption;
import com.gmail.dleemcewen.tandemfieri.menubuilder.MenuItem;
import com.gmail.dleemcewen.tandemfieri.menubuilder.OptionSelection;

import java.util.ArrayList;
import java.util.List;

public class OrderMenuActivity extends AppCompatActivity {

    private OrderItemAdapter orderItemAdapter;
    private List<OrderItem> items;
    private List<MenuItem> menuItems;
    private Restaurant restaurant;
    private ExpandableListView expandableListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_menu);

        menuItems = new ArrayList<>();
        restaurant = (Restaurant) getIntent().getSerializableExtra("Restaurant");
        menuItems.addAll(restaurant.getMenu().getSubItems());
        items = new ArrayList<>();
        expandableListView = (ExpandableListView) findViewById(R.id.menu_items);

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

        orderItemAdapter = new OrderItemAdapter(OrderMenuActivity.this, this, items);
        expandableListView.setAdapter(orderItemAdapter);
    }
}
