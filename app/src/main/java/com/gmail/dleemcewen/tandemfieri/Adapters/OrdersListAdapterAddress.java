package com.gmail.dleemcewen.tandemfieri.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

/**
 * Created by jfly1_000 on 3/14/2017.
 */

public class OrdersListAdapterAddress extends BaseAdapter {

    private final Context context;
    private final List<Order> values;
    private DatabaseReference mDatabaseDelivery;
    private boolean useDate = false;

    public OrdersListAdapterAddress(Context context, List<Order> values){
        super();
        this.context=context;
        this.values=values;
    }

    @Override
    public int getCount() {
        if(values!=null)return values.size();
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return values.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView=inflater.inflate(R.layout.order_address_row,parent,false);
        Order order = (Order) getItem(position);
        TextView totalItems = (TextView) rowView.findViewById(R.id.totalItems);
        if(!useDate)totalItems.setText(order.getStatus().toString());
        else totalItems.setText(order.getOrderDate().toString());
        mDatabaseDelivery = FirebaseDatabase.getInstance().getReference().child("User").child("Diner").child(order.getCustomerId());
        final TextView address = (TextView) rowView.findViewById(R.id.address);

        mDatabaseDelivery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                address.setText(dataSnapshot.child("address").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        return rowView;
    }


    public boolean isUseDate() {
        return useDate;
    }

    public void setUseDate(boolean useDate) {
        this.useDate = useDate;
    }
}