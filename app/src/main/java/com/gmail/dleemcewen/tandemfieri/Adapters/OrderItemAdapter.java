package com.gmail.dleemcewen.tandemfieri.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.gmail.dleemcewen.tandemfieri.Entities.OrderItem;
import com.gmail.dleemcewen.tandemfieri.Entities.OrderItemOptionGroup;
import com.gmail.dleemcewen.tandemfieri.R;

import java.text.NumberFormat;
import java.util.List;

/**
 * Created by Derek on 3/5/2017.
 */

public class OrderItemAdapter extends BaseExpandableListAdapter {

    private Activity activity;
    private Context context;
    private List<OrderItem> items;
    private LayoutInflater inflater;

    public OrderItemAdapter (Activity activity, Context context, List<OrderItem> items) {
        this.activity = activity;
        this.context = context;
        this.items = items;
        inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getGroupCount() {
        return items.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return items.get(groupPosition).getOptionGroups().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return items.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return items.get(groupPosition).getOptionGroups().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        OrderItem item = (OrderItem) getGroup(groupPosition);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.order_item, null);
        }

        TextView name = (TextView) convertView.findViewById(R.id.item_name);
        name.setText(item.getName());
        TextView price = (TextView) convertView.findViewById(R.id.item_price);
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        price.setText(formatter.format(item.getBasePrice()));

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        OrderItemOptionGroup group = (OrderItemOptionGroup) getChild(groupPosition, childPosition);
        ExpandableListView groupsListView;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.order_item_group, null);
        }

        groupsListView = (ExpandableListView) convertView.findViewById(R.id.item_groups);
        OrderGroupAdapter orderGroupAdapter =
                new OrderGroupAdapter(activity, context,
                        ((OrderItem) getGroup(groupPosition)).getOptionGroups());
        groupsListView.setAdapter(orderGroupAdapter);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
