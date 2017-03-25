package com.gmail.dleemcewen.tandemfieri.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import android.widget.TextView;

import com.gmail.dleemcewen.tandemfieri.DriverOrdersFragment;
import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * DriverOrdersListAdapter provides the required methods to render the
 * listview used in the driver orders activity
 */
public class DriverOrdersListAdapter extends BaseAdapter {
    private Activity context;
    private Resources resources;
    private List<Order> ordersList;
    private DriverOrdersFragment parentFragment;
    private int selectedIndex;

    /**
     * Default constructor
     * @param context indicates the activity context
     * @param ordersList identifies the list of orders
     */
    public DriverOrdersListAdapter(Activity context, DriverOrdersFragment parentFragment, List<Order> ordersList) {
        this.context = context;
        resources = context.getResources();
        this.ordersList = ordersList;
        this.parentFragment = parentFragment;

        selectedIndex = -1;
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return ordersList.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return ordersList.get(position);
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Indicates whether the child and group IDs are stable across changes to the
     * underlying data.
     *
     * @return whether or not the same ID always refers to the same object
     */
    @Override
    public boolean hasStableIds() {
        return false;
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Order order = (Order)getItem(position);

        LayoutInflater layoutInflater = (LayoutInflater) this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.driver_order_list_item, null);
        }

        final TextView orderInformation = (TextView)convertView.findViewById(R.id.driverOrderItem);
        setOrderText(orderInformation, position);

        orderInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentFragment.setSelectedIndex(position);
            }
        });

        return convertView;
    }

    /**
     * setSelectedIndex sets the selected index of the item in the list
     * @param index indicates the selected index
     */
    public void setSelectedIndex(int index) {
        selectedIndex = index;
    }

    private void setOrderText(TextView orderTextView, int position) {
        Order order = (Order)getItem(position);
        StringBuilder orderInformationBuilder = new StringBuilder();
        SimpleDateFormat formatOrderDate = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

        if (selectedIndex == position) {
            orderInformationBuilder.append("Order date: ");
            orderInformationBuilder.append(formatOrderDate.format(order.getOrderDate()));
            orderInformationBuilder.append("\r\n");
            orderInformationBuilder.append("Items: ");
            orderInformationBuilder.append(order.getItems().size());
            orderInformationBuilder.append(", ");
            orderInformationBuilder.append("Total: ");
            orderInformationBuilder.append(String.format("$%.2f", order.getTotal()));
            orderInformationBuilder.append("\r\n");
            orderInformationBuilder.append("(");
            for (int index = 0; index < order.getItems().size(); index++) {
                orderInformationBuilder.append(order.getItems().get(index).getName());
                if (index < order.getItems().size() - 1) {
                    orderInformationBuilder.append(", ");
                }
            }
            orderInformationBuilder.append(")");
        } else {
            orderInformationBuilder.append("Order date: ");
            orderInformationBuilder.append(formatOrderDate.format(order.getOrderDate()));
            orderInformationBuilder.append("\r\n");
            orderInformationBuilder.append("Items: ");
            orderInformationBuilder.append(order.getItems().size());
            orderInformationBuilder.append(", ");
            orderInformationBuilder.append("Total: ");
            orderInformationBuilder.append(String.format("$%.2f", order.getTotal()));
        }

        orderTextView.setText(orderInformationBuilder.toString());
        setOrderTextViewBackgroundColor(orderTextView, position);
    }
    private void setOrderTextViewBackgroundColor(TextView orderTextView, int position) {
        if (selectedIndex == position) {
            orderTextView.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            orderTextView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark));
        } else {
            orderTextView.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
            orderTextView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        }
    }
}

