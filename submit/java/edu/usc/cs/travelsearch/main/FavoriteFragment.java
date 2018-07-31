package edu.usc.cs.travelsearch.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.usc.cs.travelsearch.R;
import edu.usc.cs.travelsearch.data.LocalDataStorageHelper;
import edu.usc.cs.travelsearch.result.SearchResult;
import edu.usc.cs.travelsearch.result.SearchResultList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 *
 * to handle interaction events.
 * Use the {@link FavoriteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FavoriteFragment extends Fragment {

    public FavoriteFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FavoriteFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FavoriteFragment newInstance() {
        FavoriteFragment fragment = new FavoriteFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("[favorite fragment]", "********* resume ***********");
        this.initializeResultData();
    }

    private List<SearchResult> fetchFavoriteList() {
        SharedPreferences sharedPreferences = getActivity()
                .getSharedPreferences(getActivity().getString(R.string.app_name), Context.MODE_PRIVATE);
        LocalDataStorageHelper helper = LocalDataStorageHelper.getInstance();
        String listString = helper.getFavoriteListString(sharedPreferences);
        Log.i("[Favorites]", listString);
        JSONArray jsonArray = null;
        List<SearchResult> searchResultList = new ArrayList<>();
        try {
            jsonArray = new JSONArray(listString);
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject elem = jsonArray.getJSONObject(i);
                SearchResult searchResult = new SearchResult(elem);
                searchResultList.add(searchResult);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return searchResultList;
    }

    private void initializeResultData() {
        boolean noResult = false;
        List<SearchResult> searchResultList = this.fetchFavoriteList();

        if(searchResultList == null) {
            searchResultList = new ArrayList<>();
        }

        if(searchResultList.size() == 0) {
            noResult = true;
        }

        TextView noResultView = getActivity().findViewById(R.id.no_favorites_message);
        if(noResult) {
            noResultView.setVisibility(View.VISIBLE);
        }
        else {
            noResultView.setVisibility(View.INVISIBLE);
        }

        RecyclerView resultListView = getActivity().findViewById(R.id.favorite_list);
        FavoriteListAdapter resultListAdapter = new FavoriteListAdapter(searchResultList, getActivity());
        resultListAdapter.setFragmentId(this.getId());
        resultListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        resultListView.setAdapter(resultListAdapter);
    }
}
