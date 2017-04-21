package com.example.nikita.cityguide;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by nikita on 19.04.2017.
 */

public class Country extends RealmObject{

    @PrimaryKey
    private int id;
    private String name;

    public Country() {
    }

    public Country(int id,String name) {
        this.name = name;
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
