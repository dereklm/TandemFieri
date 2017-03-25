package com.gmail.dleemcewen.tandemfieri.menubuilder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.gmail.dleemcewen.tandemfieri.R;

import java.util.ArrayList;

/**
 * Created by jfly1_000 on 2/28/2017.
 */

public class OptionSelectionAdapter extends ArrayAdapter<OptionSelection> {
    private final Context context;
    private final ArrayList<OptionSelection> values;

    public OptionSelectionAdapter(Context context,ArrayList<OptionSelection> values){
        super(context,-1,values);
        this.context=context;
        this.values=values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView=inflater.inflate(R.layout.menu_option_rowlayout,parent,false);
        TextView text = (TextView) rowView.findViewById(R.id.text);
        text.setText(values.get(position).getSelectionName());
        TextView price = (TextView) rowView.findViewById(R.id.price);
        price.setText(String.format("$%.2f",values.get(position).getAddedPrice()));
        return rowView;
    }

    public OptionSelection getItem(int postion){
        return values.get(postion);
    }

}
