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

/**
 * Created by Ruth on 4/7/2017.
 */

public class MonthlyReportArrayAdapter extends ArrayAdapter<DisplayItem> {
    private final Context context;
    private final ArrayList<DisplayItem> items;
    private final String month;
    private final String year;
    private final double rate = .05;

    public MonthlyReportArrayAdapter(Context context, ArrayList<DisplayItem> items, String month, String year) {
        super(context, R.layout.monthly_report_layout, items);
        this.context = context;
        this.items = items;
        this.month = month;
        this.year = year;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.monthly_report_layout, parent, false);

        TextView tvName = (TextView) rowView.findViewById(R.id.rest_name);
        TextView tvMonth = (TextView) rowView.findViewById(R.id.month_to_display);
        TextView tvTotalSales = (TextView) rowView.findViewById(R.id.sales_amount);
        TextView tvRefund = (TextView) rowView.findViewById(R.id.refund_amount);
        TextView tvPayment = (TextView) rowView.findViewById(R.id.pay_amount);
        TextView tvNet = (TextView) rowView.findViewById(R.id.total_amount);

        String restaurantName = items.get(position).getName();
        String dateToDisplay = month + " " + year;
        double completeSalesAmt = items.get(position).getTotal();
        double refundAmt = items.get(position).getBasePrice();
        double totalSales = completeSalesAmt + refundAmt;
        double paymentAmt = completeSalesAmt * rate;
        double netAmt = completeSalesAmt - paymentAmt;

        tvName.setText(restaurantName);
        tvMonth.setText(dateToDisplay);
        //this line displays the completed orders + refunded orders for the total sales
        tvTotalSales.setText(formatAmount(totalSales));
        //this line displays refunds
        tvRefund.setText(formatAmount(refundAmt));
        //this line displays payment
        tvPayment.setText(formatAmount(paymentAmt));
        //this line is total sales - refund - payment
        tvNet.setText(formatAmount(netAmt));

        return rowView;
    }

    private String formatAmount(double d){
        return "$" + String.format(Locale.US, "%.2f", d);
    }
}
