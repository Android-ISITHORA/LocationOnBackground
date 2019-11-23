package com.crcodings.backgroundservice;

public class LocationModel {

    private String Loc_Id;
    private String latitude;
    private String longitude;

    public String getLoc_Id() {
        return Loc_Id;
    }

    public void setLoc_Id(String loc_Id) {
        Loc_Id = loc_Id;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
