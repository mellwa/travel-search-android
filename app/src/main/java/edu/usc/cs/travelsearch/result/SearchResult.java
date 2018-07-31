package edu.usc.cs.travelsearch.result;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class SearchResult implements Serializable{
    private String id;
    private String category;
    private String name;
    private String address;

    public SearchResult() {
    }

    public SearchResult(JSONObject jsonObject) {
        try {
            this.id = jsonObject.getString("placeId");
            this.category = jsonObject.getString("category");
            this.name = jsonObject.getString("name");
            this.address = jsonObject.getString("address");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public SearchResult(String jsonString) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jsonString);
            this.id = jsonObject.getString("placeId");
            this.category = jsonObject.getString("category");
            this.name = jsonObject.getString("name");
            this.address = jsonObject.getString("address");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("placeId", id);
            jsonObject.put("category", category);
            jsonObject.put("name", name);
            jsonObject.put("address", address);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public String toJsonString() {
        return this.toJson().toString();
    }
}
