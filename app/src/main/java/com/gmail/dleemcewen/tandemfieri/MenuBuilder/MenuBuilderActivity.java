package com.gmail.dleemcewen.tandemfieri.menubuilder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.gmail.dleemcewen.tandemfieri.Entities.Restaurant;
import com.gmail.dleemcewen.tandemfieri.R;
import com.gmail.dleemcewen.tandemfieri.Repositories.Restaurants;

import java.util.ArrayList;

public class MenuBuilderActivity extends AppCompatActivity {

    private MenuIterator iterator = MenuIterator.create();

    private MenuCatagory current;
    private  MenuItemAdapter adapter;
    private ArrayList<MenuCompenet> allItems;
    private ListView listView;
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_builder);

        current = (MenuCatagory) iterator.getCurrentComponent();

        allItems = new ArrayList<>();
        allItems.addAll(current.getSubCategories());
        allItems.addAll(current.getSubItems());

        adapter = new MenuItemAdapter(this,allItems);
        listView = (ListView) findViewById(R.id.menuItems);
        listView.setAdapter(adapter);
        context= this;

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MenuCompenet compenet = adapter.getItem(i);
                if(compenet instanceof MenuItem){
                    Intent intent = new Intent(MenuBuilderActivity.this, MenuItemEditActivity.class);
                    iterator.setCurrentComponent(compenet);
                    startActivity(intent);
                }
                else{
                    Intent intent = new Intent(MenuBuilderActivity.this,MenuBuilderActivity.class);
                    iterator.setCurrentComponent(compenet);
                    startActivity(intent);
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                MenuCompenet compenet = adapter.getItem(i);
                if(compenet instanceof MenuCatagory){
                    current.getSubCategories().remove(compenet);
                }
                else current.getSubItems().remove(compenet);
                updatAllItems();
                adapter = new MenuItemAdapter(context,allItems);
                listView.setAdapter(adapter);
                return true;
            }
        });

        TextView name = (TextView) findViewById(R.id.CatName);
        name.setText(current.name);


    }

    private void updatAllItems() {
        allItems = new ArrayList<>();
        allItems.addAll(current.getSubCategories());
        allItems.addAll(current.getSubItems());
    }

    @Override
    public void onResume(){
        super.onResume();
        updatAllItems();
        adapter = new MenuItemAdapter(this,allItems);
        listView = (ListView) findViewById(R.id.menuItems);
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // Inflate the menu;jfly this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_builder_action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch(item.getItemId()) {
        case R.id.add:
            Intent intent = new Intent(MenuBuilderActivity.this,MenuItemEditActivity.class);
            iterator.setCurrentComponent(null);
            startActivity(intent);
            return(true);
    }
        return(super.onOptionsItemSelected(item));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            iterator.backtrack();
            if (iterator.getCurrentComponent() == null) {
                Restaurants<Restaurant> restaurantsRepository = new Restaurants<>(this);
                restaurantsRepository
                        .update(iterator.getRestaurant());
            }
        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        this.finish();
    }
}
