package com.gmail.dleemcewen.tandemfieri.Repositories;

import android.content.Context;
import android.support.annotation.NonNull;

import com.gmail.dleemcewen.tandemfieri.Abstracts.Entity;
import com.gmail.dleemcewen.tandemfieri.Abstracts.Repository;
import com.gmail.dleemcewen.tandemfieri.Builders.QueryBuilder;
import com.gmail.dleemcewen.tandemfieri.Constants.QueryConstants;
import com.gmail.dleemcewen.tandemfieri.Entities.Delivery;
import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.EventListeners.QueryCompleteListener;
import com.gmail.dleemcewen.tandemfieri.Query.ParsedQuery;
import com.gmail.dleemcewen.tandemfieri.Tasks.FindEntitiesTask;
import com.gmail.dleemcewen.tandemfieri.Tasks.GetEntitiesTask;
import com.gmail.dleemcewen.tandemfieri.Tasks.NetworkConnectivityCheckTask;
import com.gmail.dleemcewen.tandemfieri.Tasks.TaskResult;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Deliveries repository defines the database logic to use when adding, removing, or updating a Delivery
 */

public class Deliveries<T extends Entity> extends Repository<Delivery> {
    private Context context;

    /**
     * Default constructor
     * @param context indicates the current application context
     */
    public Deliveries(Context context) {
        super(context);
        this.context = context;
    }

    /**
     * find entities from the database
     * @param queryString identifies the query string to send to the database
     *                     queryString is either in the form of "<field> = <value>" or "<field> between <value1> and <value2>"
     * @return all entities that match the provided query string
     */
    public Task<TaskResult<Order>> findOrders(String queryString) {
        String[] childNodes = new String[searchNodes.size()];
        childNodes = searchNodes.toArray(childNodes);

        //explictly set the querystring to an empty string if it is null to prevent errors
        if (queryString == null) {
            queryString = "";
        }

        dataContext = getDataContext(Delivery.class.getSimpleName(), childNodes);
        final ParsedQuery query = QueryBuilder.build(dataContext, queryString);

        if (!query.getQueryType().equals(QueryConstants.QueryType.NOTEQUALS)) {
            return Tasks.<Void>forResult(null)
                    .continueWithTask(new NetworkConnectivityCheckTask(context))
                    .continueWithTask(new FindEntitiesTask<>(context, Order.class, query.getQuery()))
                    .continueWith(new Continuation<Map.Entry<List<Order>, DatabaseError>, TaskResult<Order>>() {
                        @Override
                        public TaskResult<Order> then(@NonNull Task<Map.Entry<List<Order>, DatabaseError>> task) throws Exception {
                            TaskCompletionSource<TaskResult<Order>> taskCompletionSource =
                                    new TaskCompletionSource<>();

                            Map.Entry<List<Order>, DatabaseError> taskResult = task.getResult();
                            taskCompletionSource.setResult(new TaskResult<>("find", taskResult.getKey(), taskResult.getValue()));

                            //Clear after find complete
                            searchNodes.clear();

                            return taskCompletionSource.getTask().getResult();
                        }
                    });
        } else {
            return Tasks.<Void>forResult(null)
                    .continueWithTask(new NetworkConnectivityCheckTask(context))
                    .continueWithTask(new GetEntitiesTask<>(dataContext, Order.class))
                    .continueWith(new Continuation<Map.Entry<List<Order>, DatabaseError>, TaskResult<Order>>() {
                        @Override
                        public TaskResult<Order> then(@NonNull Task<Map.Entry<List<Order>, DatabaseError>> task) throws Exception {
                            TaskCompletionSource<TaskResult<Order>> taskCompletionSource =
                                    new TaskCompletionSource<>();

                            Map.Entry<List<Order>, DatabaseError> taskResult = task.getResult();

                            List<Order> notEqualItems = new ArrayList<>();
                            for (Order item : taskResult.getKey()) {
                                Field field = item.getClass().getDeclaredField(query.getField());
                                field.setAccessible(true);
                                if (!field.get(item).toString().equals(query.values.get(0).toString())) {
                                    notEqualItems.add(item);
                                }
                            }

                            taskCompletionSource.setResult(new TaskResult<>("find", notEqualItems, taskResult.getValue()));

                            //Clear after find complete
                            searchNodes.clear();

                            return taskCompletionSource.getTask().getResult();
                        }
                    });
        }
    }

    /**
     * findDriverIdForOrder finds the driver id associated with the given order
     * @param orderId uniquely identifies the order
     * @param deliveries indicates the deliveries to search through
     * @return the matching driver id
     */
    public String findDriverIdForOrder(final String orderId, final DataSnapshot deliveries) {
        String foundDriverId = "";

        for (DataSnapshot drivers : deliveries.getChildren())
        {
            String driverId = drivers.getKey();

            for (DataSnapshot customer : drivers.child("Order").getChildren())
            {
                for (DataSnapshot order : customer.getChildren())
                {
                    Order orderEntity = order.getValue(Order.class);
                    if (orderEntity.getOrderId().equals(orderId)) {
                        foundDriverId = driverId;
                    }
                }
            }
        }

        return foundDriverId;
    }

    /**
     * find entities from the database
     *
     * @param childNodes      identifies the list of string arguments that indicates the
     *                        child node(s) that identify the location of the desired data
     * @param value           indicates the data value to search for
     * @param onQueryComplete identifies the QueryCompleteListener to push results back to
     */
    @Override
    public void find(List<String> childNodes, String value, QueryCompleteListener<Delivery> onQueryComplete) {

    }

    /**
     * find entities from the database
     *
     * @param childNodes identifies the list of string arguments that indicates the
     *                   child node(s) that identify the location of the desired data
     * @param value      indicates the data value to search for
     * @return Task containing the results of the find that can be chained to other tasks
     */
    @Override
    public Task<ArrayList<Delivery>> find(List<String> childNodes, String value) {
        return null;
    }
}
