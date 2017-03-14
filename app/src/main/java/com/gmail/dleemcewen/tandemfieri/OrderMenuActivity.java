package com.gmail.dleemcewen.tandemfieri;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;

import com.gmail.dleemcewen.tandemfieri.Adapters.OrderItemAdapter;
import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.Entities.OrderItem;
import com.gmail.dleemcewen.tandemfieri.Entities.OrderItemOption;
import com.gmail.dleemcewen.tandemfieri.Entities.Restaurant;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.menubuilder.ItemOption;
import com.gmail.dleemcewen.tandemfieri.menubuilder.MenuItem;
import com.gmail.dleemcewen.tandemfieri.menubuilder.OptionSelection;

import java.util.ArrayList;
import java.util.List;

public class OrderMenuActivity extends AppCompatActivity {
    User user;
    private Order order;
    private OrderItemAdapter orderItemAdapter;
    private List<OrderItem> items;
    private List<MenuItem> menuItems;
    private Restaurant restaurant;
    private ExpandableListView expandableListView;
    private Button goToCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_menu);

        menuItems = new ArrayList<>();
        restaurant = (Restaurant) getIntent().getSerializableExtra("Restaurant");
        user = (User) getIntent().getSerializableExtra("User");
        menuItems.addAll(restaurant.getMenu().getSubItems());
        items = new ArrayList<>();
        expandableListView = (ExpandableListView) findViewById(R.id.menu_items);
        goToCart = (Button) findViewById(R.id.cart);
        order = new Order();

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
        expandableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    OrderItem item = (OrderItem) orderItemAdapter.getGroup(groupPosition);
                    order.addItem(item);

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
                intent.putExtra("restaurantId", restaurant.getRestaurantKey());
                intent.putExtra("ownerId", restaurant.getOwnerId());
                intent.putExtra("deliveryCharge", restaurant.getCharge());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (order.getItems().size() > 0) order = new Order();
    }
}
