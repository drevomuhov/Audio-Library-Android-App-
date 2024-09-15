package com.example.audiolibrary.RecyclerView.userpageRecyclerView;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.audiolibrary.R;

public class UserPageAdapterViewHolder extends RecyclerView.ViewHolder {


    TextView title_key, title_value;

    public UserPageAdapterViewHolder(@NonNull View itemView) {
        super(itemView);
        init();
    }

    public void init () {
        title_key = itemView.findViewById(R.id.title_key);
        title_value = itemView.findViewById(R.id.title_value);
    }
}
