package com.example.myapplication;

import java.util.List;

public class District {
    private String district;
    private List<Taluka> talukas;

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public List<Taluka> getTalukas() {
        return talukas;
    }

    public void setTalukas(List<Taluka> talukas) {
        this.talukas = talukas;
    }
}