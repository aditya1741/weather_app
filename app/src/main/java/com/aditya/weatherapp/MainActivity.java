package com.aditya.weatherapp;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    private RelativeLayout homeRl;
    private ProgressBar loadingProgBar;
    private TextView cityNameTv, temperatureTv, conditionTv, minTempTv, maxTempTv, sunriseTv, sunsetTv, windspeedTv, feelLikeTv;
    private TextInputEditText cityNameSearchEdt;
    private ImageView conditionIv, searchIv;
    private RecyclerView forecastRclv;
    private LottieAnimationView ltAv;


    private LocationManager locationManager;
    private String cityName;
    ArrayList<CurrentWeather> listCurrentWeather;
    private final static int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private int isStart = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        homeRl = findViewById(R.id.rlHome);
        loadingProgBar = findViewById(R.id.Progress_bar_loading);
        cityNameTv = findViewById(R.id.txtvCityName);
        temperatureTv = findViewById(R.id.txtv_temprature);
        conditionTv = findViewById(R.id.txtv_weather_condition);
        minTempTv = findViewById(R.id.txtv_min_temp);
        maxTempTv = findViewById(R.id.txtv_max_temp);
        feelLikeTv = findViewById(R.id.txtv_FeelLike);
        sunriseTv = findViewById(R.id.txtv_sunrise);
        sunsetTv = findViewById(R.id.txtv_sunset);
        windspeedTv = findViewById(R.id.txtv_windspeed);
        cityNameSearchEdt = findViewById(R.id.city_Search_Input_edt);
        conditionIv = findViewById(R.id.weather_condition_image);
        searchIv = findViewById(R.id.city_name_search_iv);
        ltAv = findViewById(R.id.lottie_loading);
        listCurrentWeather = new ArrayList<>();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }


        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        assert location != null;
        cityName = getCityNameFromLocation(location.getLatitude(), location.getLongitude());
        if (isStart == 1){
            ltAv.setVisibility(View.VISIBLE);
            getWeatherInfo(cityName);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!listCurrentWeather.isEmpty()) {
                        CurrentWeather currentWeather = listCurrentWeather.get(0);
                        Picasso.get().load(currentWeather.getIcon()).into(conditionIv);
                        conditionTv.setText(currentWeather.getCondition());
                        temperatureTv.setText(currentWeather.getTemperature());
                        cityNameTv.setText(currentWeather.getCityName());
                        sunriseTv.setText(currentWeather.getSunriseTime());
                        sunsetTv.setText(currentWeather.getSunsetTime());
                        maxTempTv.setText("Max temp "+ currentWeather.getMaxTemp() + "℃");
                        minTempTv.setText("Min temp "+ currentWeather.getMinTemp() + "℃");
                        feelLikeTv.setText("FeelsLike " +currentWeather.getFeelsLike() + "℃");
                        windspeedTv.setText("Wind Speed " + currentWeather.getWindSpeed());
                        ltAv.setVisibility(View.GONE);
                        listCurrentWeather.clear();
                        isStart = 0;
                    } else {
                        Toast.makeText(MainActivity.this, "Please Enter Another city", Toast.LENGTH_SHORT).show();
                    }
                }
            },3000);
        }
        searchIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = Objects.requireNonNull(cityNameSearchEdt.getText()).toString();
                if (city.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter city name", Toast.LENGTH_SHORT).show();
                } else {
                    ltAv.setVisibility(View.VISIBLE);
                    cityName = city;
                    getWeatherInfo(cityName);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!listCurrentWeather.isEmpty()) {
                                CurrentWeather currentWeather = listCurrentWeather.get(0);
                                Picasso.get().load(currentWeather.getIcon()).into(conditionIv);
                                conditionTv.setText(currentWeather.getCondition());
                                temperatureTv.setText(currentWeather.getTemperature());
                                cityNameTv.setText(currentWeather.getCityName());
                                sunriseTv.setText(currentWeather.getSunriseTime());
                                sunsetTv.setText(currentWeather.getSunsetTime());
                                maxTempTv.setText("Max temp "+ currentWeather.getMaxTemp() + "℃");
                                minTempTv.setText("Min temp "+ currentWeather.getMinTemp() + "℃");
                                feelLikeTv.setText("FeelsLike " +currentWeather.getFeelsLike() + "℃");
                                windspeedTv.setText("Wind Speed " + currentWeather.getWindSpeed());
                                ltAv.setVisibility(View.GONE);
                                listCurrentWeather.clear();
                            } else {
                                Toast.makeText(MainActivity.this, "Please Enter Another city", Toast.LENGTH_SHORT).show();
                            }
                        }
                    },3000);

                }
            }
        });


    }

    private void getWeatherInfo(String cityName) {
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&appid=75198cc0a6716def903f57ab7002d00b";

        Request getRequest = new Request.Builder()
                .url(url)
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(getRequest).enqueue(new Callback() {


            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    listCurrentWeather.add(getWeatherData(response.body().string()));
                    // Handle the response data

                }
            }


            @Override
            public void onFailure(Call call, IOException e) {
                // Handle the failure
                Log.d("Fail", "Failure Response");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location Permission Granted", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "Please grant location permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityNameFromLocation(double latitude, double longitude) {
        String cityLocName = "Not Found";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        System.out.println(geocoder);
        List<Address> addressList;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                addressList = geocoder.getFromLocation(latitude, longitude, 1);
            } else {
                addressList = geocoder.getFromLocation(latitude, longitude, 10);
            }
            for (Address address : addressList) {
                if (address != null) {
                    String city = address.getLocality();
                    if (city != null && !city.isEmpty()) {
                        cityLocName = city;
                    } else {
                        cityNameTv.setText(cityLocName);
                        Log.w("Log city", "Location: City Not Found!");
                        Toast.makeText(this, "Location: City Not Found!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityLocName;

    }

    public CurrentWeather getWeatherData(String jsonData) {
        CurrentWeather currentWeather = null;
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            String currentTemp = jsonObject.getJSONObject("main").getString("temp");
            String tempC = new DecimalFormat("##.##").format(Double.parseDouble(currentTemp) - 273.15);
            String condition = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");
            String conditionIconPath = jsonObject.getJSONArray("weather").getJSONObject(0).getString("icon");
            String iconPath = "https://openweathermap.org/img/wn/" + conditionIconPath + "@2x.png";
            String cityName = jsonObject.getString("name");
            String sunriseTime = epochToLocalDate(jsonObject.getJSONObject("sys").getString("sunrise"), jsonObject.getString("timezone"));
            String sunsetTime = epochToLocalDate(jsonObject.getJSONObject("sys").getString("sunset"), jsonObject.getString("timezone"));
            String tempMin = kelvinToCelcius(jsonObject.getJSONObject("main").getString("temp_min"));
            String tempMax = kelvinToCelcius(jsonObject.getJSONObject("main").getString("temp_max"));
            String feelsLike = kelvinToCelcius(jsonObject.getJSONObject("main").getString("feels_like"));
            String windSpeed = String.valueOf(new DecimalFormat("##.##").format(Double.parseDouble(jsonObject.getJSONObject("wind").getString("speed")) * 1.852) )+ "km/h";

            currentWeather = new CurrentWeather(condition, tempC + "℃", iconPath, cityName, sunriseTime, sunsetTime, tempMin, tempMax, windSpeed, feelsLike);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return currentWeather;
    }

    private String kelvinToCelcius(String kelvin) {
        return new DecimalFormat("##.##").format(Double.parseDouble(kelvin) - 273.15);
    }

    private String epochToLocalDate(String epochMilliSecond, String epochOffset) {
        long longMilliSecond = Long.parseLong(epochMilliSecond);
        int longOffset = Integer.parseInt(epochOffset);
        Instant instant = Instant.ofEpochSecond(longMilliSecond);
        ZoneOffset zoneOffset = ZoneOffset.ofTotalSeconds(longOffset);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, zoneOffset);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm a");
        return dateTime.format(timeFormatter);
    }


}



