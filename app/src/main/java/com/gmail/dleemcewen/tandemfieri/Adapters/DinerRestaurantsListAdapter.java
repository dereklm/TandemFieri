package com.gmail.dleemcewen.tandemfieri.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gmail.dleemcewen.tandemfieri.Entities.Day;
import com.gmail.dleemcewen.tandemfieri.Entities.DeliveryHours;
import com.gmail.dleemcewen.tandemfieri.Entities.Rating;
import com.gmail.dleemcewen.tandemfieri.Entities.Restaurant;
import com.gmail.dleemcewen.tandemfieri.Formatters.DateFormatter;
import com.gmail.dleemcewen.tandemfieri.R;
import com.gmail.dleemcewen.tandemfieri.Repositories.Ratings;
import com.gmail.dleemcewen.tandemfieri.Repositories.RestaurantHours;
import com.gmail.dleemcewen.tandemfieri.Tasks.TaskResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * DinerRestaurantsListAdapter provides the required methods to render the
 * listview used in the diner mainmenu activity
 */

public class DinerRestaurantsListAdapter extends BaseAdapter {
    private Activity context;
    private List<Restaurant> restaurantsList;
    private RestaurantHours<DeliveryHours> restaurantHours;
    private Ratings<Rating> ratings;
    private Date currentDate;

