package com.example.audiolibrary.RecyclerView.playlistRecyclerView;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.audiolibrary.R;

public class PlaylistViewHolder extends RecyclerView.ViewHolder {

    ImageView image_playlist;
    TextView title_playlist;

    public PlaylistViewHolder(@NonNull View itemView) {
        super(itemView);
        init();
    }

    public void init () {
        image_playlist = itemView.findViewById(R.id.image_playlist);
        title_playlist = itemView.findViewById(R.id.title_playlist);
    }


}
