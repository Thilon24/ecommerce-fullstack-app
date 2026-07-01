package com.example.ecommerceapp.adapters;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // මෙය අලුතින් එක් කළා
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide; // Glide අවශ්‍ය වේ
import com.example.ecommerceapp.R;
import com.example.ecommerceapp.models.ReviewModel;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
    List<ReviewModel> list;

    public ReviewAdapter(List<ReviewModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.single_review_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReviewModel model = list.get(position);

        holder.name.setText(model.getUserName());
        holder.review.setText(model.getReview());
        holder.ratingBar.setRating(model.getRating());


        if (model.getReviewImg() != null && !model.getReviewImg().isEmpty()) {
            holder.reviewImg.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(model.getReviewImg())
                    .into(holder.reviewImg);
        } else {

            holder.reviewImg.setVisibility(View.GONE);
        }

        if (model.getTimestamp() != null) {
            long timeInMillis = model.getTimestamp().getSeconds() * 1000;
            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                    timeInMillis,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS);
            holder.date.setText(relativeTime);
        } else {
            holder.date.setText("Just now");
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, review, date;
        RatingBar ratingBar;
        ImageView reviewImg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.rev_user_name);
            review = itemView.findViewById(R.id.rev_text);
            ratingBar = itemView.findViewById(R.id.rev_rating);
            date = itemView.findViewById(R.id.rev_date);
            reviewImg = itemView.findViewById(R.id.rev_img);
        }
    }
}