package edu.usc.cs.travelsearch.detail;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import edu.usc.cs.travelsearch.R;

/**
 */
public class InfoFragment extends Fragment {

    Details details = null;
    GridLayout infoGrid;
    TextView addressTag, address, phoneTag, phone, priceTag, price, ratingTag,
    googlePageTag, googlePage, websiteTag, website, noInfoMssage;
    RatingBar rating;

    public InfoFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static InfoFragment newInstance() {
        InfoFragment fragment = new InfoFragment();
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
        return inflater.inflate(R.layout.fragment_info, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.infoGrid = getActivity().findViewById(R.id.info_grid);
        this.addressTag = getActivity().findViewById(R.id.address_tag);
        this.address = getActivity().findViewById(R.id.address);
        this.phoneTag = getActivity().findViewById(R.id.phone_tag);
        this.phone = getActivity().findViewById(R.id.phone);
        this.priceTag = getActivity().findViewById(R.id.price_level_tag);
        this.price = getActivity().findViewById(R.id.price_level);
        this.ratingTag = getActivity().findViewById(R.id.rating_tag);
        this.rating = getActivity().findViewById(R.id.rating_bar);
        this.googlePageTag = getActivity().findViewById(R.id.google_page_tag);
        this.googlePage = getActivity().findViewById(R.id.google_page);
        this.websiteTag = getActivity().findViewById(R.id.website_tag);
        this.website = getActivity().findViewById(R.id.website);
        this.noInfoMssage = getActivity().findViewById(R.id.no_info);
        boolean containsData = this.setup();
        if(!containsData) {
            this.noInfoMssage.setVisibility(View.VISIBLE);
            this.infoGrid.setVisibility(View.GONE);
        }
        else {
            this.noInfoMssage.setVisibility(View.GONE);
            this.infoGrid.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void setDetails(Details details) {
        this.details = details;
    }

    private boolean setup() {
        boolean containsData = false;
        boolean addressData = this.setupAddress();
        boolean phoneData = this.setupPhone();
        boolean priceData = this.setupPrice();
        boolean ratingData = this.setupRating();
        boolean googleData = this.setupGooglePage();
        boolean websiteData = this.setupWebsite();
        containsData = containsData || addressData;
        containsData = containsData || phoneData;
        containsData = containsData || priceData;
        containsData = containsData || ratingData;
        containsData = containsData || googleData;
        containsData = containsData || websiteData;
        return containsData;
    }

    private boolean setupAddress() {
        String address = this.details.getAddress();
        if(StringUtils.isBlank(address)) {
            this.addressTag.setVisibility(View.GONE);
            this.address.setVisibility(View.GONE);
            return false;
        }
        this.address.setText(address);
        return true;
    }

    private boolean setupPhone() {
        String phoneNumber = this.details.getPhone();
        if(StringUtils.isBlank(phoneNumber)) {
            this.phoneTag.setVisibility(View.GONE);
            this.phone.setVisibility(View.GONE);
            return false;
        }
        this.phone.setText(phoneNumber);
        return true;
    }

    private boolean setupPrice() {
        int price = this.details.getPrice();
        if(price < 0) {
            this.priceTag.setVisibility(View.GONE);
            this.price.setVisibility(View.GONE);
            return false;
        }
        String priceStr = "";
        for(int i = 0; i < price; i++) {
            priceStr += "$";
        }
        this.price.setText(priceStr);
        return true;
    }

    private boolean setupRating() {
        double rating = this.details.getRating();
        if(rating < 0) {
            this.ratingTag.setVisibility(View.GONE);
            this.rating.setVisibility(View.GONE);
            return false;
        }
        this.rating.setRating((float) rating);
        return true;
    }

    private boolean setupGooglePage() {
        String googlePage = this.details.getGooglePage();
        if(StringUtils.isBlank(googlePage)) {
            this.googlePageTag.setVisibility(View.GONE);
            this.googlePage.setVisibility(View.GONE);
            return false;
        }
        this.googlePage.setText(googlePage);
        return true;
    }

    private boolean setupWebsite() {
        String websiteUrl = this.details.getWebsite();
        if(StringUtils.isBlank(websiteUrl)) {
            this.websiteTag.setVisibility(View.GONE);
            this.website.setVisibility(View.GONE);
            return false;
        }
        this.website.setText(websiteUrl);
        return true;
    }
}
