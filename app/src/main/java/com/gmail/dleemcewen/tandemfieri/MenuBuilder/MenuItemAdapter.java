package com.gmail.dleemcewen.tandemfieri.MenuBuilder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmail.dleemcewen.tandemfieri.R;

import java.util.ArrayList;

/**
 * Created by jfly1_000 on 2/14/2017.
 */
public class MenuItemAdapter extends ArrayAdapter<MenuCompenet>{

    private final Context context;
    private final ArrayList<MenuCompenet> values;

    public MenuItemAdapter(Context context,ArrayList<MenuCompenet> values){
        super(context,-1,values);
        this.context=context;
        this.values=values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView=inflater.inflate(R.layout.menu_rowlayout,parent,false);
        TextView text = (TextView) rowView.findViewById(R.id.text);
        ImageView image = (ImageView) rowView.findViewById(R.id.icon);
        text.setText(values.get(position).getName());
        if(values.get(position) instanceof MenuItem){
            image.setImageResource(R.drawable.ic_menu_item);
        }
        else{
            image.setImageResource(R.drawable.ic_catagory);
        }
        return rowView;
    }

    public MenuCompenet getItem(int postion){
        return values.get(postion);
    }

}
