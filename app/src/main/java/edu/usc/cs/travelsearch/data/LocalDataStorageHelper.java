package edu.usc.cs.travelsearch.data;

import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.usc.cs.travelsearch.result.SearchResult;

public class LocalDataStorageHelper {

    private static LocalDataStorageHelper localDataStorageHelper = new LocalDataStorageHelper();

    private final static String FAVORITE_LIST = "favorite_list";

    private LocalDataStorageHelper() {
    }

    //*** check sharedPreference if is null
    synchronized public void storeResult(SharedPreferences sharedPreferences, SearchResult searchResult) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        JSONArray jsonArray = new JSONArray();
        String listString = sharedPreferences.getString(FAVORITE_LIST, jsonArray.toString());
        try {
            jsonArray = new JSONArray(listString);
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject resultObj = jsonArray.getJSONObject(i);
                SearchResult element = new SearchResult(resultObj);
                if(searchResult.getId().equals(element.getId())) {
                    return;
                }
            }
            jsonArray.put(searchResult.toJson());
            editor.putString(FAVORITE_LIST, jsonArray.toString());
            editor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    synchronized public void removeResult(SharedPreferences sharedPreferences, String placeId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        JSONArray jsonArray = new JSONArray();
        String listString = sharedPreferences.getString(FAVORITE_LIST, jsonArray.toString());
        try {
            jsonArray = new JSONArray(listString);
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject resultObj = jsonArray.getJSONObject(i);
                SearchResult searchResult = new SearchResult(resultObj);
                if(searchResult.getId().equals(placeId)){
                    jsonArray.remove(i);
                    break;
                }
            }
            editor.putString(FAVORITE_LIST, jsonArray.toString());
            editor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean isInFavorite(SharedPreferences sharedPreferences, String placeId) {
        JSONArray jsonArray = new JSONArray();
        String listString = sharedPreferences.getString(FAVORITE_LIST, jsonArray.toString());
        try {
            jsonArray = new JSONArray(listString);
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject resultObj = jsonArray.getJSONObject(i);
                SearchResult element = new SearchResult(resultObj);
                if(element.getId().equals(placeId)){
                    return true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isInFavorite(SharedPreferences sharedPreferences, SearchResult searchResult) {
        String placeId = searchResult.getId();
        return isInFavorite(sharedPreferences, placeId);
    }

    public void removeAllData(SharedPreferences sharedPreferences) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        JSONArray jsonArray = new JSONArray();
        editor.putString(FAVORITE_LIST, jsonArray.toString());
        editor.commit();
    }

    public String getFavoriteListString(SharedPreferences sharedPreferences) {
        JSONArray jsonArray = new JSONArray();
        String listString = sharedPreferences.getString(FAVORITE_LIST, jsonArray.toString());
        return listString;
    }

    public static LocalDataStorageHelper getInstance() {
        return localDataStorageHelper;
    }
}
