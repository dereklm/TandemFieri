package com.gmail.dleemcewen.tandemfieri.MenuBuilder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.gmail.dleemcewen.tandemfieri.Entities.Restaurant;
import com.gmail.dleemcewen.tandemfieri.R;

import java.util.ArrayList;

public class MenuBuilderActivity extends AppCompatActivity {

    private MenuCatagory current;
    private  MenuItemAdapter adapter;
    private ArrayList<MenuCompenet> allItems;
    private ListView listView;
    private Context context;
    private Restaurant owner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_builder);

        Bundle bundle = this.getIntent().getExtras();
        current = (MenuCatagory) bundle.getSerializable("menu");

        owner = (Restaurant) bundle.getSerializable("resturaunt");
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
                    Intent intent = new Intent(MenuBuilderActivity.this,MenuItemEditActivity.class);
                    intent.putExtra("parent",current);
                    intent.putExtra("item",compenet);
                    startActivityForResult(intent,666);
                }
                else{
                    Intent intent = new Intent(MenuBuilderActivity.this,MenuBuilderActivity.class);
                    intent.putExtra("menu", (MenuCatagory)compenet);
                    startActivityForResult(intent,999);
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

        Button save = (Button) findViewById(R.id.saveButton);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("now",current);
                resultIntent.putExtra("resturaunt",owner);
                setResult(Activity.RESULT_OK,resultIntent);
                finish();
            }
        });

    }

    private void updatAllItems() {
        allItems = new ArrayList<>();
        allItems.addAll(current.getSubCategories());
        allItems.addAll(current.getSubItems());
    }

    @Override
    public void onResume(){
        super.onResume();
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
            intent.putExtra("parent",current);
            startActivityForResult(intent,666);
            return(true);
    }
        return(super.onOptionsItemSelected(item));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (666) : {
                if (resultCode == Activity.RESULT_OK) {
                    current = (MenuCatagory) data.getSerializableExtra("current");
                    updatAllItems();
                }
                break;
            }
            case(999) : {
                if(resultCode==Activity.RESULT_OK){
                    MenuCatagory temp = (MenuCatagory) data.getSerializableExtra("now");
                    MenuCatagory toRemove = new MenuCatagory("");
                    for(MenuCatagory mc : current.getSubCategories()){
                        if(mc.getName().equals(temp.getName())){
                            toRemove=mc;
                        }
                    }
                    current.getSubCategories().remove(toRemove);
                    current.getSubCategories().add(temp);
                    updatAllItems();
                }
            }
        }
    }
}
