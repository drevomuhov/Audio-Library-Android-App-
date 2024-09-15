package com.example.audiolibrary.RecyclerView.userfeedRecyclerView;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.audiolibrary.R;

public class FeedItemViewHolder extends RecyclerView.ViewHolder {

    TextView item_string;
    ImageView delete_item_feed;

    public FeedItemViewHolder(@NonNull View itemView) {
        super(itemView);
        init();
    }

    public void init () {
        item_string = itemView.findViewById(R.id.item_string);
        delete_item_feed = itemView.findViewById(R.id.delete_item_feed);
    }


}
