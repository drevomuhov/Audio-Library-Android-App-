package com.example.audiolibrary.RecyclerView.audiolistRecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.audiolibrary.R;

import java.io.IOException;
import java.util.ArrayList;

public class AudioAdapterPlaylistOpen extends RecyclerView.Adapter<AudioViewHolder> {

    // Переменные для хранения принимаемых данных в адаптер
    Context context;
    private ArrayList <Audio> list;

    View view;
    MediaPlayer mediaPlayer = new MediaPlayer();


    private AudioAdapterPlaylistOpen.OnItemClickListener listener;

    public void setOnItemClickListener(AudioAdapterPlaylistOpen.OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, ArrayList<Audio> list);
    }

    // Конструктор адаптера
    public AudioAdapterPlaylistOpen() {

    }


    // Функция установки array списка аудиозаписей в адаптер
    public void setList (ArrayList <Audio> list) {
        //Текущий список list адаптера заменяется на список полученный адаптером из активити (фрагмента) с помощью этой функции
        this.list = list;
        //Обновление отображения в RecyclerView
        notifyDataSetChanged();
    }



    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.audio_item_playlist2, parent, false);
        return new AudioViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position, list);
            }
        });


        holder.title_audio.setText(list.get(position).title_audio);
        holder.performer_audio.setText(list.get(position).performer_audio);

    }

    @Override
    public int getItemCount() {
        if (list != null) {
            return list.size();
        } else {
            return 0;
        }
    }

    public void player (View view, int position) {
        // Получить ссылку на аудиозапись
        String audioUrl = list.get(position).url_audio;
        // Создать объект MediaPlayer
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            // Установить источник данных
            mediaPlayer.setDataSource(audioUrl);
            // Подготовить и начать воспроизведение
            mediaPlayer.prepare();
            mediaPlayer.start();
            // Добавить обработчики событий
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // Освободить ресурсы
                    mp.release();
                    // Вывести сообщение о завершении воспроизведения
                    Toast.makeText(view.getContext(), "Аудиозапись закончилась", Toast.LENGTH_SHORT).show();
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    // Освободить ресурсы
                    mp.release();
                    // Вывести сообщение об ошибке
                    Toast.makeText(view.getContext(), "Произошла ошибка при воспроизведении аудиозаписи", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // Вывести сообщение о начале воспроизведения
                    Toast.makeText(view.getContext(), "Начало воспроизведения аудиозаписи", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IOException e) {
            // Обработать исключение
            e.printStackTrace();
        }
    }



}
