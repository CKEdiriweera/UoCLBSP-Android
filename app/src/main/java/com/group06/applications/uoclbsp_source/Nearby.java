package com.group06.applications.uoclbsp_source;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.json.JSONObject;

import java.util.ArrayList;

public class Nearby extends AppCompatActivity {

    MaterialSearchView searchView;
    ListView lstView;
    ArrayList<JSONObject> lstSource = new ArrayList<JSONObject>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby);

        Toolbar toolbar2 = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar2);
        getSupportActionBar().setTitle("Nearby");
        toolbar2.setTitleTextColor(Color.parseColor("#FFFFFF"));

        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        lstView = (ListView) findViewById(R.id.lstView);

        toolbar2.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // back button pressed
                onBackPressed(); // Implemented by activity
            }
        });


        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {

                //If closed Search View , lstView will return default
                lstView = (ListView) findViewById(R.id.lstView);
                SearchArrayAdapter adapter = new SearchArrayAdapter(Nearby.this, R.layout.uocmap_list_item, lstSource);
                lstView.setAdapter(adapter);

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return true;
    }
}
