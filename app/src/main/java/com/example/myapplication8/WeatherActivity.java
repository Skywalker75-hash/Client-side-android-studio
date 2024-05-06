package com.example.myapplication8;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class WeatherActivity extends AppCompatActivity {
    private TextView shiduTextView, highTempTextView, lowTempTextView, weatherTypeTextView, windDirectionTextView, windLevelTextView;
    private Spinner spinnerCity;
    private ArrayAdapter<String> cityAdapter;
    private HashMap<String, String> WeatherCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        shiduTextView = findViewById(R.id.shidu);
        highTempTextView = findViewById(R.id.zuigaowendu);
        lowTempTextView = findViewById(R.id.zuidiwendu);
        weatherTypeTextView = findViewById(R.id.type);
        windDirectionTextView = findViewById(R.id.fx);
        windLevelTextView = findViewById(R.id.fl);

        initCities();
        spinnerCity = findViewById(R.id.spinnerCity);
        cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>(WeatherCode.keySet()));
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(cityAdapter);
        Button searchButton = findViewById(R.id.buttonSearch);

        searchButton.setOnClickListener(v -> {
            String selectedCity = spinnerCity.getSelectedItem().toString();
            String weatherUrl = "http://t.weather.itboy.net/api/weather/city/" + WeatherCode.get(selectedCity);
            new FetchWeatherTask().execute(weatherUrl);
        });



    }

    private void initCities() {
        WeatherCode = new HashMap<>();
        WeatherCode.put("南京", "101190101");
        WeatherCode.put("无锡", "101190201");
        WeatherCode.put("镇江", "101190301");
        WeatherCode.put("苏州","101190401");
        WeatherCode.put("南通","101190501");
        WeatherCode.put("扬州","101190601");
        WeatherCode.put("盐城","101190701");
        WeatherCode.put("徐州","101190801");
        WeatherCode.put("淮安","101190901");
        WeatherCode.put("连云港","101191001");
        WeatherCode.put("常州","101191101");
        WeatherCode.put("泰州","101191201");
        WeatherCode.put("宿迁","101191301");
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String> {
        private static final String TAG = "FetchWeatherTask";

        @Override
        protected String doInBackground(String... urls) {
            try {
                Log.d(TAG, "Starting download from URL: " + urls[0]);
                String result = downloadUrl(urls[0]);
                Log.d(TAG, "Downloaded successfully.");
                return result;
            } catch (IOException e) {
                Log.e(TAG, "Failed to download URL: " + e.toString());
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject dataObject = jsonObject.getJSONObject("data");
                JSONObject todayForecast = dataObject.getJSONArray("forecast").getJSONObject(0);

                Log.d(TAG, "JSON parsed successfully");

                shiduTextView.setText("湿度: " + dataObject.getString("shidu"));
                highTempTextView.setText("最高温度: " + todayForecast.getString("high"));
                lowTempTextView.setText("最低温度: " + todayForecast.getString("low"));
                weatherTypeTextView.setText("天气: " + todayForecast.getString("type"));
                windDirectionTextView.setText("风向: " + todayForecast.getString("fx"));
                windLevelTextView.setText("风力: " + todayForecast.getString("fl"));
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing JSON data: " + e.toString());
                Toast.makeText(WeatherActivity.this, "Error parsing JSON data.", Toast.LENGTH_SHORT).show();
            }
        }

        private String downloadUrl(String urlString) throws IOException {
            InputStream is = null;
            HttpURLConnection conn = null;
            try {
                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(TAG, "The response code is: " + response);

                is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                return builder.toString();
            } finally {
                if (is != null) {
                    is.close();
                }
                if (conn != null) {
                    conn.disconnect();
                    Log.d(TAG, "Disconnected from server");
                }
            }
        }
    }

}