    public DinerRestaurantsListAdapter(Activity context, List<Restaurant> restaurantsList) {
        this.context = context;
        this.restaurantHours = new RestaurantHours<>(context);
        this.ratings = new Ratings<>(context);
        this.currentDate = new Date();

        if (!restaurantsList.isEmpty()) {
            this.restaurantsList = restaurantsList;
        } else {
            this.restaurantsList = new ArrayList<>();
        }
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
        final Restaurant restaurant  = restaurantsList.get(position);

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.diner_mainmenu_item_view, null);
        }

        TextView restaurantName = (TextView)convertView.findViewById(R.id.item_view);
        restaurantName.setText(restaurant.getName());
        switch (restaurant.getRestaurantType()) {
            case "American":
                restaurantName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_hamburger, 0, 0, 0);
                break;
            case "Pizza":
                restaurantName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pizza, 0, 0, 0);
                break;
            case "Barbecue":
                restaurantName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_barbecue, 0, 0, 0);
                break;
            case "Mexican":
                restaurantName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_burrito, 0, 0, 0);
                break;
            case "Chinese":
                restaurantName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_chinese, 0, 0, 0);
                break;
            default:
                restaurantName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_dining, 0, 0, 0);
                break;
        }

        TextView restaurantOpenClosed = (TextView)convertView.findViewById(R.id.restaurantOpenClosed);
        setRestaurantHours(restaurant.getId(), restaurantOpenClosed);

        TextView restaurantAverageRating = (TextView)convertView.findViewById(R.id.restaurantAverageRating);
        getRestaurantAverageRating(restaurant.getId(), restaurantAverageRating);

        return convertView;
    }

    /**
     * setRestaurantHours calculates and sets the restaurant hours text for a restaurant
     * @param restaurantId uniquely identifies the restaurant
     * @param restaurantOpenClosed identifies the textview to update
     */
    private void setRestaurantHours(String restaurantId, TextView restaurantOpenClosed) {
        getRestaurantHours(restaurantId, restaurantOpenClosed);
    }

    /**
     * getRestaurantHours retrieves the hours for the specified restaurant
     * @param restaurantId uniquely identifies the restaurant
     * @param restaurantOpenClosed identifies the textview to update
     */
    private void getRestaurantHours(String restaurantId, final TextView restaurantOpenClosed) {
        final int dayOfWeek = getDayOfWeekFromCurrentDate();

        restaurantHours
            .find("restaurantId = '" + restaurantId + "'")
            .addOnCompleteListener(context, new OnCompleteListener<TaskResult<DeliveryHours>>() {
                @Override
                public void onComplete(@NonNull Task<TaskResult<DeliveryHours>> task) {
                    List<DeliveryHours> deliveryHours = task.getResult().getResults();
                    if (!deliveryHours.isEmpty() && !deliveryHours.get(0).getDays().isEmpty()) {
                        Day dayHours = deliveryHours.get(0).getDays().get(dayOfWeek);

                        buildRestaurantHoursText(dayHours, restaurantOpenClosed);
                    } else {
                        setRestaurantHoursText("CLOSED. No delivery hours have been set for today.", restaurantOpenClosed, Color.argb(255, 128, 128, 128));
                    }
                }
            });
    }

    /**
     * buildRestaurantHoursText builds the restaurant hours text
     * @param dayHours indicates the hours the restaurant is open for each day
     * @param restaurantOpenClosed identifies the textview to update
     */
    private void buildRestaurantHoursText(Day dayHours, TextView restaurantOpenClosed) {
        StringBuilder hoursTextBuilder = new StringBuilder();

        if (dayHours.getOpen()) {
            if (dayHours.compareOpenTimeWithCurrentTime(currentDate)) {
                if (dayHours.compareClosedTimeWithCurrentTime(currentDate)) {
                    hoursTextBuilder.append("CURRENTLY OPEN. ");
                    hoursTextBuilder.append("Open today from ");
                    hoursTextBuilder.append(DateFormatter.convertMilitaryTimeToStandardString(dayHours.getHourOpen()));
                    hoursTextBuilder.append(" to ");
                    hoursTextBuilder.append(DateFormatter.convertMilitaryTimeToStandardString(dayHours.getHourClosed()));
                    hoursTextBuilder.append(".");
                    setRestaurantHoursText(hoursTextBuilder.toString(), restaurantOpenClosed, Color.BLUE);
                } else {
                    hoursTextBuilder.append("CLOSED. ");
                    hoursTextBuilder.append("Open today from ");
                    hoursTextBuilder.append(DateFormatter.convertMilitaryTimeToStandardString(dayHours.getHourOpen()));
                    hoursTextBuilder.append(" to ");
                    hoursTextBuilder.append(DateFormatter.convertMilitaryTimeToStandardString(dayHours.getHourClosed()));
                    hoursTextBuilder.append(".");
                    setRestaurantHoursText(hoursTextBuilder.toString(), restaurantOpenClosed, Color.argb(255, 128, 128, 128));
                }
            } else {
                hoursTextBuilder.append("CURRENTLY CLOSED. ");
                hoursTextBuilder.append("Open today from ");
                hoursTextBuilder.append(DateFormatter.convertMilitaryTimeToStandardString(dayHours.getHourOpen()));
                hoursTextBuilder.append(" to ");
                hoursTextBuilder.append(DateFormatter.convertMilitaryTimeToStandardString(dayHours.getHourClosed()));
                hoursTextBuilder.append(".");
                setRestaurantHoursText(hoursTextBuilder.toString(), restaurantOpenClosed, Color.argb(255, 128, 128, 128));
            }
        } else {
            hoursTextBuilder.append("CLOSED. ");
            hoursTextBuilder.append("Not open today.");
            setRestaurantHoursText(hoursTextBuilder.toString(), restaurantOpenClosed, Color.argb(255, 128, 128, 128));
        }
    }

    /**
     * setRestaurantHoursText sets the restaurant hours text
     * @param hoursText indicates the text to display
     * @param restaurantOpenClosed identifies the textview to update
     * @param textColor indicates the text color of the restaurant hours
     */
    private void setRestaurantHoursText(String hoursText, TextView restaurantOpenClosed, int textColor) {
        restaurantOpenClosed.setText(hoursText);
        restaurantOpenClosed.setTextColor(textColor);
    }

    /**
     * getDayOfWeekFromCurrentDate gets the day of the week from the current date
     * and returns the integer representation of the day
     * @return integer representing the current day of the week
     */
    private int getDayOfWeekFromCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);

        //have to subtract 1 because when the days of the week were created they were created as a
        //zero-based list, whereas calendar uses a 1-based list
        return calendar.get(Calendar.DAY_OF_WEEK) - 1;
    }

    /**
     * getRestaurantAverageRating calculates the average rating for a given restaurant
     * @param restaurantId uniquely identifies the restaurant
     * @param restaurantAverageRating identifies the textview to update
     */
    private void getRestaurantAverageRating(String restaurantId, final TextView restaurantAverageRating) {
        ratings
            .find("restaurantId = '" + restaurantId + "'")
            .addOnCompleteListener(context, new OnCompleteListener<TaskResult<Rating>>() {
                @Override
                public void onComplete(@NonNull Task<TaskResult<Rating>> task) {
                    Float averageRating = 0F;
                    List<Rating> ratings = task.getResult().getResults();

                    if (!ratings.isEmpty()) {
                        Float totalRating = 0F;
                        int numberOfApplicableRatings = 0;

                        for (Rating rating : ratings) {
                            if (rating.getDriverId() == null && rating.getOrderId() == null) {
                                totalRating += rating.getRating();
                                numberOfApplicableRatings++;
                            }
                        }

                        averageRating = totalRating / numberOfApplicableRatings;
                    }

                    setRestaurantAverageRatingText(averageRating, restaurantAverageRating);
                }
            });
    }

    /**
     * setRestaurantAverageRatingText formats and sets the average rating text for a restaurant
     * @param averageRating indicates the average rating
     * @param restaurantAverageRating identifies the textview to update
     */
    private void setRestaurantAverageRatingText(Float averageRating, TextView restaurantAverageRating) {
        if (averageRating > 0) {
            restaurantAverageRating
                .setText("Average rating: " + String.format(Locale.US, "%.2f", averageRating));
        } else {
            restaurantAverageRating
                .setText("No ratings yet");
        }
    }
}
