package com.gmail.dleemcewen.tandemfieri.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.gmail.dleemcewen.tandemfieri.Entities.OrderItemOption;
import com.gmail.dleemcewen.tandemfieri.Entities.OrderItemOptionGroup;
import com.gmail.dleemcewen.tandemfieri.R;

import java.text.NumberFormat;
import java.util.List;

/**
 * Created by Derek on 3/5/2017.
 */

public class OrderGroupAdapter extends BaseExpandableListAdapter {

    private Activity activity;
    private Context context;
    private List<OrderItemOptionGroup> groups;
    private LayoutInflater inflater;

    public OrderGroupAdapter (Activity activity, Context context, List<OrderItemOptionGroup> groups) {
        this.activity = activity;
        this.context = context;
        this.groups = groups;
        inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return groups.get(groupPosition).getOptions().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return groups.get(groupPosition).getOptions().get(childPosition);
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
        OrderItemOptionGroup group = (OrderItemOptionGroup) getGroup(groupPosition);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.order_item, null);
        }

        TextView name = (TextView) convertView.findViewById(R.id.item_name);
        name.setText(group.getName());
        TextView description = (TextView) convertView.findViewById(R.id.item_description);
        if (group.isRequired() && group.isExclusive()) description.setText("Must select a single item in this group.");
        else if (group.isRequired()) description.setText("Must select at least one item in this group.");
        else if (group.isExclusive()) description.setText("Only one item in this group can be selected");
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        OrderItemOption option = (OrderItemOption) getChild(groupPosition, childPosition);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.order_item, null);
        }

        TextView name = (TextView) convertView.findViewById(R.id.item_name);
        name.setText(option.getName());
        TextView description = (TextView) convertView.findViewById(R.id.item_description);
        description.setText(option.getDescription());
        TextView price = (TextView) convertView.findViewById(R.id.item_price);
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        price.setText(formatter.format(option.getAddedPrice()));

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
