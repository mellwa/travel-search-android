package edu.usc.cs.travelsearch.data;

import org.json.JSONException;
import org.json.JSONObject;

public class Location {

    public static String lon = "lon";

    public static String lat = "lat";

    private static Location location = new Location();

    private  JSONObject currentLocation = new JSONObject();

    private double userLat = 34.0244;
    private double userLon = -118.2851;
    private double fromLat = 34.0244;
    private double fromLon = -118.2851;

    private Location() {
        try {
            currentLocation.put(lat, "34.0522");
            currentLocation.put(lon, "-118.2437");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject currentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String lat, String lon) {
        try {
            currentLocation.put(this.lat, lat);
            currentLocation.put(this.lon, lon);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static Location getInstance() {
        return location;
    }

    public double getUserLat() {
        return userLat;
    }

    public void setUserLat(double userLat) {
        this.userLat = userLat;
    }

    public double getUserLon() {
        return userLon;
    }

    public void setUserLon(double userLon) {
        this.userLon = userLon;
    }

    public double getFromLat() {
        return fromLat;
    }

    public void setFromLat(double fromLat) {
        this.fromLat = fromLat;
    }

    public double getFromLon() {
        return fromLon;
    }

    public void setFromLon(double fromLon) {
        this.fromLon = fromLon;
    }
}
