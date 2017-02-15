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

import com.gmail.dleemcewen.tandemfieri.R;

public class MenuOptionsActivity extends AppCompatActivity {

    private OptionAdapter adapter;
    private ListView listView;
    private Context context;
    private MenuItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_options);

        Bundle bundle = this.getIntent().getExtras();
        item = (MenuItem) bundle.getSerializable("item");

        adapter = new OptionAdapter(this,item.getOptions());
        listView = (ListView) findViewById(R.id.menuItems);
        listView.setAdapter(adapter);
        context= this;

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ItemOption compenet = adapter.getItem(i);
                //
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

        Button save = (Button) findViewById(R.id.saveButton);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("now",item);
                setResult(Activity.RESULT_OK,resultIntent);
                finish();
            }
        });

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
                Intent intent = new Intent(MenuOptionsActivity.this,MenuItemEditActivity.class);
                intent.putExtra("item",this.item);
                startActivityForResult(intent,777);
                return(true);
        }
        return(super.onOptionsItemSelected(item));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (777) : {
                if (resultCode == Activity.RESULT_OK) {
                    item = (MenuItem) data.getSerializableExtra("item");
                }
                break;
            }
        }
    }
}
