package edu.usc.cs.travelsearch.main;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import edu.usc.cs.travelsearch.R;
import edu.usc.cs.travelsearch.adapter.AutocompleteAdapter;
import edu.usc.cs.travelsearch.data.Location;
import edu.usc.cs.travelsearch.result.ResultActivity;

import static android.content.ContentValues.TAG;


public class MainFragment extends Fragment {

    private RequestQueue queue = null;
    AutocompleteAdapter autocompleteAdapter;
    protected GeoDataClient mGeoDataClient;
    private static LatLngBounds latLngBounds = new LatLngBounds(new LatLng(-40, -168), new LatLng(70, 136));

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.init();
        this.initializeButtons();
    }

    public void init() {
        this.initializeSpinner();
        this.initializeRadioGroup();
        this.initializeTextInputs();
        this.initializeText();
        if (mGeoDataClient == null) {
            mGeoDataClient = Places.getGeoDataClient(getActivity());
        }
        if (autocompleteAdapter == null) {
            autocompleteAdapter = new AutocompleteAdapter(getActivity(), mGeoDataClient, latLngBounds, null);
        }
        AutoCompleteTextView location = getView().findViewById(R.id.type_loc);
        location.setAdapter(this.autocompleteAdapter);
    }

    public void reset() {
        this.resetSpinner();
        this.resetRadioGroup();
        this.initializeTextInputs();
        this.initializeText();
    }

    private void initializeSpinner() {
        Spinner spinner = getView().findViewById(R.id.categories);
        String[] categories = getResources().getStringArray(R.array.category_array);
        SpinnerAdapter adapter = new ArrayAdapter<String>(getActivity(),
                R.layout.support_simple_spinner_dropdown_item, categories);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
    }

    private void resetSpinner() {
        Spinner spinner = getView().findViewById(R.id.categories);
        spinner.setSelection(0);
    }

    private void initializeRadioGroup() {
        RadioGroup radioGroup = getView().findViewById(R.id.radio_group);
        radioGroup.check(R.id.current_loc_btn);
        AutoCompleteTextView editText = getView().findViewById(R.id.type_loc);
        editText.setEnabled(false);
        radioGroup.setOnCheckedChangeListener(((RadioGroup group, int checkedId) -> {
            switch (checkedId) {
                case R.id.current_loc_btn:
                    editText.setEnabled(false);
                    editText.setText("");
                    break;
                case R.id.other_loc_btn:
                    editText.setEnabled(true);
                    break;
            }
        }));
    }

    private void resetRadioGroup() {
        RadioGroup radioGroup = getView().findViewById(R.id.radio_group);
        radioGroup.check(R.id.current_loc_btn);
        EditText editText = getView().findViewById(R.id.type_loc);
        editText.setEnabled(false);
    }

    private void initializeTextInputs() {
        EditText keyword = getView().findViewById(R.id.keyword_input);
        EditText distance = getView().findViewById(R.id.distance_input);
        AutoCompleteTextView location = getView().findViewById(R.id.type_loc);
        keyword.setText("");
        distance.setText("");
        location.setText("");
    }

    private void initializeText() {
        TextView keywordError = getView().findViewById(R.id.keyword_error);
        TextView locError = getView().findViewById(R.id.loc_error);
        keywordError.setVisibility(View.GONE);
        locError.setVisibility(View.GONE);
    }

    private void initializeButtons() {
        Button clearButton = getView().findViewById(R.id.clear_btn);
        Button searchButton = getView().findViewById(R.id.search_btn);
        clearButton.setOnClickListener((View view) -> {
            this.reset();
        });

        searchButton.setOnClickListener((View view) -> {
            boolean valid = true;
            TextView keywordError = getView().findViewById(R.id.keyword_error);
            TextView locError = getView().findViewById(R.id.loc_error);
            if (!this.isKeywordInputValid()) {
                keywordError.setVisibility(View.VISIBLE);
                valid = false;
            }
            if (!this.isLocationInputValid()) {
                locError.setVisibility(View.VISIBLE);
                valid = false;
            }
            if (!valid) {
                Toast.makeText(getActivity(), "Please fix all fields with errors", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isInternetConnected()) {
                Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_LONG).show();
                return;
            }
            keywordError.setVisibility(View.GONE);
            locError.setVisibility(View.GONE);
            this.fetchSearchData();
        });
        searchButton.setEnabled(true);
    }

    private boolean isInternetConnected() {
        try {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
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

    private boolean isKeywordInputValid() {
        EditText keywordInput = getView().findViewById(R.id.keyword_input);
        String keyword = keywordInput.getText().toString();
        return !StringUtils.isBlank(keyword);
    }

    private boolean isLocationInputValid() {
        EditText locationInput = getView().findViewById(R.id.type_loc);
        if (!locationInput.isEnabled()) {
            return true;
        }
        String location = locationInput.getText().toString();
        return !StringUtils.isBlank(location);
    }

    private void fetchSearchData() {
        if (queue == null) {
            queue = Volley.newRequestQueue(getActivity());
        }
        EditText keywordInput = getView().findViewById(R.id.keyword_input);
        String keyword = keywordInput.getText().toString();
        Spinner spinner = getView().findViewById(R.id.categories);
        String category = spinner.getSelectedItem().toString();
        EditText distanceInput = getView().findViewById(R.id.distance_input);
        String distance = distanceInput.getText().toString();
        if (StringUtils.isBlank(distance)) {
            distance = "10";
        }
        RadioGroup radioGroup = getView().findViewById(R.id.radio_group);
        int checkedRadioId = radioGroup.getCheckedRadioButtonId();
        String lat = "";
        String lon = "";
        String address = "";
        boolean currentLoc = false;
        if (checkedRadioId == R.id.current_loc_btn) {
            currentLoc = true;
            try {
                LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    lat = Location.getInstance().currentLocation().getString(Location.lat);
                    lon = Location.getInstance().currentLocation().getString(Location.lon);
                }
                else {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    android.location.Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    Log.wtf("[device location]", location.getLatitude()+", "+location.getLongitude());
                    lat = Double.toString(location.getLatitude());
                    lon = Double.toString(location.getLongitude());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            EditText typedLoc = getView().findViewById(R.id.type_loc);
            address = typedLoc.getText().toString();
        }
        String url = getResources().getString(R.string.search_api);
        url = Uri.parse(url).buildUpon()
                .appendQueryParameter("keyword", keyword)
                .appendQueryParameter("category", category)
                .appendQueryParameter("distance", distance)
                .appendQueryParameter("currentLoc", Boolean.toString(currentLoc))
                .appendQueryParameter("lat", lat)
                .appendQueryParameter("lon", lon)
                .appendQueryParameter("address", address).build().toString();
        Log.wtf("[URL]", url);
        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage("Fetching results");
        dialog.show();
        StringRequest stringRequest =
                new StringRequest(Request.Method.GET, url, (String response) -> {
                    dialog.cancel();
                    Log.i("[data]", response);
                    this.goToResultActivity(response);

                }, (VolleyError error) -> {
                    Log.d(TAG, error.toString());
                    dialog.cancel();
                    this.goToResultActivity("");
                });
        queue.add(stringRequest);
    }

    private void goToResultActivity(String result) {
        Intent resultIntent = new Intent(getActivity(), ResultActivity.class);
        resultIntent.putExtra("results", result);
        getActivity().startActivity(resultIntent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }


    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
}
