package com.gmail.dleemcewen.tandemfieri.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.gmail.dleemcewen.tandemfieri.Entities.DisplayItem;
import com.gmail.dleemcewen.tandemfieri.R;

import java.util.ArrayList;
import java.util.Locale;

import static com.gmail.dleemcewen.tandemfieri.R.id.product_history_quantity;
import static com.gmail.dleemcewen.tandemfieri.R.id.total;

/**
 * Created by Ruth on 3/16/2017.
 */

public class ProductHistoryArrayAdapter extends ArrayAdapter<DisplayItem> {

    private final Context context;
    private final ArrayList<DisplayItem> values;

    public ProductHistoryArrayAdapter(Context context, ArrayList<DisplayItem> values) {
        super(context, R.layout.product_history_item_view, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.product_history_item_view, parent, false);

        TextView tvName = (TextView) rowView.findViewById(R.id.product_name);
        TextView tvBase = (TextView) rowView.findViewById(R.id.base_price);
        TextView tvTotal = (TextView) rowView.findViewById(total);
        TextView tvQuantity = (TextView) rowView.findViewById(product_history_quantity);

        tvName.setText(values.get(position).getName());
        tvQuantity.setText(formatQuantity(values.get(position).getQuantity()));
        tvBase.setText(formatAmount(values.get(position).getBasePrice()));
        tvTotal.setText(formatAmount(values.get(position).getTotal()));

        return rowView;
    }

    private String formatAmount(double d){
        return "$" + String.format(Locale.US, "%.2f", d);
    }
    private String formatQuantity(int i) { return String.valueOf(i); }

}//end class
