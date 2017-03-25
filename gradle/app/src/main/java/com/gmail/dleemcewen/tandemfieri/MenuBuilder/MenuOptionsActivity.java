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

import com.gmail.dleemcewen.tandemfieri.R;

public class MenuOptionsActivity extends AppCompatActivity {

    MenuIterator iterator = MenuIterator.create();

    private OptionAdapter adapter;
    private ListView listView;
    private Context context;
    private MenuItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_options);

        item = (MenuItem) iterator.getCurrentComponent();

        adapter = new OptionAdapter(this,item.getOptions());
        listView = (ListView) findViewById(R.id.menuItems);
        listView.setAdapter(adapter);
        context= this;

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ItemOption compenet = adapter.getItem(i);
                iterator.setCurrentOption(compenet);
                Intent intent = new Intent(MenuOptionsActivity.this,OptionAddActivity.class);
                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                ItemOption compenet = adapter.getItem(i);
                item.getOptions().remove(compenet);
                adapter = new OptionAdapter(context,item.getOptions());
                listView.setAdapter(adapter);
                return true;
            }
        });

        TextView name = (TextView) findViewById(R.id.CatName);
        name.setText("Options");

    }

    @Override
    public void onResume(){
        super.onResume();
        adapter = new OptionAdapter(this,item.getOptions());
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
                Intent intent = new Intent(MenuOptionsActivity.this,OptionAddActivity.class);
                iterator.setCurrentOption(null);
                startActivity(intent);
                return(true);
        }
        return(super.onOptionsItemSelected(item));
    }

}
