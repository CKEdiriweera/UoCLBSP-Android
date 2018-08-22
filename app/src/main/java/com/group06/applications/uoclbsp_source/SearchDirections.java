package com.group06.applications.uoclbsp_source;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.PolyUtil;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchDirections extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    public Polyline mPolyline;
    View mapView;
    GoogleMap mMap;
    MaterialSearchView searchView;
    ListView lstView;
    ArrayList<JSONObject> lstSource = new ArrayList<JSONObject>();
    ArrayList<JSONObject> lstFound;
    List<double[]> lstFoundLocation;
    ArrayList<ArrayList<LatLng>> myPolygons = null;
    ArrayList<Integer> myIndex = null;
    Marker markerstart;
    Marker markerend;
    LatLng currLatLng;
    Polyline polyline;
    Boolean searching = true;
    boolean done = false;
    private FusedLocationProviderClient mFusedLocationClient;
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    public Button getDirectionsButton;
    public ImageButton refreshButton;
    ArrayList<Polyline> lanePolylines = new ArrayList<Polyline>();
    private int waitingTime = 200;
    private CountDownTimer cntr;
    private DrawerLayout mDrawerLayout;
    int count = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_directions);

        Toolbar toolbar3 = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar3);
        getSupportActionBar().setTitle("Get Directions");
        toolbar3.setTitleTextColor(Color.parseColor("#FFFFFF"));


        toolbar3.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // back button pressed
                onBackPressed(); // Implemented by activity
            }
        });



        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();
        findMyLocation();


        lstView = (ListView) findViewById(R.id.lstView);
        getDirectionsButton = (Button) findViewById(R.id.get_directions_button);
        refreshButton = (ImageButton) findViewById(R.id.refresh_button);


        getDirectionsButton.setVisibility(View.INVISIBLE);
        refreshButton.setVisibility(View.INVISIBLE);


        SearchArrayAdapter adapter = new SearchArrayAdapter(this, R.layout.uocmap_list_item, lstSource);
        lstView.setAdapter(adapter);

        searchView = (MaterialSearchView) findViewById(R.id.search_view);

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {

                //If closed Search View , lstView will return default
                lstView = (ListView) findViewById(R.id.lstView);
                SearchArrayAdapter adapter = new SearchArrayAdapter(SearchDirections.this, R.layout.uocmap_list_item, lstSource);
                lstView.setAdapter(adapter);

            }
        });

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searching = true;
                final String clue = newText;
                if(cntr != null){
                    cntr.cancel();
                }
                if (newText != null && !newText.isEmpty()) {

                    try {

                        cntr = new CountDownTimer(waitingTime, 500) {

                            public void onTick(long millisUntilFinished) {
                            }

                            public void onFinish() {
                                new GetSearchDirectionsResults().execute(new Object[]{clue, SearchDirections.this, 1});
                                lstView = (ListView) findViewById(R.id.lstView);
                                lstView.setVisibility(View.VISIBLE);
                            }
                        };
                        cntr.start();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    //if search text is null
                    //return default
                    lstView = (ListView) findViewById(R.id.lstView);
                    SearchArrayAdapter adapter = new SearchArrayAdapter(SearchDirections.this, R.layout.uocmap_list_item, lstSource);
                    lstView.setAdapter(adapter);
                }
                return true;
            }

        });

        lstView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(cntr != null){
                    cntr.cancel();
                }
                String title = "";
                try {
                    title = lstFound.get(position).getString("name");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                searching = false;
                searchView.setQuery(title, true);
//                getSupportActionBar().setTitle(lstFound.get(position));
                double[] loc = lstFoundLocation.get(position);


                if(count == 0) {
                    markerstart = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(loc[0], loc[1]))
                            .title(title));

                    markerstart.showInfoWindow();
                    count++;
                    getDirectionsButton.setVisibility(View.INVISIBLE);
                    refreshButton.setVisibility(View.VISIBLE);

                    Toast.makeText(SearchDirections.this,"Please Search & Select the End Point!", Toast.LENGTH_LONG).show();

                }else if(count == 1){
                    markerend = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(loc[0], loc[1]))
                            .title(title));

                    markerend.showInfoWindow();
                    count++;
                    getDirectionsButton.setVisibility(View.VISIBLE);
                    refreshButton.setVisibility(View.VISIBLE);

                    Toast.makeText(SearchDirections.this,"Please Click GET DIRECTION!", Toast.LENGTH_LONG).show();

                }else if(count == 2){
                    if (mPolyline!=null){
                        mPolyline.remove();
                    }
                    if(markerstart != null){
                        markerstart.remove();
                    }
                    if(markerend != null){
                        markerend.remove();
                    }


                    markerstart = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(loc[0], loc[1]))
                            .title(title));

                    markerstart.showInfoWindow();
                    count--;
                    getDirectionsButton.setVisibility(View.INVISIBLE);
                    refreshButton.setVisibility(View.VISIBLE);

                    Toast.makeText(SearchDirections.this,"Please Search & Select the End Point!", Toast.LENGTH_LONG).show();

                }

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc[0], loc[1]),16));
                lstView = (ListView) findViewById(R.id.lstView);
                lstView.setVisibility(View.INVISIBLE);


            }
        });

        Toast.makeText(SearchDirections.this,"Please Search & Select Start Point!", Toast.LENGTH_LONG).show();




    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //Move camera to the default location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(6.902631, 76.860611), 16));

        //Check permission and get current location
        findMyLocation();

        //set move camera to current location
        setToCurrentLocation();
        new GetSearchDirectionsResults().execute(new Object[]{"", SearchDirections.this, 2});
        mMap.setOnCameraIdleListener(this);
        mMap.setPadding(0,150,0,0);
    }

    private void setToCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        currLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
                    }
                }
            });
        }
    }

    private void findMyLocation() {
        if (mMap == null) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        if (mLocationPermissionGranted) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
                mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            mLastKnownLocation = location;
                            //System.out.println(location);
                        }
                        //System.out.println("No location");


                    }
                });
                View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                        locationButton.getLayoutParams();
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                layoutParams.setMargins(0, 0, 30, 30);
            }

        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mLastKnownLocation = null;
        }
    }



    public void getDirections(View view) {

        findMyLocation();
        System.out.println("I'm alone");
        System.out.println(mLastKnownLocation);
        int sourcePoly = 0;
        int destinationPoly = 0;
        LatLng sourceLatLng = markerstart.getPosition();
        LatLng destinationLatLng = markerend.getPosition();
        System.out.println(myPolygons.size());
        for (int x = 0; x < myPolygons.size(); x++) {
            if (sourcePoly == 0 | destinationPoly == 0) {
                if (PolyUtil.containsLocation(sourceLatLng, myPolygons.get(x), true)) {
                    sourcePoly = myIndex.get(x);
                }
                if (PolyUtil.containsLocation(destinationLatLng, myPolygons.get(x), true)) {
                    destinationPoly = myIndex.get(x);
                }
            }
        }
        System.out.println(sourceLatLng.toString());
        System.out.println(destinationLatLng.toString());
        Object[] objects = new Object[]{sourceLatLng, sourcePoly, destinationLatLng, destinationPoly};
        new GetSearchDirectionsResults().execute(new Object[]{objects, SearchDirections.this, 3});

        Toast.makeText(SearchDirections.this,"Click Refresh to Clear Map!", Toast.LENGTH_LONG).show();
    }

    public float getLineWidth() {
        float zoom = mMap.getCameraPosition().zoom;
        if(zoom<11){
            return 0;
        }else{
            float line = (float) ((zoom-11)*1.5);
            return line;
        }

    }

    @Override
    public void onCameraIdle() {
//        System.out.println("Called");
        if (lanePolylines != null) {
            for (int i = 0; i < lanePolylines.size(); i++) {
                lanePolylines.get(i).setWidth(this.getLineWidth());
            }
            //mPolyline.setWidth(this.getLineWidth());
        }
    }

    public void refresh(View view) {
        if (mPolyline!=null){
            mPolyline.remove();
        }
        if(markerstart != null){
            markerstart.remove();
        }
        if(markerend != null){
            markerend.remove();
        }
        count = 0;
        getDirectionsButton.setVisibility(View.INVISIBLE);
        refreshButton.setVisibility(View.INVISIBLE);

        Toast.makeText(SearchDirections.this,"Refreshed Successfully!", Toast.LENGTH_LONG).show();
        Toast.makeText(SearchDirections.this,"Search & Select the Start Point!", Toast.LENGTH_LONG).show();
    }
}

