package edu.usc.cs.travelsearch.detail;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.RequestResult;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.model.Step;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.usc.cs.travelsearch.R;
import edu.usc.cs.travelsearch.adapter.AutocompleteAdapter;
import edu.usc.cs.travelsearch.data.Location;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    Details details = null;
    SupportMapFragment mapFragment;
    GoogleMap mMap;
    AutocompleteAdapter autocompleteAdapter;
    protected GeoDataClient mGeoDataClient;
    private static LatLngBounds latLngBounds = new LatLngBounds(new LatLng(-40, -168), new LatLng(70, 136));
    LatLng toLatLng;
    LatLng fromLatLng;
    String placeName = "destination";
    String currentFrom = "from";
    View view;

    String apiKey;
    String mode;
    private RequestQueue queue = null;

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w("[map]", "create!");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(view == null) {
            view = inflater.inflate(R.layout.fragment_map, container, false);
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(StringUtils.isBlank(this.apiKey)) {
            this.apiKey = getActivity().getResources().getString(R.string.google_api_key);
        }
        initSpinner();
        initMap();
        if(mGeoDataClient == null) {
            mGeoDataClient = Places.getGeoDataClient(getActivity());
        }
        if(autocompleteAdapter == null) {
            autocompleteAdapter = new AutocompleteAdapter(getActivity(), mGeoDataClient, latLngBounds, null);
        }
        AutoCompleteTextView location = getView().findViewById(R.id.mapFrom);
        location.setAdapter(this.autocompleteAdapter);
        location.setOnItemClickListener((parent, view, position, id) -> {
            String from_loc = ((TextView) getActivity().findViewById(R.id.mapFrom)).getText().toString();
            this.addressChange(from_loc);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void updateDirection() {
        // clear map
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(toLatLng).title(placeName));

        mode = getTravelMode();
        if(this.fromLatLng == null) {
            Toast.makeText(getActivity(), "Failed to get FROM postion", Toast.LENGTH_SHORT).show();
            return;
        }
        if(this.toLatLng == null) {
            Toast.makeText(getActivity(), "Failed to get TO postion", Toast.LENGTH_SHORT).show();
            return;
        }
        GoogleDirection.withServerKey(apiKey)
                .from(fromLatLng)
                .to(toLatLng)
                .transportMode(mode)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        String status = direction.getStatus();
                        if (status.equals(RequestResult.OK)) {
                            Route route = direction.getRouteList().get(0);
                            Leg leg = route.getLegList().get(0);
                            ArrayList<LatLng> posList = leg.getDirectionPoint();
                            PolylineOptions polylineOptions = DirectionConverter.createPolyline(getContext(),
                                    posList, 5, Color.BLUE);
                            mMap.addMarker(new MarkerOptions().position(fromLatLng)
                                    .title(currentFrom));
                            if (mode != "transit") {
                                mMap.addPolyline(polylineOptions);
                            } else {
                                List<Step> stepList = direction.getRouteList().get(0).getLegList().get(0).getStepList();
                                ArrayList<PolylineOptions> polylineOptionList = DirectionConverter
                                        .createTransitPolyline(getContext(), stepList, 5, Color.BLUE, 5, Color.BLUE);
                                for (PolylineOptions polylineOption : polylineOptionList) {
                                    mMap.addPolyline(polylineOption);
                                }
                            }

                            // zoom in
                            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                            for (LatLng point: posList) {
                                boundsBuilder.include(point);
                            }
                            LatLngBounds latLngBounds = boundsBuilder.build();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 0));
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(mMap.getCameraPosition().zoom - 0.5f));
                        } else {
                            Toast.makeText(getActivity(), "Failed to get direction", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        Toast.makeText(getActivity(), "Failed to get direction", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void addressChange(String address) {
        if (queue == null) {
            queue = Volley.newRequestQueue(getActivity());
        }
        if(!StringUtils.isBlank(address)) {
            String[] parts = address.split(",");
            if(parts != null && parts.length > 0) {
                this.currentFrom = parts[0];
            }
        }
        String url = getResources().getString(R.string.google_geo_api);
        url = Uri.parse(url).buildUpon()
                .appendQueryParameter("address", address)
                .appendQueryParameter("key", apiKey)
                .build().toString();
        StringRequest stringRequest =
                new StringRequest(Request.Method.GET, url, (String response) -> {
                    Log.i("[data]", response);
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response);
                        JSONArray jsonArray = jsonObject.getJSONArray("results");
                        double lat = -181, lng = -181;
                        if(jsonArray != null && jsonArray.length() > 0) {
                            JSONObject result = jsonArray.getJSONObject(0);
                            JSONObject geometry = null, location = null;
                            if(result.has("geometry")) {
                                geometry = result.getJSONObject("geometry");
                            }
                            if(geometry != null && geometry.has("location")) {
                                location = geometry.getJSONObject("location");
                            }
                            if(location != null && location.has("lat") && location.has("lng")) {
                                lat = location.getDouble("lat");
                                lng = location.getDouble("lng");
                            }
                        }
                        if( lat > -181 && lng > -181) {
                            this.fromLatLng = new LatLng(lat, lng);
                        }
                        else {
                            this.fromLatLng = null;
                        }
                        updateDirection();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, (VolleyError error) -> {
                    Log.wtf("[Details Fetching]", error.toString());
                });
        queue.add(stringRequest);
    }

    private String getTravelMode() {
        Spinner travelSpinner = getView().findViewById(R.id.travelSpinner);
        return travelSpinner.getSelectedItem().toString().toLowerCase();
    }

    private void initSpinner() {
        Log.w("[map]", "init!spinner");
        // set travel mode adapter
        Spinner travelSpinner = getView().findViewById(R.id.travelSpinner);
        String[] travelModes = getResources().getStringArray(R.array.travel_array);
        SpinnerAdapter adapter = new ArrayAdapter<String>(getActivity(),
                R.layout.support_simple_spinner_dropdown_item, travelModes);
        travelSpinner.setAdapter(adapter);
        travelSpinner.setSelection(0);
        travelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (fromLatLng != null) {
                    updateDirection();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });
    }

    private void initMap() {
        // map
        mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment == null) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            mapFragment = SupportMapFragment.newInstance();
            ft.replace(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
    }

    private void setupGoogleMap() {
        this.placeName = this.details.getName();
        if(this.toLatLng == null && this.details.getLat() > -181 && this.details.getLon() > -181) {
            this.toLatLng = new LatLng(this.details.getLat(), this.details.getLon());
        }
        Marker destination = this.mMap.addMarker(new MarkerOptions().position(toLatLng)
                .title(placeName));
        destination.showInfoWindow();
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(toLatLng).zoom(13).build();
        this.mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        mMap.setOnMapClickListener(v -> {
            Log.w("[map]", "update!");
            this.updateDirection();
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        this.setupGoogleMap();
    }

    public void setDetails(Details details) {
        this.details = details;
    }
}
