package com.eei4369.mp.atmosphre;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import android.location.Location;
import android.Manifest;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;


public class Second_page extends FragmentActivity implements OnMapReadyCallback,
        LocationListener,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private GoogleMap mMap;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    TextView tvResult,tvAqi,tvRec,tvDec,tvHealth;

    private final String url = "https://api.openweathermap.org/data/2.5/air_pollution";
    private final String appid = "c4ed5ac74e73adf2c0d07fae8ad5575c";

    private final String url1 = "https://api.airvisual.com/v2/nearest_city";
    private final String appid1 = "ae0096a3-a775-4ece-80fb-b8787d6f3c71";

    DecimalFormat df = new DecimalFormat("#.##");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_page);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        tvResult = findViewById(R.id.tvResult);
        tvAqi = findViewById(R.id.tvAqi);
        tvRec = findViewById(R.id.tvRec);
        tvDec = findViewById(R.id.tvDec);
        tvHealth = findViewById(R.id.tvHealth);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(Second_page.this, First_page.class));

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void searchLocation(View view) {
        EditText locationSearch = (EditText) findViewById(R.id.editText);
        String location = locationSearch.getText().toString();
        List<Address> addressList = null;
            if (location != null || !location.equals("")) {
                Geocoder geocoder = new Geocoder(this);
                try {
                    addressList = geocoder.getFromLocationName(location, 1);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                Toast.makeText(getApplicationContext(), address.getLatitude() + " " + address.getLongitude(), Toast.LENGTH_LONG).show();

                double lat = address.getLatitude();
                double lon = address.getLongitude();
                String tempUrl = "";
                String tempUrl1 = "";

                tempUrl = url + "?lat=" + lat + "&lon=" + lon + "&appid=" + appid;
                tempUrl1 = url1 + "?lat=" + lat + "&lon=" + lat + "&key=" + appid1;

                StringRequest stringRequest = new StringRequest
                        (Request.Method.GET, tempUrl, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                String output = "";
                                String dec = "";
                                try {
                                    JSONObject jsonResponse = new JSONObject(response);
                                    JSONArray jsonArray = jsonResponse.getJSONArray("list");
                                    JSONObject jsonObjectList = jsonArray.getJSONObject(0);
                                    JSONObject jsonObjectComponents = jsonObjectList.getJSONObject("components");
                                    float co = jsonObjectComponents.getInt("co");
                                    float no = jsonObjectComponents.getInt("no");
                                    float no2 = jsonObjectComponents.getInt("no2");
                                    float o3 = jsonObjectComponents.getInt("o3");
                                    float so2 = jsonObjectComponents.getInt("so2");
                                    float pm2_5 = jsonObjectComponents.getInt("pm2_5");
                                    float pm10 = jsonObjectComponents.getInt("pm10");
                                    float nh3 = jsonObjectComponents.getInt("nh3");
                                    tvResult.setTextColor(Color.rgb(88, 24, 69));
                                    dec +=  "\n Carbon monoxide(co): "
                                            + "\n Nitrogen monoxide(no): "
                                            + "\n Nitrogen dioxide(no2): "
                                            + "\n Ozone(o3): "
                                            + "\n Sulphur dioxide(so2): "
                                            + "\n Fine particles matter(pm2_5): "
                                            + "\n Coarse particulate matter(pm10): "
                                            + "\n Ammonia(nh3): ";

                                    output +="\n" + df.format(co) + " μg/m3"
                                            + "\n" + df.format(no) + " μg/m3"
                                            + "\n" + df.format(no2) + " μg/m3"
                                            + "\n" + df.format(o3) + " μg/m3"
                                            + "\n" + df.format(so2) + " μg/m3"
                                            + "\n" + pm2_5 + " μg/m3"
                                            + "\n" + df.format(pm10) + " μg/m3"
                                            + "\n" + df.format(nh3) + " μg/m3";

                                    tvDec.setText(dec);
                                    tvResult.setText(output);
                                } catch (JSONException e) {
                                    e.printStackTrace();

                                }

                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getApplicationContext(), error.toString().trim(), Toast.LENGTH_SHORT).show();
                            }
                        });

                StringRequest stringRequest1 = new StringRequest
                        (Request.Method.GET, tempUrl1, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonResponse = new JSONObject(response);
                                    JSONObject jsonObjectList = jsonResponse.getJSONObject("data");
                                    JSONObject jsonObjectComponents = jsonObjectList.getJSONObject("current");
                                    JSONObject jsonObjectPosition = jsonObjectComponents.getJSONObject("pollution");
                                    int aqi_data = jsonObjectPosition.getInt("aqius");

                                    if(aqi_data<50){
                                        tvAqi.setText("Good");
                                        tvAqi.setBackgroundColor(Color.GREEN);
                                        tvHealth.setText("Air quality is considered satisfactory, and air pollution poses little or no risk");

                                        Integer img = R.mipmap.good_foreground;
                                        ImageView iView = (ImageView) findViewById(R.id.aqiImg);
                                        iView.setImageResource(img);
                                    }
                                    else if(aqi_data>=51 && aqi_data<100){
                                        tvAqi.setText("Moderate");
                                        tvAqi.setBackgroundColor(Color.YELLOW);
                                        tvHealth.setText("Health warning \nAir quality is acceptable; however, for some pollutants there may be a moderate health concern for a very small number of people who are unusually sensitive to air pollution. Active children and adults, and people with respiratory disease, such as asthma, should limit prolonged outdoor exertion.");

                                        Integer img1 = R.mipmap.unhealthy_foreground;
                                        ImageView iView1 = (ImageView) findViewById(R.id.aqiImg);
                                        iView1.setImageResource(img1);
                                    }
                                    else if(aqi_data>=101 && aqi_data<150){
                                        tvAqi.setText("Unhealthy for Sensitive Groups");
                                        tvAqi.setBackgroundColor(Color.CYAN);
                                        tvHealth.setText("Health warning \nMembers of sensitive groups may experience health effects. The general public is not likely to be affected. Active children and adults, and people with respiratory disease, such as asthma, should limit prolonged outdoor exertion.");

                                        Integer img3 = R.mipmap.unhealthy_foreground;
                                        ImageView iView3 = (ImageView) findViewById(R.id.aqiImg);
                                        iView3.setImageResource(img3);
                                    }
                                    else if(aqi_data>=151 && aqi_data<200){
                                        tvAqi.setText("Unhealthy");
                                        tvAqi.setBackgroundColor(Color.RED);
                                        tvHealth.setText("Health warning \nEveryone may begin to experience health effects members of sensitive groups may experience more serious health effects. Active children and adults, and people with respiratory disease, such as asthma, should avoid prolonged outdoor exertion; everyone else, especially children, should limit prolonged outdoor exertion");

                                        Integer img4 = R.mipmap.unhealthy_ii_foreground;
                                        ImageView iView4 = (ImageView) findViewById(R.id.aqiImg);
                                        iView4.setImageResource(img4);
                                    }
                                    else if(aqi_data>=201 && aqi_data<300){
                                        tvAqi.setText("Very unhealthy");
                                        tvAqi.setBackgroundColor(Color.MAGENTA);
                                        tvHealth.setText("Health warning \nemergency conditions. The entire population is more likely to be affected.Active children and adults, and people with respiratory disease, such as asthma, should avoid all outdoor exertion; everyone else, especially children, should limit outdoor exertion.");

                                        Integer img5 = R.mipmap.very_unhealthy_foreground;
                                        ImageView iView5 = (ImageView) findViewById(R.id.aqiImg);
                                        iView5.setImageResource(img5);

                                    }else{
                                        tvAqi.setText("Hazardous");
                                        tvAqi.setBackgroundColor(Color.LTGRAY);
                                        tvHealth.setText("Health warning \neveryone may experience more serious health effects Everyone should avoid all outdoor exertion");

                                        Integer img6 = R.mipmap.defult_foreground;
                                        ImageView iView6 = (ImageView) findViewById(R.id.aqiImg);
                                        iView6.setImageResource(img6);
                                    }
                                    tvRec.setTextColor(Color.rgb(68, 134, 199));
                                    tvRec.setText(Integer.toString(aqi_data));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getApplicationContext(), error.toString().trim(), Toast.LENGTH_SHORT).show();
                            }
                        });
                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                requestQueue.add(stringRequest);
                requestQueue.add(stringRequest1);
            }
        }
    }

