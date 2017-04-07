package com.gmail.dleemcewen.tandemfieri.Adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.gmail.dleemcewen.tandemfieri.CreateDeliveryHoursActivity;
import com.gmail.dleemcewen.tandemfieri.DriverRatings;
import com.gmail.dleemcewen.tandemfieri.EditRestaurantActivity;
import com.gmail.dleemcewen.tandemfieri.Entities.DeliveryHours;
import com.gmail.dleemcewen.tandemfieri.Entities.Restaurant;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.ManageRestaurantDrivers;
import com.gmail.dleemcewen.tandemfieri.ProductHistoryActivity;
import com.gmail.dleemcewen.tandemfieri.R;
import com.gmail.dleemcewen.tandemfieri.Repositories.Restaurants;
import com.gmail.dleemcewen.tandemfieri.RestaurantMapActivity;
import com.gmail.dleemcewen.tandemfieri.menubuilder.MenuBuilderActivity;
import com.gmail.dleemcewen.tandemfieri.menubuilder.MenuCatagory;
import com.gmail.dleemcewen.tandemfieri.menubuilder.MenuIterator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * ManageRestaurantExpandableListAdapter provides the required methods to render the expandable
 * listview used in the manage restaurant activity
 */

public class ManageRestaurantExpandableListAdapter extends BaseExpandableListAdapter {
    private Activity context;
    private List<Restaurant> restaurantsList;
    private Map<String, List<Restaurant>> childDataList;
    private Resources resources;
    private Restaurants<Restaurant> restaurantsRepository;
    private static final int UPDATE_RESTAURANT = 2;
    DatabaseReference mDatabase;
    private User user;

    public ManageRestaurantExpandableListAdapter(Activity context, List<Restaurant> restaurantsList,
                                                 Map<String, List<Restaurant>> childDataList, User currentUser) {
        this.context = context;
        this.restaurantsList = restaurantsList;
        this.childDataList = childDataList;
        resources = context.getResources();
        restaurantsRepository = new Restaurants<>(context);
        user = currentUser;
    }

    /**
     * Gets the number of groups.
     * @return the number of groups
     */
    @Override
    public int getGroupCount() {
        return restaurantsList.size();
    }

    /**
     * Gets the number of children in a specified group.
     *
     * @param groupPosition the position of the group for which the children
     *                      count should be returned
     * @return the children count in the specified group
     */
    @Override
    public int getChildrenCount(int groupPosition) {
        return childDataList.get(restaurantsList.get(groupPosition).getKey()).size();
    }

    /**
     * Gets the data associated with the given group.
     *
     * @param groupPosition the position of the group
     * @return the data child for the specified group
     */
    @Override
    public Object getGroup(int groupPosition) {
        return restaurantsList.get(groupPosition);
    }

