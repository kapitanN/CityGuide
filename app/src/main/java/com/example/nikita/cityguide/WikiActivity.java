package com.example.nikita.cityguide;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WikiActivity extends AppCompatActivity {

    public static String LOG_TAG = "Wiki_Log";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wiki);
        new GeonamesTask().execute();
    }

    private class GeonamesTask extends AsyncTask<Void, Void, String> {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";
        URL url = null;

        public void progress(final boolean progressFlag){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
                    if (progressFlag){
                        progressBar.setVisibility(ProgressBar.VISIBLE);
                    }
                    else {
                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                    }
                }
            });
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Intent intent = getIntent();
            String spinner = intent.getStringExtra(MainActivity.WikiCity);
            String urlString = "http://api.geonames.org/wikipediaSearchJSON?q="+spinner+"&maxRows=1000&username=kapitann";
            try {
                url = new URL(urlString);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        protected String doInBackground(Void... params) {
            progress(true);
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                resultJson = buffer.toString();
            }
            catch (Exception e){
                e.printStackTrace();
            }
            progress(false);
            return resultJson;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(LOG_TAG, s);
            try {
                JSONObject object = new JSONObject(s);
                JSONArray geonames = object.getJSONArray("geonames");
                JSONObject firstArray = geonames.getJSONObject(0);
                String wikiUrl = firstArray.getString("wikipediaUrl");
                String summary = firstArray.getString("summary") + "\n\n" + wikiUrl;
                String noData = "Sorry, wikipedia doesn't have data for this city";
                TextView textView = new TextView(WikiActivity.this);
                textView.setTextSize(25);
                if (!summary.equals("")){
                    textView.setText(summary);
                    Log.d("summary",summary + "\n" + wikiUrl);
                }
                else {
                    textView.setText(noData);
                }
                Linkify.addLinks(textView,Linkify.ALL);
//                 Устанавливаем текстовое поле в системе компоновки activity
                setContentView(textView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
