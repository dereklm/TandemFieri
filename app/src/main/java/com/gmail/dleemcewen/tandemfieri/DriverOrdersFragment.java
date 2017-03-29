package com.gmail.dleemcewen.tandemfieri;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.gmail.dleemcewen.tandemfieri.Adapters.DriverOrdersListAdapter;
import com.gmail.dleemcewen.tandemfieri.Entities.Delivery;
import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.Enums.OrderEnum;
import com.gmail.dleemcewen.tandemfieri.Repositories.Deliveries;
import com.gmail.dleemcewen.tandemfieri.Tasks.TaskResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class DriverOrdersFragment extends DialogFragment {
    private String driverId;
    private String restaurantId;
    private String customerId;
    private RelativeLayout myDeliveriesLayout;
    private ListView myDeliveriesList;
    private DriverOrdersListAdapter listAdapter;
    private Button closeMyOrders;
    private Button selectCurrentDelivery;
    private Deliveries<Delivery> deliveriesRepository;
    private List<Order> driverOrders;
    private Order currentOrder, order;

    /**
     * Default constructor
     */
    public DriverOrdersFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        deliveriesRepository = new Deliveries<>(getActivity());


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_driver_orders, container, false);

        initialize(view);
        findControlReferences(view);
        bindEventHandlers();
        retrieveData();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * initialize all necessary variables
     */
    private void initialize(View view) {
        if (getArguments() != null) {
            driverId = getArguments().getString("driverId");
            restaurantId = getArguments().getString("restaurantId");
            customerId = getArguments().getString("customerId");
        }

        driverOrders = new ArrayList<>();
    }

    /**
     * find all control references
     */
    private void findControlReferences(View view) {
        myDeliveriesLayout = (RelativeLayout)view.findViewById(R.id.myDeliveriesLayout);
        myDeliveriesList = (ListView)myDeliveriesLayout.findViewById(R.id.myDeliveriesList);
        selectCurrentDelivery = (Button)myDeliveriesLayout.findViewById(R.id.selectCurrentDelivery);
    }

    /**
     * bind required event handlers
     */
    private void bindEventHandlers() {
        selectCurrentDelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase
                    .getInstance()
                    .getReference(Delivery.class.getSimpleName())
                    .child(driverId)
                    .child("currentOrderId")
                    .setValue(((DriverMainMenu)getActivity()).getCurrentOrder().getOrderId());

                Toast.makeText(getActivity().getApplicationContext(), "Order Selected.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * retrieve data
     */
    private void retrieveData() {
        deliveriesRepository = (Deliveries<Delivery>) deliveriesRepository
                .atNode(driverId)
                .atNode("Order")
                .atNode(customerId);

        deliveriesRepository
                .findOrders("status != '" + OrderEnum.COMPLETE.toString() + "'")
                .addOnCompleteListener(getActivity(), new OnCompleteListener<TaskResult<Order>>() {
                    @Override
                    public void onComplete(@NonNull Task<TaskResult<Order>> task) {
                        List<Order> orders = task.getResult().getResults();
                        driverOrders.clear();

                        if (!orders.isEmpty()) {
                            driverOrders.addAll(orders);
                        }

                        //bind the orders to the listview
                        listAdapter = new DriverOrdersListAdapter(getActivity(), DriverOrdersFragment.this, driverOrders);
                        myDeliveriesList.setAdapter(listAdapter);
                    }
                });
    }

    /**
     * setSelectedIndex sets the selected index of the item in the list
     * @param index indicates the selected index
     */
    public void setSelectedIndex(int index) {
        listAdapter.setSelectedIndex(index);
        listAdapter.notifyDataSetChanged();
        ((DriverMainMenu)getActivity()).setCurrentOrder(driverOrders.get(index));
    }
}
