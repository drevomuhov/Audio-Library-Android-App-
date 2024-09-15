package com.example.audiolibrary.RecyclerView.audiolistRecyclerView;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.audiolibrary.R;

public class AudioViewHolderPlaylist extends RecyclerView.ViewHolder {

    ImageView image_audio;
    TextView title_audio, performer_audio;

    public AudioViewHolderPlaylist(@NonNull View itemView) {
        super(itemView);
        init();
    }

    public void init () {
        image_audio = itemView.findViewById(R.id.image_audio);
        title_audio = itemView.findViewById(R.id.title_audio);
        performer_audio = itemView.findViewById(R.id.performer_audio);

    }
}
