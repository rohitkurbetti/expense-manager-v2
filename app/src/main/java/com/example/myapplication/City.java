package com.example.myapplication;

import java.util.List;

public class City {
    private String city;
    private List<String> villages;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public List<String> getVillages() {
        return villages;
    }

    public void setVillages(List<String> villages) {
        this.villages = villages;
    }
}