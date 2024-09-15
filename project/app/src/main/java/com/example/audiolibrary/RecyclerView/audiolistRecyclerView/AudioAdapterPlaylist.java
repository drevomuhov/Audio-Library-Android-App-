package com.example.audiolibrary.RecyclerView.audiolistRecyclerView;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.example.audiolibrary.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;

public class AudioAdapterPlaylist extends Adapter<AudioViewHolderPlaylist>  {

    private FirebaseAuth authentication = FirebaseAuth.getInstance();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    // Переменные для хранения принимаемых данных в адаптер

    private ArrayList <Audio> list;


    public String IDPlaylist;



    // Конструктор адаптера
    public AudioAdapterPlaylist() {

    }


    public void setPlaylist (String IDPlaylist) {
        this.IDPlaylist = IDPlaylist;
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
    public AudioViewHolderPlaylist onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.audio_item, parent, false);
        return new AudioViewHolderPlaylist(view);
    }



    @Override
    public void onBindViewHolder(@NonNull AudioViewHolderPlaylist holder, @SuppressLint("RecyclerView") int position) {

        holder.title_audio.setText(list.get(position).title_audio);
        holder.performer_audio.setText(list.get(position).performer_audio);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getInfoAudio(view, position);


            }
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





    public void getInfoAudio (View view, int position) {

        // Основная информация об аудиозаписи
        String id_audio = list.get(position).id_audio;
        String id_user = list.get(position).id_user;
        String title_audio = list.get(position).title_audio;
        String performer_audio = list.get(position).performer_audio;
        String url_audio = list.get(position).url_audio;

        //Эмойиональная информация об аудиозаписи
        int mood_happy = list.get(position).mood_happy;
        int mood_normal = list.get(position).mood_sad;
        int mood_sad = list.get(position).mood_normal;

        //Передаем полученные данные об аудиозаписи в метод добавления в плейлист
        addInPlaylist(id_audio, id_user, title_audio, performer_audio, url_audio, mood_happy, mood_normal, mood_sad, view);


    }




    private void addInPlaylist (String id_audio, String id_user, String title_audio, String performer_audio, String url_audio, int mood_happy, int mood_normal, int mood_sad, View view) {

        HashMap<String, Object> new_audio = new HashMap<>();
        new_audio.put("id_audio", id_audio);
        new_audio.put("id_user", id_user);
        new_audio.put("title_audio", title_audio);
        new_audio.put("performer_audio", performer_audio);
        new_audio.put("url_audio", url_audio);
        new_audio.put("mood_happy", mood_happy);
        new_audio.put("mood_normal", mood_normal);
        new_audio.put("mood_sad", mood_sad);

        if (IDPlaylist != null) {

            Task<Void> query = database.getReference().child("users").child(authentication.getCurrentUser().getUid()).child("user_uploads").child("playlist").child(IDPlaylist).child("audio_playlist").child(id_audio).setValue(new_audio);
            query.addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()) {

                        // Обновляем значение количества аудиозаписей в плейлисте (увеличиваем значение на 1)
                        database.getReference("users").child(authentication.getCurrentUser().getUid()).child("user_uploads").child("playlist").child(IDPlaylist).child("info_playlist").child("colvoaudio_playlist").setValue(ServerValue.increment(1));

                        Toast.makeText(view.getContext(), "Аудиозапись добавлена в плейлист!", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
    }

}
