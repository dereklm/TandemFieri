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
import com.gmail.dleemcewen.tandemfieri.Entities.OrderItemOption;
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

    public void setItems(List<OrderItem> itemList) {
        this.items = itemList;
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

        TextView name = (TextView) convertView.findViewById(R.id.order_item_name);
        name.setText(item.getName());
        TextView price = (TextView) convertView.findViewById(R.id.item_price);
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        price.setText(formatter.format(item.getBasePrice()));

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        //List<OrderItemOptionGroup> groups = items.get(groupPosition).getOptionGroups();
        //LogWriter.log(context, Level.WARNING, "size of groups " + groups.size() + " for item " + ((OrderItem) getGroup(groupPosition)).getName());
        final OrderItemOptionGroup group = (OrderItemOptionGroup) getChild(groupPosition, childPosition);
        final ExpandableListView groupsListView;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.order_item_group, null);
        }

        groupsListView = (ExpandableListView) convertView.findViewById(R.id.item_groups);
        OrderGroupAdapter orderGroupAdapter = new OrderGroupAdapter(activity, context, group);
        groupsListView.setAdapter(orderGroupAdapter);

        if (group.isExclusive()) groupsListView.setChoiceMode(ExpandableListView.CHOICE_MODE_SINGLE);
        else groupsListView.setChoiceMode(ExpandableListView.CHOICE_MODE_MULTIPLE);

        // group expand click
        groupsListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                int height = 0;
                for (int i = 0; i < groupsListView.getChildCount(); i++) {
                    height += groupsListView.getChildAt(i).getMeasuredHeight();
                    height += groupsListView.getDividerHeight();
                }
                groupsListView.getLayoutParams().height = (height+6)*(3);
            }
        });

        // Listview Group collapsed listener
        groupsListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
                groupsListView.getLayoutParams().height = 100;
            }
        });

        // set option click listener.
        groupsListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                    OrderItemOption option = group.getOptions().get(childPosition);
                    if (option.isSelected()) {
                        option.setSelected(false);
                        parent.setItemChecked(childPosition, false);
                        v.setActivated(false);

                        // if option group was exclusive set hasChildSelected to false and deselect all children.
                        if (group.isExclusive()) {
                            group.setHasChildSelected(false);
                            for (OrderItemOption opt : group.getOptions()) {
                                opt.setSelected(false);
                            }
                        } else {
                            // option group may be required but not exclusive, find if there are any selected.
                            //  set hasChildSelected based on results.
                            int numSelected = 0;
                            for (OrderItemOption opt : group.getOptions()) {
                                if (opt.isSelected()) numSelected++;
                            }
                            if (numSelected == 0) {
                                group.setHasChildSelected(false);
                            }
                        }
                    }
                    else {
                        option.setSelected(true);
                        parent.setItemChecked(childPosition, true);

                        // if option group was exclusive, deselect all other children.
                        if (group.isExclusive()) {
                            for (OrderItemOption opt : group.getOptions()) {
                                if ((opt.getName() != option.getName()) && opt.isSelected()) {
                                    opt.setSelected(false);
                                    break;
                                }
                            }
                        }

                        group.setHasChildSelected(true);
                    }
                    return true;
                }
                return false;
            }
        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
