package com.example.audiolibrary.RecyclerView.audiolistRecyclerView;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.audiolibrary.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class AudioAdapterPredict extends RecyclerView.Adapter<AudioViewHolder> {

    private FirebaseAuth authentication = FirebaseAuth.getInstance();
    private final String currentUser = authentication.getCurrentUser().getUid().toString();

    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    // Переменные для хранения принимаемых данных в адаптер

    private ArrayList<Audio> list;

    View view;

    String USER_MOOD = null;


    private final String type_player = "predict";
    private String user_mood = "";

    private AudioAdapterPredict.OnItemClickListener listener;

    public void setOnItemClickListener(AudioAdapterPredict.OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, String type_player, String user_mood, ArrayList<Audio> list);
    }




    // Конструктор адаптера
    public AudioAdapterPredict() {

    }


    // Метод вызывается для получения настроения пользователя
    public void setUserMood (String USER_MOOD) {
        this.USER_MOOD = USER_MOOD;
        this.user_mood = USER_MOOD;
    }


    // Метод вызывается для получения списка аудиозаписей из базы данных и преедачи его в generateRandomList
    public void setList(ArrayList<Audio> list) {
        this.list = list;
        notifyDataSetChanged();
    }


    // Метод вызывается для сохранения сгенерированного плейлиста в плейлисты пользователя
    public void savePlaylist (String title_playlist) {

        
        if (list.size() > 0) {

            // Создаем уникальный ID для плейлиста
            String id_playlist = UUID.randomUUID().toString();

            // Информация о плейлисте
            if (title_playlist.isEmpty()) {
                title_playlist = "Сгенерированный";
            }

            int colvoaudio = Integer.parseInt(String.valueOf(list.size()));

            HashMap<String, Object> new_playlist = new HashMap<>();
            new_playlist.put("id_playlist", id_playlist);
            new_playlist.put("title_playlist", title_playlist);
            new_playlist.put("colvoaudio_playlist", colvoaudio);


            // Создание объекта Map для хранения всех обновлений
            HashMap<String, Object> updates = new HashMap<>();

            // Добавление информации о плейлисте
            updates.put("/users/" + currentUser + "/user_uploads/playlist/" + id_playlist + "/info_playlist", new_playlist);

            // Добавление всех аудиозаписей в плейлист
            for (Audio audio : list) {
                String id_audio = audio.getId_audio();
                updates.put("/users/" + currentUser + "/user_uploads/playlist/" + id_playlist + "/audio_playlist/" + id_audio, audio);
            }

            // Выполнение одного запроса для обновления базы данных
            database.getReference().updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(view.getContext(), "Плейлист сохранен", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(view.getContext(), "Ошибка создания плейлиста!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            
        } else {
            Toast.makeText(view.getContext(), "Ошибка сохранения. Плейлист пуст", Toast.LENGTH_SHORT).show();
        }
        
    }


    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.audio_item_predict, parent, false);
        return new AudioViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position, type_player, user_mood, list);
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




}

