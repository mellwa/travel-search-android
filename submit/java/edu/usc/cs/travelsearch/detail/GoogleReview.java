package edu.usc.cs.travelsearch.detail;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class GoogleReview implements Review{
    private String authorName = "Anonymous", authorUrl, profileUrl, text;
    private int rating = 0;
    private long time = -1;
    private String dataTime = "";

    public GoogleReview() {

    }

    public GoogleReview(JSONObject jsonObject) {
        try {
            if(jsonObject.has("author_name")) {
                this.authorName = jsonObject.getString("author_name");
                if(StringUtils.isBlank(this.authorName)) {
                    this.authorName = "Anonymous";
                }
            }
            if(jsonObject.has("author_url")) {
                this.authorUrl = jsonObject.getString("author_url");
            }
            if(jsonObject.has("profile_photo_url")) {
                this.profileUrl = jsonObject.getString("profile_photo_url");
            }
            if(jsonObject.has("text")) {
                this.text = jsonObject.getString("text");
            }
            if(jsonObject.has("rating")) {
                this.rating = jsonObject.getInt("rating");
            }
            if(jsonObject.has("time")) {
                this.time = jsonObject.getInt("time");
                Date date = new Date(this.time*1000L);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("GMT-7"));
                this.dataTime = sdf.format(date);
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
        return dataTime;
    }

}
