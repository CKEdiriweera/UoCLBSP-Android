package com.group06.applications.uoclbsp_source;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.PolyUtil;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.squareup.picasso.Picasso;

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

import static com.group06.applications.uoclbsp_source.R.id.refresh_button;

public class Directions extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    public Polyline mPolyline;
    View mapView;
    GoogleMap mMap;
    MaterialSearchView searchView;
    ListView lstView;
    TextView textView;
    ArrayList<JSONObject> lstSource = new ArrayList<JSONObject>();
    ArrayList<JSONObject> lstFound;
    List<double[]> lstFoundLocation;
    ArrayList<ArrayList<LatLng>> myPolygons = null;
    ArrayList<Integer> myIndex = null;
    Marker marker;
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
    private static final int LOCATION_REQUEST = 500;
    ArrayList<LatLng> listPoints;

    LatLng ltlng1;
    LatLng ltlng2;
    int count = 0;


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


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();


        refreshButton = (ImageButton) findViewById(R.id.refresh_button);
        getDirectionsButton = (Button) findViewById(R.id.get_directions_button);
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //Move camera to the default location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(6.902317, 79.860718), 16));

        //Check permission and get current location

        new GetSearchResultsD().execute(new Object[]{"", Directions.this, 2});
        mMap.setOnCameraIdleListener(this);
        mMap.setPadding(0,150,0,0);

        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
            return;
        }
        mMap.setMyLocationEnabled(true);

        getDirectionsButton.setVisibility(View.INVISIBLE);
        refreshButton.setVisibility(View.INVISIBLE);

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if(count == 0) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    Marker marker1 = mMap.addMarker(markerOptions);
                    ltlng1 = marker1.getPosition();
                    refreshButton.setVisibility(View.VISIBLE);
                    count++;
                }else if(count == 1) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    Marker marker2 = mMap.addMarker(markerOptions);
                    ltlng2 = marker2.getPosition();
                    getDirectionsButton.setVisibility(View.VISIBLE);
                    refreshButton.setVisibility(View.VISIBLE);
                    count++;
                }else if(count == 2){
                    mMap.clear();
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    Marker marker1 = mMap.addMarker(markerOptions);
                    ltlng1 = marker1.getPosition();
                    refreshButton.setVisibility(View.VISIBLE);
                    count = 1;
                }
            }
        });

    }




    public void getDirections(View view) {

        System.out.println("I'm alone");
        System.out.println(mLastKnownLocation);
        int sourcePoly = 0;
        int destinationPoly = 0;
        LatLng sourceLatLng = ltlng1;
        LatLng destinationLatLng = ltlng2;
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
        new GetSearchResultsD().execute(new Object[]{objects, Directions.this, 3});
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
        getDirectionsButton.setVisibility(View.INVISIBLE);
        refreshButton.setVisibility(View.INVISIBLE);
        mMap.clear();
        count = 0;
    }
}



class GetSearchResultsD extends AsyncTask {

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
                Directions directions = (Directions) params[1];
                directions.lstFound = new ArrayList<JSONObject>();
                directions.lstFoundLocation = new ArrayList<double[]>();

                for (int i = 0; i < jsonArray.length(); i++) {

                    directions.lstFound.add(jsonArray.getJSONObject(i));
                    directions.lstFoundLocation.add(new double[]{jsonArray.getJSONObject(i).getDouble("lat"), jsonArray.getJSONObject(i).getDouble("lng")});
                }
                SearchArrayAdapter adapter = new SearchArrayAdapter(directions, R.layout.uocmap_list_item, directions.lstFound);
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
            Directions directions = (Directions) params[1];
            if ((int) params[2] == 1) {
                SearchArrayAdapter adapter = (SearchArrayAdapter) params[0];
                if (directions.searching) {
                    directions.lstView.setAdapter(adapter);
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
                        directions.lanePolylines.add(directions.mMap.addPolyline(new PolylineOptions()
                                .add(vertexMap.get(edgeObject.getInt("source")),vertexMap.get(edgeObject.getInt("destination")))
                                .width(directions.getLineWidth())
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

                directions.myPolygons = myPolygons;
                directions.myIndex = myIndex;
                directions.done = true;
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
                if (directions.mPolyline!=null){
                    directions.mPolyline.remove();
                }
                directions.mPolyline = directions.mMap.addPolyline(new PolylineOptions()
                        .addAll(polylines)
                        .width(10)
                        .color(Color.BLUE)
                        .zIndex(10)
                );
                directions.mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),50));

                directions.getDirectionsButton.setVisibility(View.INVISIBLE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


