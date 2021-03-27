package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

//implementing the OnSuccessListener in this activity since there is only a single callback.
public class MainActivity extends AppCompatActivity implements OnSuccessListener<Location> {

    //Bundle key
    public static final String DATA_TAG = "weather.data.go";

    //Used for getting location
    private FusedLocationProviderClient fusedLocationClient;

    //Declare UI elements
    private Button goButton;
    private ImageButton locButton;
    private TextView idTextView;
    private EditText locEditText;
    public static WeatherData data = new WeatherData();

    private Context context;



    //Volley request queue;
    private RequestQueue queue;

    //LOCATION REQUEST CODE KEY, not important since only asking for a single permission and don't need to distinguish
    public static final int LOCATION_REQUEST_CODE = 11;

    private final int REQ_CODE = 100;
    TextView textView;

   /* @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.text);
        ImageView speak = findViewById(R.id.speak);
        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                        "Need to speak");
                try {
                    startActivityForResult(intent, REQ_CODE);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(getApplicationContext(),
                            "Sorry your device not supported",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    textView.setText(result.get(0));
                }
                break;
            }
        }
    }
*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //Remove that uggo TitleBar
        getSupportActionBar().hide();

        //Set context
        context = getApplicationContext();


        //Attach references to UI elements
        goButton = findViewById(R.id.goButton);
        locButton = findViewById(R.id.locButton);
        idTextView = findViewById(R.id.textView2);
        locEditText = findViewById(R.id.locEditText);
        ImageView speak = findViewById(R.id.speak);

        //Instantiate the request queue
        queue = Volley.newRequestQueue(this);





    }

    public static WeatherData getWeatherInstance(){
        return data;
    }

    public void onLocClick(View v){
        System.out.println("Location Button Clicked");

        //Ask for location permission
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            getLocation();
            System.out.println("PERMISSION ALREADY GRANTED FOR LOCATION, DOING ACTION");
        }else {
            //directly ask for the permission.
            requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION },LOCATION_REQUEST_CODE);


        }
    }

    public void onGoClick(View v){
        System.out.println("Go Button Clicked");
        String text = locEditText.getText().toString();
        int zipcode;
        try{
            System.out.println("ZIPCODE DETECTED!");
            zipcode = Integer.parseInt(text);
            getWeatherByZip(zipcode);
        }
        catch(Exception e){
            //Opps, try by city
            System.out.println("CITY DETECTED!");
            getWeatherByCity(text);
        }

    }



    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        System.out.println("onRequestPermissionsResult Callback Entered");

        //Check that permission was granted
        if (grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED){
            getLocation();
        }


    }

    @SuppressLint("MissingPermission")
    private void getLocation(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this,this);


    }

    private final int REQ_CODE = 100;
    private TextView inputTextView;


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_main, container, false);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE) {
            if (resultCode == Activity.RESULT_OK && null != data) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                assert result != null;
                inputTextView.setText(result.get(0));
            }
        }
    }


    @Override
    //onSuccess for Location Services
    public void onSuccess(Location location) {
        //Get Weather by Location
        String lat = String.valueOf(location.getLatitude());
        String lon = String.valueOf(location.getLongitude());
        System.out.println("Lattitude = " + lat);
        System.out.println("Longitude = " + lon);
        data.setLat(lat);
        data.setLon(lon);
        getWeatherByLocation(lat,lon);

    }



    public void getWeatherByCity(String city){
        String url = getString(R.string.WEATHER_API_URL_CITY) + city + getString(R.string.WEATHER_API_KEY);
        System.out.println(url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        JSONObject main = null;
                        try {
                            main = response.getJSONObject("main");
                            JSONObject coords = response.getJSONObject("coord");
                            data.setLat(coords.getString("lat"));
                            data.setLon(coords.getString("lon"));
                            data.setTemp(main.getString("temp"));
                            data.setFeelsLike(main.getString("feels_like"));
                            data.setCityName(response.getString("name"));
                            data.setTempMax(main.getString("temp_max"));
                            data.setTempMin(main.getString("temp_min"));
                            //TODO : Bundle the weather object and send to next activity
                            //Current implementation is just using static member

                            Intent intent = new Intent(context, DisplayActivity.class);
                            startActivity(intent);




                        } catch (JSONException e) {
                            System.out.println("JSON EXPLOSION");
                        }


                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("ERROR WITH VOLLEY REQUEST");

                    }
                });

        queue.add(jsonObjectRequest);

    }

    public void getWeatherByZip(int zip){
        String zipcode = String.valueOf(zip);
        String url = getString(R.string.WEATHER_API_URL_ZIP) + zipcode + getString(R.string.WEATHER_API_KEY);
        System.out.println(url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        JSONObject main = null;
                        try {
                            main = response.getJSONObject("main");
                            JSONObject coords = response.getJSONObject("coord");
                            data.setLat(coords.getString("lat"));
                            data.setLon(coords.getString("lon"));
                            data.setTemp(main.getString("temp"));
                            data.setFeelsLike(main.getString("feels_like"));
                            data.setCityName(response.getString("name"));
                            data.setTempMax(main.getString("temp_max"));
                            data.setTempMin(main.getString("temp_min"));
                            //TODO : Bundle the weather object and send to next activity
                            //Current implementation is just using static member

                            Intent intent = new Intent(context, DisplayActivity.class);
                            startActivity(intent);




                        } catch (JSONException e) {
                            System.out.println("JSON EXPLOSION");
                        }


                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("ERROR WITH VOLLEY REQUEST");

                    }
                });

        queue.add(jsonObjectRequest);




    }

    public void getWeatherByLocation(String lat,String lon){
        String url = getString(R.string.WEATHER_API_URL_LAT) + lat + getString(R.string.WEATHER_LON_SUFFIX) + lon
                + getString(R.string.WEATHER_API_KEY);

        System.out.println(url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        JSONObject main = null;
                        try {
                            main = response.getJSONObject("main");
                            data.setTemp(main.getString("temp"));
                            data.setFeelsLike(main.getString("feels_like"));
                            data.setCityName(response.getString("name"));
                            data.setTempMax(main.getString("temp_max"));
                            data.setTempMin(main.getString("temp_min"));
                            //TODO : Bundle the weather object and send to next activity
                            //Current implementation is just using static member

                            Intent intent = new Intent(context, DisplayActivity.class);
                            startActivity(intent);




                        } catch (JSONException e) {
                            System.out.println("JSON EXPLOSION");
                        }


                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("ERROR WITH VOLLEY REQUEST");

                    }
                });

        queue.add(jsonObjectRequest);
    }
}