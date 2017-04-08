package com.gmail.dleemcewen.tandemfieri.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.gmail.dleemcewen.tandemfieri.DisplayItem;
import com.gmail.dleemcewen.tandemfieri.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Ruth on 4/7/2017.
 */

public class MonthlyReportArrayAdapter extends ArrayAdapter<DisplayItem> {
    private final Context context;
    private final ArrayList<DisplayItem> values;
    private final String month;
    private final String year;
    private final double payment = 100.00;

    public MonthlyReportArrayAdapter(Context context, ArrayList<DisplayItem> values, String month, String year) {
        super(context, R.layout.monthly_report_layout, values);
        this.context = context;
        this.values = values;
        this.month = month;
        this.year = year;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.monthly_report_layout, parent, false);

        SimpleDateFormat formatDateJava = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

        TextView tvName = (TextView) rowView.findViewById(R.id.rest_name);
        TextView tvMonth = (TextView) rowView.findViewById(R.id.month_to_display);
        TextView tvTotal = (TextView) rowView.findViewById(R.id.sales_amount);
        TextView tvPayment = (TextView) rowView.findViewById(R.id.pay_amount);

        tvName.setText(values.get(position).getName());
        tvMonth.setText(month + " " + year);
        tvTotal.setText(formatAmount(values.get(position).getTotal()));
        tvPayment.setText(formatAmount(payment));

        return rowView;
    }

    private String formatAmount(double d){
        return "$" + String.format(Locale.US, "%.2f", d);
    }
}
