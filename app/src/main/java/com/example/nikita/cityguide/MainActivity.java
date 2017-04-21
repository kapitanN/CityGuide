package com.example.nikita.cityguide;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;


public class MainActivity extends AppCompatActivity {


    public static String LOG_TAG = "my_log";
    private Spinner spinner,spinner1;
    private Button btnSubmit;
    public final static String WikiCity = "City";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("on create", "test");
        super.onCreate(savedInstanceState);
        RealmConfiguration config = new RealmConfiguration.Builder(this)
                .deleteRealmIfMigrationNeeded()
                .schemaVersion(1)
                .build();
        Realm.setDefaultConfiguration(config);
        setContentView(R.layout.activity_main);
        new ParseTask().execute();
        addListenerOnButton();
        addListenerOnSpinnerItemSelection();
    }

    public void addListenerOnSpinnerItemSelection() {
        spinner1 = (Spinner)findViewById(R.id.spinner1);
        spinner1.setOnItemSelectedListener(new CustomOnItemSelectedListener());
    }

    public void addListenerOnButton() {

        spinner = (Spinner) findViewById(R.id.spinner);
        spinner1 = (Spinner) findViewById(R.id.spinner1);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,WikiActivity.class);
                intent.putExtra(WikiCity,String.valueOf(spinner1.getSelectedItem()));
                startActivity(intent);
            }

        });
    }


    private class ParseTask extends AsyncTask<Void, Void, String> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";
        private Realm realm;


        public void progress(final boolean progressFlag){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar2);
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
        protected String doInBackground(Void... params) {
            // получаем данные с внешнего ресурса
            try {
                URL url = new URL("https://raw.githubusercontent.com/David-Haim/CountriesToCitiesJSON/master/countriesToCities.json");

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
                JSONObject dataJsonObj = new JSONObject(resultJson);
                realm = Realm.getDefaultInstance();
                progress(true);
                //Добавляем страны и города из json в локальную базу данных
                for(int i = 0; i<dataJsonObj.length(); i++){
                    realm.beginTransaction();
                    Country checkCountry  = realm.where(Country.class).equalTo("id",i).findFirst();
                    realm.commitTransaction();
                    if (checkCountry == null){
                        realm.beginTransaction();
                        Log.d("country circle", "" + dataJsonObj.names().getString(i));
                        Country country = new Country();
                        country.setId(i);
                        country.setName(dataJsonObj.names().getString(i));
                        Country realmCountry = realm.copyToRealmOrUpdate(country);
                        JSONArray cities = dataJsonObj.getJSONArray(dataJsonObj.names().getString(i));
                        for (int j = 0; j < cities.length(); j++ ){
                            Log.d("city circle", "" + cities.get(j).toString());
                            City city = new City();
                            city.setId(j);
                            String name = cities.get(j).toString();
                            city.setName(name);
                            city.setCountry(country.getName());
                            City realmCity = realm.copyToRealmOrUpdate(city);
                        }
                        realm.commitTransaction();
                    }}
                progress(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            realm.close();
            return resultJson;
        }

        @Override
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);
            // выводим целиком полученную json-строку
            Log.d(LOG_TAG, strJson);
            final Realm realm1;
            realm1 = Realm.getDefaultInstance();
            JSONObject dataJsonObj = null;
            try {
                spinner = (Spinner) findViewById(R.id.spinner);
                spinner1 = (Spinner) findViewById(R.id.spinner1);
                List<String> list = new ArrayList<>();
                realm1.beginTransaction();
                final List<Country> countries = realm1.where(Country.class).findAll();
                realm1.commitTransaction();
                for (int i = 0; i<countries.size(); i++){
                    list.add(i,countries.get(i).getName());
                }

                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(MainActivity.this,
                        android.R.layout.simple_spinner_item, list);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(dataAdapter);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        realm1.beginTransaction();
                        Country country = realm1.where(Country.class).equalTo("name",String.valueOf(spinner.getSelectedItem())).findFirst();
                        Log.d("Country from database",country.getName());
                        RealmResults<City> cities = realm1.where(City.class).equalTo("country",country.getName()).findAll();
                        realm1.commitTransaction();
                        List<String> citiesList = new ArrayList<>();
                        for (int i = 0; i<cities.size(); i++){
                            citiesList.add(i,cities.get(i).getName());
                        }
                        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(MainActivity.this,
                                android.R.layout.simple_spinner_item, citiesList);
                        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner1.setAdapter(dataAdapter);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
