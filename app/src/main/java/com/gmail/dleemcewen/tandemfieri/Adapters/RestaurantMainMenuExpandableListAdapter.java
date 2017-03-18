package com.gmail.dleemcewen.tandemfieri.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.ManageOrders;
import com.gmail.dleemcewen.tandemfieri.R;
import com.gmail.dleemcewen.tandemfieri.ViewOrderActivity;
import com.google.firebase.database.DatabaseReference;

import java.util.List;
import java.util.Map;

/**
 * Created by Ruth on 3/11/2017.
 */

public class RestaurantMainMenuExpandableListAdapter extends BaseExpandableListAdapter {

    private Activity context;
    private List<Order> orderList;
    private Map<String, List<Order>> childList;
    private Resources resources;
    private DatabaseReference mDatabase;
    private TextView orderName;
    private Order order;
    private LayoutInflater inflater;
    private Button manage_button;
    private User user;

    public RestaurantMainMenuExpandableListAdapter(Activity context, List<Order> orderList,
                                                   Map<String, List<Order>> childList, User user) {
        this.context = context;
        this.orderList = orderList;
        this.childList = childList;
        this.user = user;

    }//end constructor

    public Object getChild(int groupPosition, int childPosition) {
        return childList.get(orderList.get(groupPosition).getKey()).get(childPosition);
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    public int getChildrenCount(int groupPosition) {
        return childList.get(orderList.get(groupPosition).getKey()).size();
    }

    public Object getGroup(int groupPosition) {
        return orderList.get(groupPosition);
    }

    public int getGroupCount() {
        return orderList.size();
    }

    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    public boolean hasStableIds() {
        return false;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final Order order = (Order)getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.restaurant_main_menu_group_header, null);
        }

        orderName = (TextView)convertView.findViewById(R.id.order_name);
        orderName.setTypeface(null, Typeface.BOLD);
        orderName.setText(order.toString());
        orderName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //LogWriter.log(context, Level.INFO, "This should open the order");
                Intent intent = new Intent(context, ViewOrderActivity.class);
                Bundle orderBundle = new Bundle();
                orderBundle.putSerializable("Order", order);
                intent.putExtras(orderBundle);
                context.startActivity(intent);
            }
        });

        return convertView;
    }

    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final Order selectedOrder = (Order) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.restaurant_main_menu_list_item, null);
        }

        Button manage_button = (Button) convertView.findViewById(R.id.manage_button);
        manage_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("Order", selectedOrder);
                bundle.putSerializable("Owner", user);
                Intent intent = new Intent(context, ManageOrders.class);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });

        Button status_button = (Button) convertView.findViewById(R.id.order_button);
        status_button.setText("Change Status");
        status_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast
                        .makeText(context, "change Status for " + selectedOrder.getKey(), Toast.LENGTH_SHORT)
                        .show();
            }
        });

        return convertView;
    }//end get child view

}//end class
