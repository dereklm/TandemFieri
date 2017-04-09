package com.gmail.dleemcewen.tandemfieri;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.dleemcewen.tandemfieri.Adapters.OrdersListAdapterAddress;
import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.Enums.OrderEnum;
import com.gmail.dleemcewen.tandemfieri.Logging.LogWriter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;


public class CompetedOrdersDriverActivity extends AppCompatActivity implements DatePickerFragment.DateListener{
    private int viewId;
    private User user;
    private String customerId;
    private DatabaseReference mDatabaseDelivery;
    private TextView fromDate;
    private TextView toDate;
    private Date date_from;
    private Date date_to;
    private DateListener dateListener;
    private Button showButton;
    private ShowButtonListener showButtonListener;
    private Context context;
    private ListView ordersListView;
    private List<Order> entities;
    private OrdersListAdapterAddress listAdapter;
    private DatabaseReference mDatabaseRemoval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_competed_orders_driver);

        Bundle bundle = this.getIntent().getExtras();
        user = (User) bundle.getSerializable("User");

        dateListener = new DateListener();
        showButtonListener = new ShowButtonListener();

        mDatabaseRemoval = FirebaseDatabase.getInstance().getReference();

        context = this;

        ordersListView = (ListView) findViewById(R.id.orders);
        ordersListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                setNotComplete((Order)listAdapter.getItem(i));
                return true;
            }
        });

        LogWriter.log(getApplicationContext(), Level.INFO, "The user is " + user.getAuthUserID());
        mDatabaseDelivery = FirebaseDatabase.getInstance().getReference().child("Delivery").child(user.getAuthUserID()).child("Order");

        mDatabaseDelivery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                entities = new ArrayList<Order>();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    for (DataSnapshot child2 : child.getChildren()) {
                        entities.add(child2.getValue(Order.class));
                    }
                    //Toast.makeText(getApplicationContext(), ""+child.child("Order").getValue(), Toast.LENGTH_LONG).show();
                    //order = child.child("Order").getValue(Order.class);
                }
                loadList();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        fromDate = (TextView)findViewById(R.id.from_date);
        toDate = (TextView)findViewById(R.id.to_date);
        showButton = (Button)findViewById(R.id.show_product_history);
        fromDate.setOnClickListener(dateListener);
        toDate.setOnClickListener(dateListener);
        showButton.setOnClickListener(showButtonListener);


    }//end onCreate

    private void setNotComplete(Order item) {
        final Order temp = item;
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        temp.setStatus(OrderEnum.EN_ROUTE);
                        mDatabaseRemoval.child("Delivery").child(user.getAuthUserID()).child("Order").child(temp.getCustomerId()).child(temp.getOrderId()).child("status").setValue(OrderEnum.EN_ROUTE);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
                dialog.dismiss();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you wish to mark this order as not complete?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }


    private void loadList() {
        List<Order> toShow = new ArrayList<>();

        for (Order o : entities) {
            if (o.getStatus() == OrderEnum.COMPLETE){
                if(date_to!=null&&date_to.after(o.getOrderDate())) {
                    if (date_from != null && date_from.before(o.getOrderDate()))
                        toShow.add(o);
                    else if (date_from == null) toShow.add(o);
                }
                else if (date_to==null&&date_from!=null  && date_from.before(o.getOrderDate()))
                    toShow.add(o);
                else if( date_to==null && date_from==null) toShow.add(o);
            }
        }
        listAdapter = new OrdersListAdapterAddress(context, toShow);
        listAdapter.setUseDate(true);
        ordersListView.setAdapter(listAdapter);
    }

    private boolean datesVerified(){
        //verify the start date is not after the end date
        Date current = new Date();
        if (date_from == null || date_to == null){
            Toast.makeText(getApplicationContext(), "Please enter valid dates.", Toast.LENGTH_LONG).show();
            return false;
        }
        if(date_from.after(date_to)){
            Toast.makeText(getApplicationContext(), "From date cannot be after To date.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onFinishDialog(Date date) {
        Date current = new Date();
        SimpleDateFormat formatDateJava = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        Calendar cal = Calendar.getInstance();
        if(viewId == fromDate.getId()){
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, cal.getMinimum(Calendar.MINUTE));
            cal.set(Calendar.SECOND, cal.getMinimum(Calendar.SECOND));
            cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
            date_from = cal.getTime();
            fromDate.setText(formatDateJava.format(date_from));
        }else if(viewId == toDate.getId()){
            if(date.after(current)){
                cal.setTime(current);
                cal.set(Calendar.HOUR_OF_DAY, cal.getMaximum(Calendar.HOUR_OF_DAY));
                cal.set(Calendar.MINUTE, cal.getMaximum(Calendar.MINUTE));
                cal.set(Calendar.SECOND, cal.getMaximum(Calendar.SECOND));
                cal.set(Calendar.MILLISECOND, cal.getMaximum(Calendar.MILLISECOND));
                date_to= cal.getTime();
            }else{
                cal.setTime(date);
                cal.set(Calendar.HOUR_OF_DAY, cal.getMaximum(Calendar.HOUR_OF_DAY));
                cal.set(Calendar.MINUTE, cal.getMaximum(Calendar.MINUTE));
                cal.set(Calendar.SECOND, cal.getMaximum(Calendar.SECOND));
                cal.set(Calendar.MILLISECOND, cal.getMaximum(Calendar.MILLISECOND));
                date_to= cal.getTime();
            }
            toDate.setText(formatDateJava.format(date_to));
        }
    }

    public class DateListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            //begin date picker
            viewId = view.getId();
            DialogFragment fragment = new DatePickerFragment();
            fragment.show(getFragmentManager(), "datePicker");
        }
    }

    public class ShowButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (datesVerified()) {
                loadList();
            } else {
                Toast.makeText(getApplicationContext(), "Please enter a valid date range.", Toast.LENGTH_LONG);
            }
        }
    }
}