class GetSearchDirectionsResults extends AsyncTask {

    @Override
    protected Object doInBackground(Object[] params) {

        try {
            InetAddress address = InetAddress.getByName("ec2-18-216-184-231.us-east-2.compute.amazonaws.com");
            Socket s1 = null;
            String line = null;
            BufferedReader br = null;
            BufferedReader is = null;
            PrintWriter os = null;

            s1 = new Socket(address, 1978); // You can use static final constant PORT_NUM
            br = new BufferedReader(new InputStreamReader(System.in));
            is = new BufferedReader(new InputStreamReader(s1.getInputStream()));
            os = new PrintWriter(s1.getOutputStream());

            String response = null;
            JSONObject jsonObject1 = new JSONObject();
            if ((int) params[2] == 1) {
                jsonObject1.put("type", "searchRequest");
                jsonObject1.put("input", String.valueOf(params[0]));
                jsonObject1.put("role", "registered");
            } else if ((int) params[2] == 2) {
                jsonObject1.put("type", "mapRequest");
            } else {
                Object[] objects = (Object[]) params[0];
                jsonObject1.put("type", "getPath");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("latitudes", ((LatLng) objects[0]).latitude);
                jsonObject.put("longitudes", ((LatLng) objects[0]).longitude);
                jsonObject.put("inside", (int) objects[1]);
                jsonObject1.put("source", jsonObject);
                jsonObject = new JSONObject();
                jsonObject.put("latitudes", ((LatLng) objects[2]).latitude);
                jsonObject.put("longitudes", ((LatLng) objects[2]).longitude);
                jsonObject.put("inside", (int) objects[3]);
                jsonObject1.put("destination", jsonObject);
            }
            line = jsonObject1.toString();

            os.println(line);
            os.flush();
            response = is.readLine();
            System.out.println("Server Response : " + response);

            JSONObject jsonObject = new JSONObject(response);
            if ((int) params[2] == 1) {
                JSONArray jsonArray = jsonObject.getJSONArray("Results");
                SearchDirections searchDirections = (SearchDirections) params[1];
                searchDirections.lstFound = new ArrayList<JSONObject>();
                searchDirections.lstFoundLocation = new ArrayList<double[]>();

                for (int i = 0; i < jsonArray.length(); i++) {

                    searchDirections.lstFound.add(jsonArray.getJSONObject(i));
                    searchDirections.lstFoundLocation.add(new double[]{jsonArray.getJSONObject(i).getDouble("lat"), jsonArray.getJSONObject(i).getDouble("lng")});
                }
                SearchArrayAdapter adapter = new SearchArrayAdapter(searchDirections, R.layout.uocmap_list_item, searchDirections.lstFound);
                params[0] = adapter;
            } else if ((int) params[2] == 2) {
                //JSONArray jsonArray = jsonObject.getJSONArray("polygons");
                params[0] = jsonObject;
            } else {
                JSONArray jsonArray = jsonObject.getJSONArray("steps");
                params[0] = jsonArray;
            }
            return params;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        try {
            Object[] params = (Object[]) o;
            SearchDirections searchDirections = (SearchDirections) params[1];
            if ((int) params[2] == 1) {
                SearchArrayAdapter adapter = (SearchArrayAdapter) params[0];
                if (searchDirections.searching) {
                    searchDirections.lstView.setAdapter(adapter);
                }
            } else if ((int) params[2] == 2) {
                JSONObject mapObject = (JSONObject) params[0];
                JSONArray jsonArray = (JSONArray) mapObject.get("polygons");
                JSONArray graphArray = (JSONArray) mapObject.get("graphs");

                for (int i = 0; i < graphArray.length(); i++) {
                    HashMap<Integer,LatLng> vertexMap = new HashMap<Integer, LatLng>();
                    JSONObject graphElement = graphArray.getJSONObject(i);
                    JSONArray verArray = graphElement.getJSONArray("vertexes");
                    JSONArray edgeArray = graphElement.getJSONArray("edges");
                    for (int j = 0; j < verArray.length(); j++) {
                        JSONObject verObject = verArray.getJSONObject(j);
                        vertexMap.put(verObject.getInt("id"),new LatLng(verObject.getDouble("lat"),verObject.getDouble("lng")));
                    }
                    for (int j = 0; j < edgeArray.length(); j++) {
                        JSONObject edgeObject = edgeArray.getJSONObject(j);
                        searchDirections.lanePolylines.add(searchDirections.mMap.addPolyline(new PolylineOptions()
                                .add(vertexMap.get(edgeObject.getInt("source")),vertexMap.get(edgeObject.getInt("destination")))
                                .width(searchDirections.getLineWidth())
                                .color(Color.rgb(242,242,242))
                        ));
                    }
                }
                ArrayList<ArrayList<LatLng>> myPolygons = new ArrayList<ArrayList<LatLng>>();
                ArrayList<Integer> myIndex = new ArrayList<Integer>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                    myIndex.add(jsonObject.getInt("id"));

                    JSONArray jsonArray1 = jsonObject.getJSONArray("vertexes");
                    ArrayList<LatLng> points = new ArrayList<LatLng>();
                    for (int j = 0; j < jsonArray1.length(); j++) {
                        JSONObject jsonObject1 = (JSONObject) jsonArray1.get(j);
                        points.add(new LatLng(jsonObject1.getDouble("lat"), jsonObject1.getDouble("lng")));
                    }
                    myPolygons.add(points);

                }

                searchDirections.myPolygons = myPolygons;
                searchDirections.myIndex = myIndex;
                searchDirections.done = true;
            } else {
                JSONArray jsonArray = (JSONArray) params[0];
                ArrayList<LatLng> polylines = new ArrayList<LatLng>();
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                    LatLng myLatLng = new LatLng(jsonObject.getDouble("lat"), jsonObject.optDouble("lng"));
                    polylines.add(myLatLng);
                    if (i==0 | i==jsonArray.length()-1 | i%20==0){
                        builder.include(myLatLng);
                    }

                }
                if (searchDirections.mPolyline!=null){
                    searchDirections.mPolyline.remove();
                }
                searchDirections.mPolyline = searchDirections.mMap.addPolyline(new PolylineOptions()
                        .addAll(polylines)
                        .width(10)
                        .color(Color.BLUE)
                        .zIndex(10)
                );
                searchDirections.mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),50));

                searchDirections.getDirectionsButton.setVisibility(View.INVISIBLE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


