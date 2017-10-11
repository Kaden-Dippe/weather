package com.example.kadendippe.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.example.kadendippe.myapplication.R.drawable.cloudy_nosun;
import static com.example.kadendippe.myapplication.R.drawable.moon;
import static com.example.kadendippe.myapplication.R.drawable.snow;
import static com.example.kadendippe.myapplication.R.drawable.storm_1;
import static com.example.kadendippe.myapplication.R.drawable.sun_cloudy;
import static com.example.kadendippe.myapplication.R.drawable.sunny;

public class MainActivity extends AppCompatActivity {

    FusedLocationProviderClient mFusedLocationClient;

    Context context;

    String provider;

    String slam;

    TextView time;
    TextView date;
    ImageView icon;
    TextView temperature;
    TextView summary;
    TextView rain;

    double longitude;
    double latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //time = (TextView) findViewById(R.id.time);
        //date = (TextView) findViewById(R.id.date);
        icon = (ImageView) findViewById(R.id.icon);
        temperature = (TextView) findViewById(R.id.temperature);
        summary = (TextView) findViewById(R.id.summary);
        rain = (TextView) findViewById(R.id.rain);

        //clear-day, clear-night, rain, snow, sleet, wind, fog, cloudy, partly-cloudy-day, or partly-cloudy-night.

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_CALENDAR);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    1);

        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            longitude = location.getLongitude();
                            latitude = location.getLatitude();
                            getWeather w = new getWeather(latitude,longitude);
                            w.execute();
                        }
                    }
                });


    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //execute utils class

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    class getWeather extends AsyncTask<Void, Void, JSONObject> {

        String API_URL = "https://api.darksky.net/forecast/";

        String API_KEY = "5295128303f41a3686541b7daee5fdd4/";

        String Lat;
        String Long;

        //context, cast to an activity, then acess
        getWeather(Double Lat, Double Long) {
            this.Lat = Lat.toString() + ",";
            this.Long = Long.toString();
        }

        private Exception exception;

        protected JSONObject doInBackground(Void... urls) {
            // Do some validation here
            try {
                URL url = new URL(API_URL + API_KEY + Lat + Long);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuffer json = new StringBuffer(1024);
                String temp = "";
                while ((temp = reader.readLine()) != null) {
                    json.append(temp).append("\n");
                }
                reader.close();
                JSONObject data = new JSONObject(json.toString());
                return data;
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }
        protected void onPostExecute(JSONObject response) {
            if (response == null) {
                Log.e("ERROR", "There was an error");
            }
            try {
                String curr = response.getJSONObject("currently").getString("temperature");
                int temp = (int) Float.parseFloat(curr);
                temperature.setText(temp + "\u00b0 F");
                String description = response.getJSONObject("minutely").getString("summary");
                summary.setText(description);
                JSONArray minutes = response.getJSONObject("minutely").getJSONArray("data");

                //pass into date constructer, multiply by 1,000, and then
                /*
                for(int i = 0; i<60; i++){
                    JSONObject minData = minutes.getJSONObject(i);
                    minData.getInt("percipProbability");
                }
                */
                JSONArray data = response.getJSONObject("daily").getJSONArray("data");

                JSONObject minData = data.getJSONObject(2);

                String ic = minData.getString("icon");

                //clear-day, clear-night, rain, snow, sleet, wind, fog, cloudy, partly-cloudy-day, or partly-cloudy-night.

                switch(ic){
                    case "clear-day":
                        icon.setImageResource(sunny);
                        break;
                    case "clear-night":
                        icon.setImageResource(moon);
                        break;
                    case "rain":
                        icon.setImageResource(storm_1);
                        break;
                    case "snow":
                        icon.setImageResource(snow);
                        break;
                    case "sleet":
                        icon.setImageResource(snow);
                        break;
                    case "wind":
                        icon.setImageResource(cloudy_nosun);
                        break;
                    case "fog":
                        icon.setImageResource(cloudy_nosun);
                        break;
                    case "cloudy":
                        icon.setImageResource(cloudy_nosun);
                        break;
                    case " partly-cloudy-day":
                        icon.setImageResource(sun_cloudy);
                        break;
                    case "partly-cloudy-night":
                        icon.setImageResource(cloudy_nosun);
                        break;
                    default:
                        icon.setImageResource(cloudy_nosun);

                }
            } catch (Exception e) {
                e.getMessage();
            }
        }
    }
}
