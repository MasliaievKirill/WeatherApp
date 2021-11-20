package com.demo.weatherapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private boolean connection = false;

    private final String API_URL = "http://api.openweathermap.org/data/2.5/weather?q=%s&appid=03e3808f24e6b06be1dcbf1266c26dd6&lang=ru&units=metric";

    private EditText editText;
    private TextView textView;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getColor(R.color.blue));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        editText = findViewById(R.id.editTextTextTown);
        textView = findViewById(R.id.textViewWeather);
    }

    public void onClickSearch(View view) {
        checkConnection();
        if (connection) {
            String town = editText.getText().toString().trim();
            if (!town.isEmpty()) {
                DownloadJSONTask task = new DownloadJSONTask();
                task.execute(String.format(API_URL, town));
                Log.i("URL", String.format(API_URL, town));
            }
        } else {
            Toast.makeText(this, "Отсутствует Интернет-соединение", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkConnection () {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            connection = true;
        }
    }

    private class DownloadJSONTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            URL url = null;
            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();
            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    result.append(line);
                    line = reader.readLine();
                }
                return result.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                if (s != null) {
                    JSONObject jsonObject = new JSONObject(s);
                    String townName = jsonObject.getString("name");
                    String tempValue = jsonObject.getJSONObject("main").getString("temp");
                    String weatherDescription = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");
                    String info = String.format("Город: %s\nТемпература: %s  C°\nНа улице: %s", townName, tempValue, weatherDescription);
                    Log.i("INFO", info);
                    textView.setText(info);
                } else {
                    textView.setText("Ошибка загрузки");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }
}