package edu.usc.cs.travelsearch.detail;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.usc.cs.travelsearch.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReviewsFragment extends Fragment {

    enum SortCategory{
        DEFAULT("Default Order"),
        HIGHEST_RATING("Highest Rating"),
        LOWEST_RATING("Lowest Rating"),
        MOST_RECENT("Most Recent"),
        LEAST_RECENT("Least Recent"),;

        String value;

        SortCategory(String s) {
            value = s;
        }

        public String getValue() {
            return value;
        }
    }

    enum ReviewCategory {
        GOOGLE_REVIEW("Google Reviews"),
        YELP_REVIEW("Yelp Reviews");

        String value;

        ReviewCategory(String s) {
            value = s;
        }

        public String getValue() {
            return value;
        }
    }

    Details details = null;

    SortCategory currentSortingCategory = SortCategory.DEFAULT;
    ReviewCategory currentReviewCategory = ReviewCategory.GOOGLE_REVIEW;

    public ReviewsFragment() {
        // Required empty public constructor
    }

    public static ReviewsFragment newInstance() {
        ReviewsFragment fragment = new ReviewsFragment();
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
        return inflater.inflate(R.layout.fragment_reviews, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.initializeSpinners();
        this.setupViews();
    }

    public void setupViews() {
        List<Review> reviews = new ArrayList<>();
        if(currentReviewCategory == ReviewCategory.YELP_REVIEW) {
            List<YelpReview> yelpReviewList = new ArrayList<>();
            switch (this.currentSortingCategory) {
                case DEFAULT:
                    yelpReviewList = this.details.getYelpReviewList();
                    break;
                case HIGHEST_RATING:
                    yelpReviewList = this.details.getHighestRateYelpReviewList();
                    break;
                case LOWEST_RATING:
                    yelpReviewList = this.details.getLowestRateYelpReviewList();
                    break;
                case MOST_RECENT:
                    yelpReviewList = this.details.getMostRecentRateYelpReviewList();
                    break;
                case LEAST_RECENT:
                    yelpReviewList = this.details.getLeastRecentRateYelpReviewList();
                    break;
            }
            for(int i = 0; i < yelpReviewList.size(); i++) {
                reviews.add(yelpReviewList.get(i));
            }
        }
        else {
            List<GoogleReview> googleReview = new ArrayList<>();
            switch (this.currentSortingCategory) {
                case DEFAULT:
                    googleReview = this.details.getGoogleReviewList();
                    break;
                case HIGHEST_RATING:
                    googleReview = this.details.getHighestRateGoogleReviewList();
                    break;
                case LOWEST_RATING:
                    googleReview = this.details.getLowestRateGoogleReviewList();
                    break;
                case MOST_RECENT:
                    googleReview = this.details.getMostRecentGoogleReviewList();
                    break;
                case LEAST_RECENT:
                    googleReview = this.details.getLeastRecentGoogleReviewList();
                    break;
            }
            for(int i = 0; i < googleReview.size(); i++) {
                reviews.add(googleReview.get(i));
            }
        }
        RecyclerView reviewListView = getActivity().findViewById(R.id.review_list);
        TextView noReviews = getActivity().findViewById(R.id.no_review_message);
        if(reviews.size() > 0) {
            reviewListView.setVisibility(View.VISIBLE);
            noReviews.setVisibility(View.GONE);
            ReviewsAdapter reviewsAdapter = new ReviewsAdapter(reviews, getActivity());
            reviewListView.setLayoutManager(new LinearLayoutManager(getActivity()));
            reviewListView.setAdapter(reviewsAdapter);
        }
        else { // no reviews
            reviewListView.setVisibility(View.GONE);
            noReviews.setVisibility(View.VISIBLE);
        }
    }

    private void initializeSpinners() {
        Spinner reviewSpinner = getView().findViewById(R.id.review_categories);
        Spinner sortSpinner = getView().findViewById(R.id.sort_categories);
        ArrayList<String> reviewCategoryList = new ArrayList<>();
        ArrayList<String> sortCategoryList = new ArrayList<>();
        for(ReviewCategory category : ReviewCategory.values()) {
            reviewCategoryList.add(category.getValue());
        }
        for(SortCategory sortCategory : SortCategory.values()) {
            sortCategoryList.add(sortCategory.getValue());
        }
        String[] reviewArray = reviewCategoryList.toArray(new String[reviewCategoryList.size()]);
        String[] sortArray = sortCategoryList.toArray(new String[sortCategoryList.size()]);
        SpinnerAdapter reviewCategoryAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.support_simple_spinner_dropdown_item, reviewArray);
        SpinnerAdapter sortCategoryAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.support_simple_spinner_dropdown_item, sortArray);
        reviewSpinner.setAdapter(reviewCategoryAdapter);
        sortSpinner.setAdapter(sortCategoryAdapter);
        reviewSpinner.setSelection(0);
        sortSpinner.setSelection(0);

        reviewSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if(position == 0) {
                    currentReviewCategory = ReviewCategory.GOOGLE_REVIEW;
                }
                else {
                    currentReviewCategory = ReviewCategory.YELP_REVIEW;
                }
                setupViews();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position) {
                    case 0:
                        currentSortingCategory = SortCategory.DEFAULT;
                        break;
                    case 1:
                        currentSortingCategory = SortCategory.HIGHEST_RATING;
                        break;
                    case 2:
                        currentSortingCategory = SortCategory.LOWEST_RATING;
                        break;
                    case 3:
                        currentSortingCategory = SortCategory.MOST_RECENT;
                        break;
                    case 4:
                        currentSortingCategory = SortCategory.LEAST_RECENT;
                        break;
                }
                setupViews();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void setDetails(Details details) {
        this.details = details;
    }
}
