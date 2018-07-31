package edu.usc.cs.travelsearch.detail;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.support.design.widget.TabLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.usc.cs.travelsearch.R;
import edu.usc.cs.travelsearch.data.LocalDataStorageHelper;
import edu.usc.cs.travelsearch.main.SectionsPagerAdapter;
import edu.usc.cs.travelsearch.result.SearchResult;

public class DetailsActivity extends AppCompatActivity {


    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private int[] tabIcons = {
            R.drawable.ic_info_outline,
            R.drawable.ic_photos,
            R.drawable.ic_maps,
            R.drawable.ic_review
    };

    private int[] tabText = {
            R.string.info_tab,
            R.string.photos_tab,
            R.string.map_tab,
            R.string.reviews_tab
    };
    private RequestQueue queue = null;
    ProgressDialog dialog;
    SearchResult searchResult;
    Details details = new Details();

    private SectionsPagerAdapter mSectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        this.dialog = new ProgressDialog(this);
        SearchResult searchResult = (SearchResult) getIntent().getSerializableExtra("search_result");
        this.searchResult = searchResult;
        this.setup(searchResult);
        this.fetchDetails(searchResult);
    }

    private boolean isInternetConnected() {
        try {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
                if (info != null) {
                    for (NetworkInfo anInfo : info) {
                        if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void fetchDetails(SearchResult searchResult) {
        if (queue == null) {
            queue = Volley.newRequestQueue(this);
        }
        this.details.setPlaceId(searchResult.getId());
        Log.wtf("[place id]", searchResult.getId());
        if (!isInternetConnected()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show();
            return;
        }
        String url = getResources().getString(R.string.google_place_details_api);
        url = Uri.parse(url).buildUpon()
                .appendQueryParameter("placeid", searchResult.getId())
                .build().toString();
        dialog.setMessage("Fetching results");
        dialog.show();
        StringRequest stringRequest =
                new StringRequest(Request.Method.GET, url, (String response) -> {
                    Log.i("[data]", response);
                    JSONObject responseObj = null;
                    try {
                        responseObj = new JSONObject(response);
                        if(!responseObj.has("status") || !responseObj.has("result")){
                            return;
                        }
                        String status = responseObj.getString("status");
                        JSONObject result = responseObj.getJSONObject("result");
                        if(!"OK".equals(status) || result == null){
                            return;
                        }
                        String city = "", state = "", country = "";
                        JSONArray addressComponents = result.getJSONArray("address_components");
                        for(int i = 0; i < addressComponents.length(); i++) {
                            JSONObject addressComponent = addressComponents.getJSONObject(i);
                            JSONArray types = addressComponent.getJSONArray("types");
                            for(int j = 0; j < types.length(); j++) {
                                String type = types.getString(j);
                                if("locality".equals(type)) {
                                    if(addressComponent.has("long_name")) {
                                        city = addressComponent.getString("long_name");
                                    }
                                    else if(addressComponent.has("short_name")) {
                                        city = addressComponent.getString("short_name");
                                    }
                                }
                                if("administrative_area_level_1".equals(type)) {
                                    if(addressComponent.has("short_name")) {
                                        state = addressComponent.getString("short_name");
                                    }
                                }
                                if("country".equals(type)) {
                                    if(addressComponent.has("short_name")) {
                                        country = addressComponent.getString("short_name");
                                    }
                                }
                            }
                        }
                        this.details.setName(searchResult.getName());
                        if(result.has("formatted_address")) {
                            String formattedAddress = result.getString("formatted_address");
                            searchResult.setAddress(formattedAddress);
                            this.details.setAddress(formattedAddress);
                        }
                        if(result.has("international_phone_number")) {
                            this.details.setPhone(result.getString("international_phone_number"));
                        }
                        if(result.has("price_level")) {
                            this.details.setPrice(result.getInt("price_level"));
                        }
                        if(result.has("rating")) {
                            this.details.setRating(result.getDouble("rating"));
                        }
                        if(result.has("url")) {
                            this.details.setGooglePage(result.getString("url"));
                        }
                        if(result.has("website")) {
                            this.details.setWebsite(result.getString("website"));
                        }
                        if(result.has("reviews")) {
                            JSONArray reviews = result.getJSONArray("reviews");
                            for(int i = 0; i < reviews.length(); i++) {
                                JSONObject reviewObj = reviews.getJSONObject(i);
                                GoogleReview googleReview = new GoogleReview(reviewObj);
                                this.details.addGoogleReview(googleReview);
                            }
                        }
                        if(result.has("geometry")) {
                            JSONObject geometry = result.getJSONObject("geometry");
                            if(geometry.has("location")) {
                                JSONObject location = geometry.getJSONObject("location");
                                this.details.setLat(location.getDouble("lat"));
                                this.details.setLon(location.getDouble("lng"));
                            }
                        }

                        this.fetchYelpReview(searchResult.getName(), searchResult.getAddress(), city, state, country);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        dialog.cancel();
                    }

                }, (VolleyError error) -> {
                    Log.wtf("[Details Fetching]", error.toString());
                    this.setupFragments();
                    dialog.cancel();
                });
        queue.add(stringRequest);
    }

    private void fetchYelpReview(String name, String address, String city, String state, String country) {
        String url = getResources().getString(R.string.yelp_review_api);
        url = Uri.parse(url).buildUpon()
                .appendQueryParameter("name", name)
                .appendQueryParameter("address", address)
                .appendQueryParameter("city", city)
                .appendQueryParameter("state", state)
                .appendQueryParameter("country", country)
                .build().toString();
        StringRequest stringRequest =
                new StringRequest(Request.Method.GET, url, (String response) -> {
                    Log.i("[data]", response);
                    JSONObject responseObj = null;
                    try {
                        responseObj = new JSONObject(response);
                        if(responseObj.has("reviews")) {
                            JSONArray reviews = responseObj.getJSONArray("reviews");
                            for (int i = 0; i < reviews.length(); i++) {
                                JSONObject reviewObj = reviews.getJSONObject(i);
                                YelpReview yelpReview = new YelpReview(reviewObj);
                                this.details.addYelpReview(yelpReview);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    this.setupFragments();
                    dialog.cancel();
                }, (VolleyError error) -> {
                    Log.wtf("[Details Fetching]", error.toString());
                    this.setupFragments();
                    dialog.cancel();
                });
        queue.add(stringRequest);
    }

    private void setup(SearchResult searchResult) {
        // back button
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        this.setupToolBar(searchResult);
    }

    private void setupFragments() {
        this.setupViewPage();
        this.createTabIcons();
    }

    private void setupToolBar(SearchResult searchResult) {
        Toolbar toolbar = findViewById(R.id.detail_toolbar);
        toolbar.setTitle(searchResult.getName());
        ImageView shareBtn = findViewById(R.id.share_btn);
        ImageView favoriteBtn = findViewById(R.id.favorite_btn);
        shareBtn.setPadding(30, 0,30,0);
        favoriteBtn.setPadding(30, 0,30,0);

        shareBtn.setImageResource(R.drawable.ic_share);

        shareBtn.setOnClickListener(view -> {
            String name = this.searchResult.getName();
            String address = this.details.getAddress();
            String website = this.details.getWebsite();
            String twitterUrl = getResources().getString(R.string.twitter_url);
            String text = "Check out " + name;
            if(!StringUtils.isBlank(address)) {
                text += " located at " + address +".";
            }
            if(!StringUtils.isBlank(website)) {
                text += " Website:";
            }
            Log.i("[twitter url]", text + twitterUrl);
            twitterUrl = Uri.parse(twitterUrl).buildUpon()
                    .appendQueryParameter("text", text)
                    .appendQueryParameter("url", website)
                    .toString();
            Uri twitterPage = Uri.parse(twitterUrl);
            Intent intent = new Intent(Intent.ACTION_VIEW, twitterPage);
            startActivity(intent);
        });

        // Handle favorite click
        LocalDataStorageHelper localDataStorageHelper = LocalDataStorageHelper.getInstance();
        SharedPreferences sharedPreferences =
                getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);

        if( localDataStorageHelper.isInFavorite(sharedPreferences, searchResult.getId()) ){
            favoriteBtn.setImageResource(R.drawable.ic_heart_fill_white);
            favoriteBtn.setTag(R.drawable.ic_heart_fill_white);
        }
        else {
            favoriteBtn.setImageResource(R.drawable.ic_heart_outline_white);
            favoriteBtn.setTag(R.drawable.ic_heart_outline_white);
        }

        favoriteBtn.setOnClickListener(view -> {
            ImageView imageView = (ImageView) view;
            Integer tag = (Integer)R.drawable.ic_heart_outline_white;
            if(imageView.getTag().equals(tag) ){
                imageView.setImageResource(R.drawable.ic_heart_fill_white);
                imageView.setTag(R.drawable.ic_heart_fill_white);
                localDataStorageHelper.storeResult(sharedPreferences, searchResult);
                Toast.makeText(this, searchResult.getName()+" was added to favorites", Toast.LENGTH_SHORT).show();
            }
            else {
                imageView.setImageResource(R.drawable.ic_heart_outline_white);
                imageView.setTag(R.drawable.ic_heart_outline_white);
                localDataStorageHelper.removeResult(sharedPreferences, searchResult.getId());
                Toast.makeText(this, searchResult.getName()+" was removed from favorites", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createTabIcons() {
        TabLayout tabLayout = findViewById(R.id.detail_tabs);
        tabLayout.setupWithViewPager(mViewPager);
        for(int i = 0; i < this.tabIcons.length; i++) {
            View tab = LayoutInflater.from(this).inflate(R.layout.detail_tabs_view, null);
            TextView tabText = tab.findViewById(R.id.tabTextView);
            ImageView tabImage = tab.findViewById(R.id.tabImageView);
            String text = getResources().getString(this.tabText[i]);
            tabText.setText(text);
            tabImage.setImageResource(this.tabIcons[i]);
            tab.setMinimumWidth(255);
            tabLayout.getTabAt(i).setCustomView(tab);
        }
    }

    private void setupViewPage() {
        String infoTab = getResources().getString(R.string.info_tab);
        String photosTab = getResources().getString(R.string.photos_tab);
        String mapTab = getResources().getString(R.string.map_tab);
        String reviewTab = getResources().getString(R.string.reviews_tab);
        InfoFragment infoFragment = InfoFragment.newInstance();
        PhotosFragment photosFragment = PhotosFragment.newInstance();
        MapFragment mapFragment = MapFragment.newInstance();
        ReviewsFragment reviewsFragment = ReviewsFragment.newInstance();

        infoFragment.setDetails(this.details);
        photosFragment.setPlaceId(this.details.getPlaceId());
        mapFragment.setDetails(this.details);
        reviewsFragment.setDetails(this.details);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mSectionsPagerAdapter.addFragment(infoFragment, infoTab);
        mSectionsPagerAdapter.addFragment(photosFragment, photosTab);
        mSectionsPagerAdapter.addFragment(mapFragment, mapTab);
        mSectionsPagerAdapter.addFragment(reviewsFragment, reviewTab);
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

}
