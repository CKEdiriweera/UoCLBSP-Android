package com.group06.applications.uoclbsp_source;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import com.miguelcatalan.materialsearchview.MaterialSearchView;

public class Directions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions);

        Toolbar toolbar2 = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar2);
        getSupportActionBar().setTitle("Directions");
        toolbar2.setTitleTextColor(Color.parseColor("#FFFFFF"));


        toolbar2.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // back button pressed
                onBackPressed(); // Implemented by activity
            }
        });
    }
}
