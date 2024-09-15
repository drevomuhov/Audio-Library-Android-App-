package com.example.audiolibrary.RecyclerView.friendlistRecyclerView;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.audiolibrary.R;

public class FriendViewHolder extends RecyclerView.ViewHolder {

    ImageView profile_image;
    TextView name_friend_rv, colvoaudio_friend_rv;
    Button deletefriend_friend_rv;


    public FriendViewHolder(@NonNull View itemView) {
        super(itemView);

        profile_image = itemView.findViewById(R.id.profile_image);
        name_friend_rv = itemView.findViewById(R.id.friendBlock_name_friend);
        colvoaudio_friend_rv = itemView.findViewById(R.id.friendBlock_colvo_audio_info);
        deletefriend_friend_rv = itemView.findViewById(R.id.friendBlock_delete_friend_button);

    }

}
