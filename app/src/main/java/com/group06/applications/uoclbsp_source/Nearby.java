package com.group06.applications.uoclbsp_source;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.tasks.OnSuccessListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class Nearby extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener {

    MaterialSearchView searchView;
    ListView lstView;
    View mapView;
    GoogleMap mMap;
    ArrayList<JSONObject> lstSource = new ArrayList<JSONObject>();
    private FusedLocationProviderClient mFusedLocationClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    public Polyline mPolyline;
    Marker marker;
    LatLng currLatLng;
    Polyline polyline;
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    ArrayList<Polyline> lanePolylines = new ArrayList<Polyline>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby);

        Toolbar toolbar2 = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar2);
        getSupportActionBar().setTitle("Nearby Search");
        toolbar2.setTitleTextColor(Color.parseColor("#FFFFFF"));

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();
        findMyLocation();



        toolbar2.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // back button pressed
                onBackPressed(); // Implemented by activity
            }
        });



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
//        new GetSearchResults().execute(new Object[]{"", MapsActivity.this, 2});
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

    public void getCanteens(View view){

        String source_name = "nearbySearch";
        String room_type = "Canteen";
        mMap.clear();
        findMyLocation();
        LatLng sourceLatLng = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
        System.out.println(sourceLatLng.toString());
        Object[] objects = new Object[]{source_name,sourceLatLng,room_type};
        new GetNearbySearchResults().execute(new Object[]{objects,Nearby.this});
    }

    public void getWashrooms(View view){
        String source_name = "nearbySearch";
        String room_type = "Washroom";
        mMap.clear();
        findMyLocation();
        LatLng sourceLatLng = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
        System.out.println(sourceLatLng.toString());
        Object[] objects = new Object[]{source_name,sourceLatLng,room_type};
        new GetNearbySearchResults().execute(new Object[]{objects,Nearby.this});
    }

    public void getLibraries(View view){
        String source_name = "nearbySearch";
        String room_type = "Library";
        mMap.clear();
        findMyLocation();
        LatLng sourceLatLng = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
        System.out.println(sourceLatLng.toString());
        Object[] objects = new Object[]{source_name,sourceLatLng,room_type};
        new GetNearbySearchResults().execute(new Object[]{objects,Nearby.this});
    }

}

class GetNearbySearchResults extends AsyncTask {

    JSONObject jsonObject;

    @Override
    protected Object doInBackground(Object[] params) {

        try {
//            String link = "http://ec2-18-216-184-231.us-east-2.compute.amazonaws.com/UoCLBSP-WebServer/Nearby_search/get_nearby_places_android";
            String link = "http://5bf3b2c5.ngrok.io/UoCLBSP-WebServer/Nearby_search/get_nearby_places_android";
            JSONObject jsonObject = new JSONObject();
            Object[] objects = (Object[]) params[0];
            jsonObject.put("source_name", objects[0]);
            jsonObject.put("source_lat", ((LatLng) objects[1]).latitude);
            jsonObject.put("source_lng", ((LatLng) objects[1]).longitude);
            jsonObject.put("room_type",objects[2]);

            String data = jsonObject.toString();

            URL url = new URL(link);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            OutputStream outputStream = conn.getOutputStream();
            OutputStreamWriter wr = new OutputStreamWriter(outputStream);


            wr.write( data );
            wr.flush();
            wr.close();
            outputStream.close();
            int responseCode = conn.getResponseCode();
            System.out.println(responseCode);
            String sb = null;

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                InputStream inputStream = conn.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);


                sb = reader.readLine();
                JSONObject jsonObject1 = new JSONObject(sb.toString());
                JSONArray jsonArray = jsonObject1.getJSONArray("result");
                params[0] = jsonArray;
            }

            else {
                sb = "Nearby "+ objects[2]+ "s not found";
                params[0] = 1;
            }


            System.out.println("Server Response : " + sb);
            return params;


        }

        catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Object obj){

        try {

            Object[] params = (Object[]) obj;

            if(params[0] instanceof JSONArray){
                Nearby nearby = (Nearby) params[1];
                JSONArray array = (JSONArray) params[0];
                System.out.println(array);

                for (int x = 0; x < array.length(); x++) {
                    System.out.println(x);
                    jsonObject = array.getJSONObject(x);
                    String source_name = jsonObject.get("name").toString();
                    LatLng sourceLatLng = new LatLng(jsonObject.getDouble("lat"), jsonObject.getDouble("lng"));
                    String description = jsonObject.get("description").toString();

                    nearby.mMap.addMarker(new MarkerOptions().position(sourceLatLng)
                            .title(source_name).snippet(description));
                    nearby.mMap.moveCamera(CameraUpdateFactory.newLatLng(sourceLatLng));


                }
            }

        }

        catch (Exception e){
            e.printStackTrace();
        }
    }
}
