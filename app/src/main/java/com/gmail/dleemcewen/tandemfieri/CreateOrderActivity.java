package com.gmail.dleemcewen.tandemfieri;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.gmail.dleemcewen.tandemfieri.Entities.Restaurant;
import com.gmail.dleemcewen.tandemfieri.menubuilder.MenuCatagory;
import com.gmail.dleemcewen.tandemfieri.menubuilder.MenuCompenet;
import com.gmail.dleemcewen.tandemfieri.menubuilder.MenuItem;
import com.gmail.dleemcewen.tandemfieri.menubuilder.MenuItemAdapter;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class CreateOrderActivity extends AppCompatActivity {
    DatabaseReference mDatabase;
    private  MenuItemAdapter adapter;
    private Context context;
    Restaurant restaurant;
    private MenuCatagory current;
    private ArrayList<MenuCompenet> allItems;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_order);
        Bundle bundle = new Bundle();
        bundle = this.getIntent().getExtras();
        restaurant = (Restaurant) bundle.getSerializable("Restaurant");
        int i = 0, j = 0;

        allItems = new ArrayList<>();
        allItems.addAll(restaurant.getMenu().getSubCategories());
        allItems.addAll(restaurant.getMenu().getSubItems());


        adapter = new MenuItemAdapter(this,allItems);
        listView = (ListView) findViewById(R.id.menuItemsOrder);
        listView.setAdapter(adapter);
        context= this;

        //listview = (ListView) findViewById(R.id.menuItemsOrder);

        //Toast.makeText(getApplicationContext(),"The Restaurant is " + restaurant.getName(), Toast.LENGTH_LONG).show();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MenuCompenet compenet = adapter.getItem(i);
                if(compenet instanceof MenuItem){
                    Toast.makeText(getApplicationContext(),"Clicking an item", Toast.LENGTH_LONG).show();
                    /*
                    Intent intent = new Intent(hMenuBuilderActivity.this,MenuItemEditActivity.class);
                    intent.putExtra("parent",current);
                    intent.putExtra("item",compenet);
                    startActivityForResult(intent,666);
                    */
                }
                else{
                    Toast.makeText(getApplicationContext(),"Clicking a category", Toast.LENGTH_LONG).show();
                    /*
                    Intent intent = new Intent(MenuBuilderActivity.this,MenuBuilderActivity.class);
                    intent.putExtra("menu", (MenuCatagory)compenet);
                    startActivityForResult(intent,999);
                    */
                }
            }
        });

    }


    //create menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.look_at_menu, menu);
        return true;
    }

    //determine which menu option was selected and call that option's action method
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.orderLaunch:
                /*
                **************************
                launch you Activity here Derek
                ***************************
                 */
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
