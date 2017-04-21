package com.example.nikita.cityguide;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by nikita on 19.04.2017.
 */

public class City extends RealmObject{


    private int id;
    @PrimaryKey
    private String name;
    private String country;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public City() {
    }

    public City(int id, String name, String country) {
        this.id = id;
        this.name = name;
        this.country = country;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
