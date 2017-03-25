package com.gmail.dleemcewen.tandemfieri.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import static com.gmail.dleemcewen.tandemfieri.R.id.total;

/**
 * Created by Ruth on 3/24/2017.
 */

public class DinerOrderHistoryArrayAdapter extends ArrayAdapter<Order> {
    private final Context context;
    private final ArrayList<Order> values;

    public DinerOrderHistoryArrayAdapter(Context context, ArrayList<Order> values) {
        super(context, R.layout.diner_order_history_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.diner_order_history_item, parent, false);

        SimpleDateFormat formatDateJava = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

        TextView tvDate = (TextView) rowView.findViewById(R.id.order_date);
        TextView tvName = (TextView) rowView.findViewById(R.id.restaurant_name);
        TextView tvTotal = (TextView) rowView.findViewById(total);

        tvDate.setText(formatDateJava.format(values.get(position).getOrderDate()));
        tvName.setText(values.get(position).getRestaurantName());
        tvTotal.setText(formatAmount(values.get(position).getTotal()));

        return rowView;
    }

    private String formatAmount(double d){
        return "$" + String.format(Locale.US, "%.2f", d);
    }

}//end class
