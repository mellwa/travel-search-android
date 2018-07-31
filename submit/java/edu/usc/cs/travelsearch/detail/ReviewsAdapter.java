package edu.usc.cs.travelsearch.detail;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import edu.usc.cs.travelsearch.R;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewsViewHolder> {

    private List<Review> reviewList;
    private Context context;

    public ReviewsAdapter(List<Review> reviewList, Context context) {
        this.reviewList = reviewList;
        this.context = context;
    }

    @NonNull
    @Override
    public ReviewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.details_review_element, null);
        ReviewsViewHolder holder = new ReviewsViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewsViewHolder holder, int position) {
        Review review = reviewList.get(position);
        Picasso.get().load(review.getProfileUrl())
                .resize(150,150)
                .centerCrop()
                .into(holder.userProfile);
        holder.userName.setText(review.getAuthorName());
        holder.userRating.setRating(review.getRating());
        holder.dateTime.setText(review.getDateTime());
        holder.content.setText(review.getText());
        holder.entity.setOnClickListener(view -> {
            if(!StringUtils.isBlank(review.getAuthorUrl())) {
                Uri twitterPage = Uri.parse(review.getAuthorUrl());
                Intent intent = new Intent(Intent.ACTION_VIEW, twitterPage);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if(reviewList != null) {
            count = reviewList.size();
        }
        return count;
    }

    class ReviewsViewHolder extends RecyclerView.ViewHolder {
        ImageView userProfile;
        TextView userName, content, dateTime;
        RatingBar userRating;
        View entity;
        public ReviewsViewHolder(View itemView) {
            super(itemView);
            this.userProfile = itemView.findViewById(R.id.user_profile_icon);
            this.userName = itemView.findViewById(R.id.user_name);
            this.content = itemView.findViewById(R.id.user_content);
            this.dateTime = itemView.findViewById(R.id.user_time);
            this.userRating = itemView.findViewById(R.id.user_rating);
            this.entity = itemView;
        }
    }

}