    /**
     * Gets the data associated with the given child within the given group.
     *
     * @param groupPosition the position of the group that the child resides in
     * @param childPosition the position of the child with respect to other
     *                      children in the group
     * @return the data of the child
     */
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childDataList.get(restaurantsList.get(groupPosition).getKey()).get(childPosition);
    }

    /**
     * Gets the ID for the group at the given position. This group ID must be
     * unique across groups. The combined ID (see
     * {@link #getCombinedGroupId(long)}) must be unique across ALL items
     * (groups and all children).
     *
     * @param groupPosition the position of the group for which the ID is wanted
     * @return the ID associated with the group
     */
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    /**
     * Gets the ID for the given child within the given group. This ID must be
     * unique across all children within the group. The combined ID (see
     * {@link #getCombinedChildId(long, long)}) must be unique across ALL items
     * (groups and all children).
     *
     * @param groupPosition the position of the group that contains the child
     * @param childPosition the position of the child within the group for which
     *                      the ID is wanted
     * @return the ID associated with the child
     */
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
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
     * Gets a View that displays the given group. This View is only for the
     * group--the Views for the group's children will be fetched using
     * {@link #getChildView(int, int, boolean, View, ViewGroup)}.
     *
     * @param groupPosition the position of the group for which the View is
     *                      returned
     * @param isExpanded    whether the group is expanded or collapsed
     * @param convertView   the old view to reuse, if possible. You should check
     *                      that this view is non-null and of an appropriate type before
     *                      using. If it is not possible to convert this view to display
     *                      the correct data, this method can create a new view. It is not
     *                      guaranteed that the convertView will have been previously
     *                      created by
     *                      {@link #getGroupView(int, boolean, View, ViewGroup)}.
     * @param parent        the parent that this view will eventually be attached to
     * @return the View corresponding to the group at the specified position
     */
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final Restaurant restaurant = (Restaurant)getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.manage_restaurants_list_group_header, null);
        }

        TextView restaurantName = (TextView)convertView.findViewById(R.id.restaurantName);
        restaurantName.setText(restaurant.getName());
        restaurantName.setTypeface(null, Typeface.BOLD);
        restaurantName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("Restaurant", restaurant);
                bundle.putString("key", restaurant.getId());
                Intent intent = new Intent(context, EditRestaurantActivity.class);
                intent.putExtras(bundle);
                context.startActivityForResult(intent, UPDATE_RESTAURANT);
            }
        });

        BootstrapButton removeRestaurant = (BootstrapButton) convertView.findViewById(R.id.remove);
        removeRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder dialogMessageBuilder = new StringBuilder();
                dialogMessageBuilder.append(resources.getString(R.string.removeConfirmationQuestion));
                dialogMessageBuilder.append(" ");
                dialogMessageBuilder.append(restaurant.getName());
                dialogMessageBuilder.append("?");

                AlertDialog.Builder removalConfirmationDialog  = new AlertDialog.Builder(context);
                removalConfirmationDialog
                        .setMessage(dialogMessageBuilder.toString());
                removalConfirmationDialog
                        .setTitle(resources.getString(R.string.manageRestaurantsActivityTitle));
                removalConfirmationDialog.setCancelable(false);
                removalConfirmationDialog.setPositiveButton(
                        resources.getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //find the corresponding delivery hours and remove
                                removeDeliveryHours(restaurant.getId());

                                restaurantsList.remove(restaurant);
                                childDataList.remove(Arrays.asList(restaurant));
                                restaurantsRepository.remove(restaurant);
                                notifyDataSetChanged();

                                dialog.cancel();
                            }
                        });
                removalConfirmationDialog.setNegativeButton(
                        resources.getString(R.string.no),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                removalConfirmationDialog
                        .create()
                        .show();
            }
        });

        return convertView;
    }

    /**
     * Gets a View that displays the data for the given child within the given
     * group.
     *
     * @param groupPosition the position of the group that contains the child
     * @param childPosition the position of the child (for which the View is
     *                      returned) within the group
     * @param isLastChild   Whether the child is the last child within the group
     * @param convertView   the old view to reuse, if possible. You should check
     *                      that this view is non-null and of an appropriate type before
     *                      using. If it is not possible to convert this view to display
     *                      the correct data, this method can create a new view. It is not
     *                      guaranteed that the convertView will have been previously
     *                      created by
     *                      {@link #getChildView(int, int, boolean, View, ViewGroup)}.
     * @param parent        the parent that this view will eventually be attached to
     * @return the View corresponding to the child at the specified position
     */
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final Restaurant selectedChild = (Restaurant) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.manage_restaurants_list_item, null);
        }

        BootstrapButton manageMenuItems = (BootstrapButton)convertView.findViewById(R.id.manageMenuItems);
        BootstrapButton viewSales = (BootstrapButton)convertView.findViewById(R.id.viewSales);
        BootstrapButton viewDeliveryArea = (BootstrapButton)convertView.findViewById(R.id.viewDeliveryArea);
        BootstrapButton manageDrivers = (BootstrapButton)convertView.findViewById(R.id.manageDrivers);
        BootstrapButton rateDrivers = (BootstrapButton)convertView.findViewById(R.id.rateDrivers);
        BootstrapButton deliveryHours = (BootstrapButton)convertView.findViewById(R.id.deliveryHours);


        manageMenuItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MenuBuilderActivity.class);
                if (selectedChild.getMenu()==null){
                    selectedChild.setMenu(new MenuCatagory("Main"));
                }

                MenuIterator iterator = MenuIterator.create(); //set up iterator

                try {
                    iterator.setRestaurant(selectedChild);
                } catch (Exception e) {
                    Toast
                            .makeText(context, e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                }

                context.startActivity(intent);

                Toast
                        .makeText(context, "Managing menu items for " + selectedChild.getName(), Toast.LENGTH_SHORT)
                        .show();
            }
        });
        viewSales.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ProductHistoryActivity.class);
                intent.putExtra("restaurant", selectedChild);
                intent.putExtra("ID", user.getAuthUserID());
                context.startActivity(intent);
            }
        });
        viewDeliveryArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, RestaurantMapActivity.class);
                intent.putExtra("restaurant", selectedChild);
                context.startActivity(intent);
            }
        });
        manageDrivers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("Restaurant", selectedChild);
                bundle.putString("key", selectedChild.getKey());
                Intent intent = new Intent(context, ManageRestaurantDrivers.class);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
        rateDrivers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Android assigns a new key to the restaurant when deserializing
                //so send the existing key along as well so the new key can be replaced
                Bundle bundle = new Bundle();
                Intent intent = new Intent(context, DriverRatings.class);
                bundle.putSerializable("Restaurant", selectedChild);
                bundle.putString("key", selectedChild.getKey());
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
        deliveryHours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CreateDeliveryHoursActivity.class);
                intent.putExtra("restId",selectedChild.getKey());
                intent.putExtra("editOrCreate", "edit");
                context.startActivity(intent);
            }
        });


        return convertView;
    }

    /**
     * Whether the child at the specified position is selectable.
     *
     * @param groupPosition the position of the group that contains the child
     * @param childPosition the position of the child within the group
     * @return whether the child is selectable.
     */
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public Restaurant findRefrenceToUpdate(Restaurant restaurant) {
        for (Restaurant inList : restaurantsList) {
            if(restaurant.getOwnerId().equals(inList.getOwnerId())
                    &&restaurant.getName().equals(inList.getName())&&
                    restaurant.getStreet().equals(inList.getStreet())) return inList;
        }
        return null; //something went horribly wrong
    }

    public void removeDeliveryHours(String restaurant_id){
        final String id = restaurant_id;
        mDatabase = FirebaseDatabase.getInstance().getReference().child("DeliveryHours");

        mDatabase.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot ps : dataSnapshot.getChildren()) {
                            DeliveryHours d = ps.getValue(DeliveryHours.class);
                            if(d.getRestaurantId().equals(id)){
                                //correct restaurant found
                                mDatabase.child(ps.getKey()).removeValue();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }
}