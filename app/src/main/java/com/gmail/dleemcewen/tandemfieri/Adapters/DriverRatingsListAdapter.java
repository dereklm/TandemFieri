package com.gmail.dleemcewen.tandemfieri.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gmail.dleemcewen.tandemfieri.R;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DriverRatingsListAdapter provides the required methods to render the
 * listview used in the driver ratings activity
 */

public class DriverRatingsListAdapter extends BaseAdapter {
    private Context context;
    private List<Map.Entry<String, Double>> driverRatingsList;

    public DriverRatingsListAdapter(Context context, List<Map.Entry<String, Double>> driverRatingsList) {
        this.context = context;

        if (!driverRatingsList.isEmpty()) {
            this.driverRatingsList = driverRatingsList;
        } else {
            this.driverRatingsList = buildRatingListWithDefaultMessage();
        }
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return driverRatingsList.size();
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
        return driverRatingsList.get(position);
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
        final Map.Entry<String, Double> driverRating = driverRatingsList.get(position);

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.driver_ratings_list_item, null);
        }

        TextView driverName = (TextView)convertView.findViewById(R.id.driverName);
        driverName.setText(driverRating.getKey());
        driverName.setTypeface(null, Typeface.BOLD);

        TextView averageRating = (TextView)convertView.findViewById(R.id.averageRating);
        averageRating.setText(driverRating.getValue().toString());

        return convertView;
    }

    /**
     * buildRatingListWithDefaultMessage builds a rating list with a default message
     * @return rating list containing default message
     */
    private List<Map.Entry<String, Double>> buildRatingListWithDefaultMessage() {
        AbstractMap.Entry<String, Double> defaultMessageEntry =
            new AbstractMap.SimpleEntry<String, Double>("No driver ratings to show", 0d);

        List<Map.Entry<String, Double>> ratingListWithDefaultMessage =
            new ArrayList<Map.Entry<String, Double>>();
        ratingListWithDefaultMessage.add(defaultMessageEntry);

        return ratingListWithDefaultMessage;
    }
}
