package com.group06.applications.uoclbsp_source;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class SearchDirections extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_directions);

        Toolbar toolbar3 = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar3);
        getSupportActionBar().setTitle("Search Directions");
        toolbar3.setTitleTextColor(Color.parseColor("#FFFFFF"));


        toolbar3.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // back button pressed
                onBackPressed(); // Implemented by activity
            }
        });

    }
}
