package edu.usc.cs.travelsearch.result;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import edu.usc.cs.travelsearch.R;
import edu.usc.cs.travelsearch.data.LocalDataStorageHelper;
import edu.usc.cs.travelsearch.detail.DetailsActivity;

public class ResultListAdapter extends RecyclerView.Adapter<ResultListAdapter.ResultListViewHolder> {

    private List<SearchResult> results;
    private Context context;

    public ResultListAdapter(List<SearchResult> results, Context context) {
        this.results = results;
        this.context = context;
    }

    @NonNull
    @Override
    public ResultListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.result_list_element, null);
        ResultListViewHolder holder = new ResultListViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ResultListViewHolder holder, int position) {
        FragmentActivity activity = (FragmentActivity) this.context;
        SearchResult searchResult = results.get(position);

        Picasso.get().load(searchResult.getCategory())
                .resize(120, 120)
                .centerCrop().into(holder.categoryView);
        holder.categoryView.setMinimumWidth(120);
        holder.nameView.setText(searchResult.getName());
        holder.addressView.setText(searchResult.getAddress());

        //detect if added to favorite
        Drawable redHeart = this.context.getDrawable(R.drawable.ic_heart_fill_red);
        Drawable blackHeart = this.context.getDrawable(R.drawable.ic_heart_outline_black);

        SharedPreferences sharedPreferences =
                activity.getSharedPreferences(this.context.getString(R.string.app_name), Context.MODE_PRIVATE);
        LocalDataStorageHelper localDataStorageHelper = LocalDataStorageHelper.getInstance();
        if(localDataStorageHelper.isInFavorite(sharedPreferences, searchResult)) {
            holder.favoriteBtnView.setImageDrawable(redHeart);
            holder.favoriteBtnView.setTag(R.drawable.ic_heart_fill_red);
        }
        else {
            holder.favoriteBtnView.setImageDrawable(blackHeart);
            holder.favoriteBtnView.setTag(R.drawable.ic_heart_outline_black);
        }

        holder.favoriteBtnView.setOnClickListener((view) -> {
            ImageView imageView = (ImageView) view;
            Integer tag = (Integer)R.drawable.ic_heart_outline_black;
            if(imageView.getTag().equals(tag) ){
                imageView.setImageDrawable(redHeart);
                imageView.setTag(R.drawable.ic_heart_fill_red);
                localDataStorageHelper.storeResult(sharedPreferences, searchResult);
                Toast.makeText(activity, searchResult.getName()+" was added to favorites", Toast.LENGTH_SHORT).show();
            }
            else {
                imageView.setImageDrawable(blackHeart);
                imageView.setTag(R.drawable.ic_heart_outline_black);
                localDataStorageHelper.removeResult(sharedPreferences, searchResult.getId());
                Toast.makeText(activity, searchResult.getName()+" was removed from favorites", Toast.LENGTH_SHORT).show();
            }
        });

        holder.itemView.setOnClickListener(view -> {
            Log.wtf("[view clicked]", ""+view.getId());
            this.goToDetails(searchResult);
        });

    }

    private void goToDetails(SearchResult searchResult) {
        Intent intent = new Intent(this.context, DetailsActivity.class);
        intent.putExtra("search_result", searchResult);
        this.context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if(results != null) {
            count = results.size();
        }
        return count;
    }

    class ResultListViewHolder extends RecyclerView.ViewHolder {

        ImageView categoryView, favoriteBtnView;
        TextView nameView, addressView;

        public ResultListViewHolder(View itemView) {
            super(itemView);
            this.categoryView = itemView.findViewById(R.id.result_icon);
            this.favoriteBtnView = itemView.findViewById(R.id.like_btn);
            this.nameView = itemView.findViewById(R.id.result_name);
            this.addressView = itemView.findViewById(R.id.result_address);
        }
    }
}
