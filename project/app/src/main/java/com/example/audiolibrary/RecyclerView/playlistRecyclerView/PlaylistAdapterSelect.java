package com.example.audiolibrary.RecyclerView.playlistRecyclerView;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.audiolibrary.R;
import com.example.audiolibrary.RecyclerView.audiolistRecyclerView.Audio;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PlaylistAdapterSelect extends RecyclerView.Adapter<PlaylistViewHolder> {


    // Переменные для хранения получаемой аудиозаписи для добавления в плейлист
    private String ID_AUDIO;
    private Audio selectedAudio;

    private AlertDialog dialog;


    // Инициализация списка для хранения плейлистов
    private ArrayList <Playlist> playlistList = new ArrayList<>();


    // Подключение модулей Firebase
    private FirebaseAuth authentication = FirebaseAuth.getInstance();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();


    // Метод вызывается для установки полученного списка в список в адаптере
    public void setList (ArrayList <Playlist> playlistList) {

        this.playlistList = playlistList;
        notifyDataSetChanged();

    }


    // Метод вызывается для получения ID и самого объекта выбранной аудиозаписи
    public void getSelectedAudio (String ID_AUDIO, Audio selectedAudio, Context context) {
        this.ID_AUDIO = ID_AUDIO;
        this.selectedAudio = selectedAudio;
        //Toast.makeText(context, "Аудиозапись получена", Toast.LENGTH_SHORT).show();
    }


    // Метод вызывается для получения диалогового окна выбора плейлиста
    public void getDialog (AlertDialog dialog, Context context) {
        this.dialog = dialog;
        //Toast.makeText(context, "Диалог получен", Toast.LENGTH_SHORT).show();
    }


    // Метод вызывается для добавления полученной аудиозаписи в плейлист в базу данных
    private void addAudioInPlaylist (String ID_PLAYLIST, View view) {

        Query query_check = database.getReference("users").child(authentication.getCurrentUser().getUid().toString()).child("user_uploads").child("playlist").child(ID_PLAYLIST).child("audio_playlist").child(ID_AUDIO);
        query_check.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot main_snapshot) {

                if (main_snapshot.exists()) {

                    Toast.makeText(view.getContext(), selectedAudio.getTitle_audio() + " уже добавлена в плейлист!", Toast.LENGTH_SHORT).show();

                } else {

                    Task<Void> query = database.getReference("users").child(authentication.getCurrentUser().getUid().toString()).child("user_uploads").child("playlist").child(ID_PLAYLIST).child("audio_playlist").child(ID_AUDIO).setValue(selectedAudio);
                    query.addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                Task<Void> query_update_colvo = database.getReference("users").child(authentication.getCurrentUser().getUid().toString()).child("user_uploads").child("playlist").child(ID_PLAYLIST).child("info_playlist").child("colvoaudio_playlist").setValue(ServerValue.increment(1));
                                query_update_colvo.addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {

                                            dialog.dismiss();

                                            Toast.makeText(view.getContext(), selectedAudio.getTitle_audio() + " добавлена в этот плейлист", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(view.getContext(), "Ошибка добавления в плейлист", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item, parent, false);
        return new PlaylistViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, @SuppressLint("RecyclerView") int position) {

        // Хранение объекта текущего выбранного плейлиста
        Playlist currentPlaylist = playlistList.get(holder.getAdapterPosition());

        holder.title_playlist.setText(playlistList.get(position).title_playlist);

        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // Получение ID текущего выбранного плейлиста
                String ID_PLAYLIST = currentPlaylist.uid_playlist;

                // Вызов метода добавления аудиозаписи в плейлист в базе данных
                addAudioInPlaylist(ID_PLAYLIST, view);

            }
        });
    }


    @Override
    public int getItemCount() {
        if (playlistList != null) {
            return playlistList.size();
        } else {
            return 0;
        }
    }

}
