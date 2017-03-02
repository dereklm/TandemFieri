package com.gmail.dleemcewen.tandemfieri.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.gmail.dleemcewen.tandemfieri.R;
import com.gmail.dleemcewen.tandemfieri.menubuilder.ItemOption;
import com.gmail.dleemcewen.tandemfieri.menubuilder.MenuItem;

import java.text.NumberFormat;
import java.util.List;

/**
 * Created by Derek on 3/1/2017.
 */

public class OrderListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<MenuItem> items;

    public OrderListAdapter (Context context, List<MenuItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getGroupCount() {
        return items.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        // TODO: figure out how to get size of options list.
        List<ItemOption> options = items.get(groupPosition).getOptions();
        return options.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return items.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
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
    public View getGroupView(final int groupPosition, boolean isExpanded, View view, ViewGroup parent) {
        MenuItem item = (MenuItem) getGroup(groupPosition);
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.order_list_item, null);
        }
        TextView name = (TextView) view.findViewById(R.id.item_name);
        TextView price = (TextView) view.findViewById(R.id.item_price);
        name.setText(item.getName());
        NumberFormat format = NumberFormat.getCurrencyInstance();
        price.setText(format.format(item.getBasePrice()));
        ImageButton remove = (ImageButton) view.findViewById(R.id.remove_button);
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: get the removal button working properly.
                v.setVisibility(View.GONE);
                items.remove(getGroup(groupPosition));
            }
        });
        return view;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View view, ViewGroup parent) {
        // TODO: figure out how to get actual options not just  a list of optiongroups.
        ItemOption option = (ItemOption) getChild(groupPosition, childPosition);
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.order_list_item, null);
        }
        TextView name = (TextView) view.findViewById(R.id.item_name);
        TextView price = (TextView) view.findViewById(R.id.item_price);
        name.setText(option.getOptionName());
        NumberFormat format = NumberFormat.getCurrencyInstance();
        price.setText(format.format(25.00));
        ImageButton remove = (ImageButton) view.findViewById(R.id.remove_button);
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: get the removal button working properly.
                v.setVisibility(View.GONE);
                items.get(groupPosition).getOptions().remove(childPosition);
            }
        });
        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
