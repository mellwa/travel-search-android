package edu.usc.cs.travelsearch.detail;

import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class YelpReview implements Review{
    private String authorName = "Anonymous", authorUrl, profileUrl, text;
    private int rating = -1;
    private long time = -1;
    private String dateTime = "";

    public YelpReview() {

    }

    public YelpReview(JSONObject jsonObject) {
        try {
            if(jsonObject.has("user")) {
                JSONObject userObj = jsonObject.getJSONObject("user");
                if(userObj.has("name")) {
                    this.authorName = userObj.getString("name");
                }
                if(userObj.has("image_url")) {
                    this.profileUrl = userObj.getString("image_url");
                }
            }
            if(jsonObject.has("url")) {
                this.authorUrl = jsonObject.getString("url");
            }
            if(jsonObject.has("text")) {
                this.text = jsonObject.getString("text");
            }
            if(jsonObject.has("rating")) {
                this.rating = jsonObject.getInt("rating");
            }
            if(jsonObject.has("time_created")) {
                this.dateTime = jsonObject.getString("time_created");
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    Date date = sdf.parse(this.dateTime);
                    this.time = date.getTime()/1000L;
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorUrl() {
        return authorUrl;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public String getText() {
        return text;
    }

    public int getRating() {
        return rating;
    }

    public long getTime() {
        return time;
    }

    public String getDateTime() {
        return dateTime;
    }
}
