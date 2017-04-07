package com.gmail.dleemcewen.tandemfieri.Adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.gmail.dleemcewen.tandemfieri.Entities.Rating;
import com.gmail.dleemcewen.tandemfieri.Entities.Restaurant;
import com.gmail.dleemcewen.tandemfieri.Formatters.DateFormatter;
import com.gmail.dleemcewen.tandemfieri.R;
import com.gmail.dleemcewen.tandemfieri.Repositories.Ratings;

import java.util.Date;
import java.util.List;

/**
 * RestaurantRatingsListAdapter provides the required methods to render the
 * listview used in the restaurant ratings activity
 */
public class RestaurantRatingsListAdapter extends BaseAdapter {
    private Activity context;
    private Resources resources;
    private List<Restaurant> restaurantsList;
    private Ratings<Rating> ratingsRepository;

    public RestaurantRatingsListAdapter(Activity context, List<Restaurant> restaurantsList) {
        this.context = context;
        resources = context.getResources();
        this.restaurantsList = restaurantsList;
        ratingsRepository = new Ratings<>(context);
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return restaurantsList.size();
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
        return restaurantsList.get(position);
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
    public View getView(int position, View convertView, ViewGroup parent) {
        final Restaurant restaurant = (Restaurant)getItem(position);
        final LayoutInflater layoutInflater = (LayoutInflater) this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.restaurant_ratings_list_item, null);
        }

        TextView rateableRestaurantName = (TextView)convertView.findViewById(R.id.restaurantToRate);
        rateableRestaurantName.setText(restaurant.getName());

        BootstrapButton selectRestaurantToRate = (BootstrapButton)convertView.findViewById(R.id.selectRestaurantToRate);
        selectRestaurantToRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder dialogTitleBuilder = new StringBuilder();
                dialogTitleBuilder.append("Rate ");
                dialogTitleBuilder.append(restaurant.getName());

                //Inflate custom view
                View dialogLayout = layoutInflater.inflate(R.layout.assign_restaurant_rating, null);

                //Find restaurant rating text in dialoglayout
                final TextView restaurantRatingText = (TextView)dialogLayout.findViewById(R.id.restaurantRatingText);

                //Find ratingsbar in dialoglayout
                final RatingBar restaurantRatingBar = (RatingBar)dialogLayout.findViewById(R.id.restaurantRatingBar);
                restaurantRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                    @Override
                    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                        restaurantRatingText.setText(String.valueOf(rating));
                    }
                });

                //Build alert dialog with custom view
                AlertDialog.Builder rateRestaurantDialog  = new AlertDialog.Builder(context);
                rateRestaurantDialog.setView(dialogLayout);
                rateRestaurantDialog
                        .setTitle(dialogTitleBuilder.toString());
                rateRestaurantDialog.setCancelable(false);
                rateRestaurantDialog.setPositiveButton(
                        resources.getString(R.string.save),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Rating newRestaurantRating = new Rating();
                                newRestaurantRating.setDate(DateFormatter.toTimeStamp(new Date()).toString());
                                newRestaurantRating.setRating(restaurantRatingBar.getRating());
                                newRestaurantRating.setRestaurantId(restaurant.getId());

                                ratingsRepository.add(newRestaurantRating);

                                dialog.cancel();
                            }
                        });
                rateRestaurantDialog.setNegativeButton(
                        resources.getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                rateRestaurantDialog
                        .show();
            }
        });

        return convertView;
    }
}

