package edu.usc.cs.travelsearch.detail;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import edu.usc.cs.travelsearch.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PhotosFragment extends Fragment {

    String placeId = "";

    GeoDataClient geoDataClient;

    static List<Bitmap> photoList = new ArrayList<>();

    static TableLayout photoTable;

    static TextView noPhotoMessage;

    public PhotosFragment() {
        // Required empty public constructor
    }

    public static PhotosFragment newInstance() {
        PhotosFragment fragment = new PhotosFragment();
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
        return inflater.inflate(R.layout.fragment_photos, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.w("[photo]", "on activity created!");
        this.geoDataClient = Places.getGeoDataClient(getActivity());
        photoTable = getActivity().findViewById(R.id.photo_table);
        noPhotoMessage = getActivity().findViewById(R.id.no_photos);
        this.fetchPhotos();
    }

    @Override
    public void onResume() {
        super.onResume();
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

    private void fetchPhotos() {
        if (!isInternetConnected()) {
            try {
                Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_LONG).show();
                if(this.photoList != null && this.photoList.size() > 0) {
                    for(int i = 0; i < this.photoList.size(); i ++) {
                        View view = createSinglePhotoView(this.photoList.get(i));
                        this.photoTable.addView(view);
                    }
                    photoTable.setVisibility(View.VISIBLE);
                    noPhotoMessage.setVisibility(View.GONE);
                }else {
                    photoTable.setVisibility(View.GONE);
                    noPhotoMessage.setVisibility(View.VISIBLE);
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        final Task<PlacePhotoMetadataResponse> photoMetadataResponse = geoDataClient.getPlacePhotos(this.placeId);
        photoMetadataResponse.addOnCompleteListener(task -> {
            try {
                // Get the list of photos.
                PlacePhotoMetadataResponse photos = task.getResult();
                // Get the PlacePhotoMetadataBuffer (metadata for all of the photos).
                PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                if (photoMetadataBuffer.getCount() == 0) {
                    photoTable.setVisibility(View.GONE);
                    noPhotoMessage.setVisibility(View.VISIBLE);
                } else {
                    photoTable.setVisibility(View.VISIBLE);
                    noPhotoMessage.setVisibility(View.GONE);
                }
                for (int i = 0; i < photoMetadataBuffer.getCount(); i++) {
                    PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(i);
                    // Get a full-size bitmap for the photo.
                    Task<PlacePhotoResponse> photoResponse = geoDataClient.getPhoto(photoMetadata);
                    photoResponse.addOnCompleteListener(task1 -> {
                        PlacePhotoResponse photo = task1.getResult();
                        Bitmap bitmap = photo.getBitmap();
                        photoList.add(bitmap);
                        View view = createSinglePhotoView(bitmap);
                        if (view != null) {
                            photoTable.addView(view);
                        }
                    });
                }
                photoMetadataBuffer.release();
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Internet connection error", Toast.LENGTH_LONG).show();
                photoTable.setVisibility(View.GONE);
                noPhotoMessage.setVisibility(View.VISIBLE);
            }
        });
    }

    private View createSinglePhotoView(Bitmap bm) {
        View view = null;
        try {
            view = LayoutInflater.from(getActivity()).inflate(R.layout.photo_row, null);
            ImageView photoView = view.findViewById(R.id.photo_view);
            photoView.setImageBitmap(bm);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }
}
