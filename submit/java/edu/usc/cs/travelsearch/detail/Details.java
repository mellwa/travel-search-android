package edu.usc.cs.travelsearch.detail;

import android.content.Intent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Details {
    private String name, address, phone, googlePage, website, placeId;
    private double rating = -1;
    private int price = -1;
    double lat = -181;
    double lon = -181;
    private List<GoogleReview> googleReviewList = new ArrayList<>();
    private List<YelpReview> yelpReviewList = new ArrayList<>();

    public Details() {

    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getGooglePage() {
        return googlePage;
    }

    public void setGooglePage(String googlePage) {
        this.googlePage = googlePage;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public void addGoogleReview(GoogleReview googleReview) {
        this.googleReviewList.add(googleReview);
    }

    public void addYelpReview(YelpReview yelpReview) {
        this.yelpReviewList.add(yelpReview);
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public List<GoogleReview> getGoogleReviewList() {
        return googleReviewList;
    }

    public List<YelpReview> getYelpReviewList() {
        return yelpReviewList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public List<GoogleReview> getHighestRateGoogleReviewList() {
        List<GoogleReview> list = new ArrayList<>(this.googleReviewList);
        Comparator<GoogleReview> highestRateComparator = new GoogleReviewHighestRateComparator();
        Collections.sort(list, highestRateComparator);
        return list;
    }

    public List<YelpReview> getHighestRateYelpReviewList() {
        List<YelpReview> list = new ArrayList<>(this.yelpReviewList);
        Comparator<YelpReview> highestRateComparator = new YelpReviewHighestRateComparator();
        Collections.sort(list, highestRateComparator);
        return list;
    }

    public List<GoogleReview> getLowestRateGoogleReviewList() {
        List<GoogleReview> list = new ArrayList<>(this.googleReviewList);
        Comparator<GoogleReview> lowestRateComparator = new GoogleReviewLowestRateComparator();
        Collections.sort(list, lowestRateComparator);
        return list;
    }

    public List<YelpReview> getLowestRateYelpReviewList() {
        List<YelpReview> list = new ArrayList<>(this.yelpReviewList);
        Comparator<YelpReview> lowestRateComparator = new YelpReviewLowestRateComparator();
        Collections.sort(list, lowestRateComparator);
        return list;
    }

    public List<GoogleReview> getMostRecentGoogleReviewList() {
        List<GoogleReview> list = new ArrayList<>(this.googleReviewList);
        Comparator<GoogleReview> mostRecentRateComparator = new GoogleReviewMostRecentComparator();
        Collections.sort(list, mostRecentRateComparator);
        return list;
    }

    public List<YelpReview> getMostRecentRateYelpReviewList() {
        List<YelpReview> list = new ArrayList<>(this.yelpReviewList);
        Comparator<YelpReview> mostRecentRateComparator = new YelpReviewMostRecentComparator();
        Collections.sort(list, mostRecentRateComparator);
        return list;
    }

    public List<GoogleReview> getLeastRecentGoogleReviewList() {
        List<GoogleReview> list = new ArrayList<>(this.googleReviewList);
        Comparator<GoogleReview> leastRateComparator = new GoogleReviewLeastRecentComparator();
        Collections.sort(list, leastRateComparator);
        return list;
    }

    public List<YelpReview> getLeastRecentRateYelpReviewList() {
        List<YelpReview> list = new ArrayList<>(this.yelpReviewList);
        Comparator<YelpReview> leastRecentRateComparator = new YelpReviewLeastRecentComparator();
        Collections.sort(list, leastRecentRateComparator);
        return list;
    }

    private class GoogleReviewHighestRateComparator implements Comparator<GoogleReview> {

        @Override
        public int compare(GoogleReview googleReview, GoogleReview t1) {
            int value = googleReview.getRating() - t1.getRating();
            return Integer.compare(0, value);
        }
    }

    private class GoogleReviewLowestRateComparator implements Comparator<GoogleReview> {

        @Override
        public int compare(GoogleReview googleReview, GoogleReview t1) {
            int value = googleReview.getRating() - t1.getRating();
            return Integer.compare(value, 0);
        }
    }

    private class GoogleReviewMostRecentComparator implements Comparator<GoogleReview> {

        @Override
        public int compare(GoogleReview googleReview, GoogleReview t1) {
            long diff = googleReview.getTime() - t1.getTime();
            return Long.compare(0, diff);
        }
    }

    private class GoogleReviewLeastRecentComparator implements Comparator<GoogleReview> {

        @Override
        public int compare(GoogleReview googleReview, GoogleReview t1) {
            long diff = googleReview.getTime() - t1.getTime();
            return Long.compare(diff, 0);
        }
    }

    private class YelpReviewHighestRateComparator implements Comparator<YelpReview> {

        @Override
        public int compare(YelpReview yelpReview, YelpReview t1) {
            int value = yelpReview.getRating() - t1.getRating();
            return Integer.compare(0, value);
        }
    }

    private class YelpReviewLowestRateComparator implements Comparator<YelpReview> {

        @Override
        public int compare(YelpReview yelpReview, YelpReview t1) {
            int value = yelpReview.getRating() - t1.getRating();
            return Integer.compare(value, 0);
        }
    }

    private class YelpReviewMostRecentComparator implements Comparator<YelpReview> {

        @Override
        public int compare(YelpReview yelpReview, YelpReview t1) {
            long diff = yelpReview.getTime() - t1.getTime();
            return Long.compare(0, diff);
        }
    }

    private class YelpReviewLeastRecentComparator implements Comparator<YelpReview> {

        @Override
        public int compare(YelpReview yelpReview, YelpReview t1) {
            long diff = yelpReview.getTime() - t1.getTime();
            return Long.compare(diff, 0);
        }
    }
}
