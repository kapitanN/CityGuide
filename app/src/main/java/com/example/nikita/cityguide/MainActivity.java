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

    private Spinner spinner,spinner1;
    public final static String WikiCity = "City";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        Button btnSubmit;
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

        private void progress(final boolean progressFlag){
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

        private void addCountryAndCities(JSONObject allData){
            Realm realm = Realm.getDefaultInstance();
            progress(true);
            //Adding countries and cities to the local database
            try {
                for(int i = 0; i<allData.length(); i++){
                    realm.beginTransaction();
                    Country checkCountry  = realm.where(Country.class).equalTo("id",i).findFirst(); //Check country in database
                    realm.commitTransaction();
                    if (checkCountry == null){
                        realm.beginTransaction();
                        Country country = new Country();
                        country.setId(i);
                        country.setName(allData.names().getString(i));
                        Country realmCountry = realm.copyToRealmOrUpdate(country); //Adding country to DB
                        JSONArray cities = allData.getJSONArray(allData.names().getString(i)); //Get all the cities in the current country
                        for (int j = 0; j < cities.length(); j++ ){
                            City city = new City();
                            city.setId(j);
                            String name = cities.get(j).toString();
                            city.setName(name);
                            city.setCountry(country.getName());
                            City realmCity = realm.copyToRealmOrUpdate(city); //Adding city to DB
                        }
                        realm.commitTransaction();
                    }}
                realm.close();
            }catch (Exception e){
                e.printStackTrace();
            }
            progress(false);
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

                JSONObject allData = new JSONObject(resultJson); //Get all json data
                addCountryAndCities(allData);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return resultJson;
        }

        @Override
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);
            final Realm realm1;
            realm1 = Realm.getDefaultInstance();
            try {
                spinner = (Spinner) findViewById(R.id.spinner); //
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
                        Country country = realm1.where(Country.class)
                                .equalTo("name",String.valueOf(spinner.getSelectedItem())).findFirst(); //Get selected in spinner country from database
                        Log.d("Country from database",country.getName());
                        List<City> cities = realm1.where(City.class)
                                .equalTo("country",country.getName()).findAll(); //Get list of all cities in selected country
                        realm1.commitTransaction();
                        List<String> citiesList = new ArrayList<>();
                        for (int i = 0; i<cities.size(); i++){
                            citiesList.add(i,cities.get(i).getName());
                        }
                        //Set list of cities in spinner
                        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_spinner_item, citiesList);
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
