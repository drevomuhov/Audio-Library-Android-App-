package com.example.audiolibrary.RecyclerView.userlistRecyclerView;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.audiolibrary.R;

public class UserViewHolder extends RecyclerView.ViewHolder {


    CardView userBlock;


    TextView userBlock_name_user, userBlock_colvo_audio_info;
    ImageView profile_image, userBlock_add_friend_button;


    public UserViewHolder(@NonNull View itemView) {
        super(itemView);

        userBlock = itemView.findViewById(R.id.userBlock);

        profile_image = itemView.findViewById(R.id.profile_image);

        userBlock_name_user = itemView.findViewById(R.id.userBlock_name_user);
        userBlock_colvo_audio_info = itemView.findViewById(R.id.userBlock_colvo_audio_info);

        userBlock_add_friend_button = itemView.findViewById(R.id.userBlock_add_friend_button);
    }

}
