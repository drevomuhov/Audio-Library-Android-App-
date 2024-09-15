package com.example.audiolibrary.RecyclerView.playlistRecyclerView;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.audiolibrary.Navigation.screens.PlaylistActivity;
import com.example.audiolibrary.R;

import java.util.ArrayList;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistViewHolder> {

    // Переменные для хранения принимаемых данных в адаптер

    private ArrayList <Playlist> list;


    // Конструктор адаптера
    public PlaylistAdapter() {

    }


    // Функция установки array списка аудиозаписей в адаптер
    public void setList (ArrayList <Playlist> list) {
        //Текущий список list адаптера заменяется на список полученный адаптером из активити (фрагмента) с помощью этой функции
        this.list = list;
        //Обновление отображения в RecyclerView
        notifyDataSetChanged();
    }



    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item, parent, false);
        return new PlaylistViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Context context = holder.itemView.getContext();

        holder.title_playlist.setText(list.get(position).title_playlist);


        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, PlaylistActivity.class);
            intent.putExtra("playlist_id", list.get(position).uid_playlist);
            intent.putExtra("playlist_title", list.get(position).title_playlist);
            intent.putExtra("playlist_colvoaudio", list.get(position).colvoaudio_playlist);
            context.startActivity(intent);

        });


    }

    @Override
    public int getItemCount() {
        if (list != null) {
            return list.size();
        } else {
            return 0;
        }
    }

}
