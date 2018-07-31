package edu.usc.cs.travelsearch.result;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.zip.Inflater;

import edu.usc.cs.travelsearch.data.LocalDataStorageHelper;
import edu.usc.cs.travelsearch.data.Location;
import edu.usc.cs.travelsearch.detail.DetailsActivity;
import edu.usc.cs.travelsearch.R;

public class ResultActivity extends AppCompatActivity {

    private RequestQueue queue = null;
    Stack<String> previousPageData = new Stack<>();
    String currentData = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        String resultString = getIntent().getStringExtra("results");
        this.currentData = resultString;
        this.initializeResultData(this.getUsefulDataFromSearch(this.currentData));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("[onResume]", "result activity");
        this.initializeResultData(this.getUsefulData(this.currentData));
    }

    public void onClick(View view) {
        Intent indent = new Intent(this, DetailsActivity.class);
        String message = "message";
        indent.putExtra(message, "hello");
        startActivity(indent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("[onDestroy]", "result activity");
    }

    private SearchResultList getUsefulDataFromSearch(String dataStr) {
        JSONObject result = null;
        try {
            result = new JSONObject(dataStr);
            JSONObject currentLocation = result.getJSONObject("current_location");
            String status = result.getString("status");
            if(currentLocation == null || !"OK".equals(status)) {
                return null;
            }
            String lat = currentLocation.getString("lat");
            String lon = currentLocation.getString("lon");
            if(StringUtils.isBlank(lat) || StringUtils.isBlank(lon)) {
                return null;
            }
            Location.getInstance().setCurrentLocation(lat, lon);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getUsefulData(dataStr);
    }

    private SearchResultList getUsefulData(String dataStr) {
        SearchResultList searchResultList = new SearchResultList();
        try {
            JSONObject result = new JSONObject(dataStr);
            String status = result.getString("status");
            if(!"OK".equals(status)) {
                return searchResultList;
            }
            String nextPageToken = result.has("next_page_token")
                    ? result.getString("next_page_token") : "";
            JSONArray results = result.getJSONArray("results");
            if(results == null || results.length() < 1) {
                return searchResultList;
            }
            for(int i = 0; i < results.length(); i++) {
                JSONObject placeResult = (JSONObject) results.get(i);
                SearchResult searchResult = new SearchResult();
                searchResult.setCategory(placeResult.getString("icon"));
                searchResult.setName(placeResult.getString("name"));
                searchResult.setAddress(placeResult.getString("vicinity"));
                searchResult.setId(placeResult.getString("place_id"));
                searchResultList.addResult(searchResult);
            }
            searchResultList.setNextPageToken(nextPageToken);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.currentData = dataStr;
        return searchResultList;
    }

    private void initializeResultData(SearchResultList searchResultList) {
        boolean noResult = false;
        if(searchResultList == null) {
            searchResultList = new SearchResultList();
        }
        if(searchResultList.getResults().size() == 0) {
            noResult = true;
        }
        Button prevBtn = findViewById(R.id.previous_btn);
        Button nextBtn = findViewById(R.id.next_btn);

        TextView noResultView = findViewById(R.id.no_results_message);
        if(noResult) {
            noResultView.setVisibility(View.VISIBLE);
            prevBtn.setVisibility(View.INVISIBLE);
            nextBtn.setVisibility(View.INVISIBLE);
        }
        else {
            noResultView.setVisibility(View.INVISIBLE);
        }

        RecyclerView resultListView = findViewById(R.id.result_list);
        List<SearchResult> results = searchResultList.getResults();
        ResultListAdapter resultListAdapter = new ResultListAdapter(results, this);
        resultListView.setLayoutManager(new LinearLayoutManager(this));
        resultListView.setAdapter(resultListAdapter);

        if(previousPageData.empty()) {
            prevBtn.setEnabled(false);
        }
        else {
            prevBtn.setEnabled(true);
        }
        if(StringUtils.isBlank(searchResultList.getNextPageToken())){
            nextBtn.setEnabled(false);
        }else {
            nextBtn.setEnabled(true);
        }
        String nextPageToken = searchResultList.getNextPageToken();
        prevBtn.setOnClickListener(view -> {
            this.fetchPreviousPage();
        });
        nextBtn.setOnClickListener(view -> {
            this.fetchNextPage(nextPageToken);
        });
    }

    private void fetchPreviousPage() {
        String dataStr = this.previousPageData.pop();
        this.initializeResultData(this.getUsefulData(dataStr));
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

    private void fetchNextPage(String pageToken) {
        if (queue == null) {
            queue = Volley.newRequestQueue(this);
        }
        if (!isInternetConnected()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show();
            return;
        }
        String url = getResources().getString(R.string.next_page_api);
        url = Uri.parse(url).buildUpon()
                .appendQueryParameter("pagetoken", pageToken)
                .build().toString();
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Fetching results");
        dialog.show();
        Log.i("[URL", url);
        StringRequest stringRequest =
                new StringRequest(Request.Method.GET, url, (String response) -> {
                    dialog.cancel();
                    Log.i("[data]", response);
                    this.previousPageData.push(this.currentData);
                    this.initializeResultData(this.getUsefulData(response));
                }, (VolleyError error) -> {
                    Log.wtf("[NEXT PAGE]", error.toString());
                    dialog.cancel();
                    Toast.makeText(this, "next page fetching failed", Toast.LENGTH_SHORT).show();
                });
        queue.add(stringRequest);
    }
}
