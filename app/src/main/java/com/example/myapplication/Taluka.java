package com.example.myapplication;

import java.util.List;

public class Taluka {
    private String taluka;
    private List<City> cities;

    public String getTaluka() {
        return taluka;
    }

    public void setTaluka(String taluka) {
        this.taluka = taluka;
    }

    public List<City> getCities() {
        return cities;
    }

    public void setCities(List<City> cities) {
        this.cities = cities;
    }
}