package com.example.audiolibrary.Navigation.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.audiolibrary.Navigation.Navigation_Controller;
import com.example.audiolibrary.R;
import com.example.audiolibrary.RecyclerView.audiolistRecyclerView.Audio;
import com.example.audiolibrary.RecyclerView.audiolistRecyclerView.AudioAdapterPlaylistOpen;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PlaylistActivity extends AppCompatActivity {


    //Переменные данные плейлиста
    String ID_PLAYLIST, TITLE_PLAYLIST, COLVO_AUDIO_PLAYLIST;


    ImageView dots_button;

    //Переменные отображения данных плейлиста
    TextView title_playlist, colvoaudio_playlist;


    //Переменные списка аудиозаписей плейлиста и адаптер для этого списка
    RecyclerView playlistActivityRecyclerView;
    AudioAdapterPlaylistOpen audioListAdapter = new AudioAdapterPlaylistOpen();


    private final String type_player = "default";


    //Подключение к базе данных и модулю авторизации
    private FirebaseAuth authentication = FirebaseAuth.getInstance();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        init();


        //Получаем данные выбранного пользователем плейлиста
        Intent intent = getIntent();
        ID_PLAYLIST = intent.getStringExtra("playlist_id");
        TITLE_PLAYLIST = intent.getStringExtra("playlist_title");
        COLVO_AUDIO_PLAYLIST = intent.getStringExtra("playlist_colvoaudio");


        //Устанавливаем полученные сведения в поля информации плейлиста
        title_playlist.setText(TITLE_PLAYLIST);
        colvoaudio_playlist.setText("Добавлено аудиозаписей: " + COLVO_AUDIO_PLAYLIST);


        //Загружаем список аудиозаписей плейлиста после получения его ID_PLAYLIST
        if (ID_PLAYLIST != null) {
            loadAudioList();
        } else {
            Toast.makeText(this, "Ошибка получения данных плейлиста!", Toast.LENGTH_LONG).show();
        }




        // Отправка выбранного трека в блок с плеером
        audioListAdapter.setOnItemClickListener((position, audio_list) -> {

            Intent intent1 = new Intent(this, Navigation_Controller.class);

            intent1.putExtra("position", position);
            intent1.putExtra("type_player", type_player);
            intent1.putExtra("audio_list", audio_list);

            startActivity(intent1);
        });



        dots_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deletePlaylist(ID_PLAYLIST);
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public void init() {
        title_playlist = findViewById(R.id.title_playlist);
        colvoaudio_playlist = findViewById(R.id.info_playlist);

        dots_button = findViewById(R.id.dots_button);

        playlistActivityRecyclerView = findViewById(R.id.audio_playlist_recyclerview);

        // Устанавливаем менеджер
        playlistActivityRecyclerView.setLayoutManager(new LinearLayoutManager(PlaylistActivity.this, LinearLayoutManager.VERTICAL, false));
        playlistActivityRecyclerView.addItemDecoration(new DividerItemDecoration(PlaylistActivity.this, DividerItemDecoration.VERTICAL));

        // Устанавливаем адаптер для Recycler View
        playlistActivityRecyclerView.setAdapter(audioListAdapter);
    }


    private void loadAudioList () {

        Query query = database.getReference().child("users").child(authentication.getCurrentUser().getUid()).child("user_uploads").child("playlist").child(ID_PLAYLIST).child("audio_playlist");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot main_snapshot) {
                // Если данные найдены
                if (main_snapshot.exists()) {

                    // Инициализируем список для записи полученных аудиозаписей
                    ArrayList<Audio> audio_list = new ArrayList<Audio>();

                    // Проходимся по дочерним элементам основного снимка записей из базы данных
                    for (DataSnapshot audioSnapshot : main_snapshot.getChildren()) {

                        // Записываем полученные данные в переменные
                        String id_audio = audioSnapshot.getKey();
                        String id_user = audioSnapshot.child("id_user").getValue(String.class);
                        String title_audio = audioSnapshot.child("title_audio").getValue(String.class);
                        String performer_audio = audioSnapshot.child("performer_audio").getValue(String.class);
                        String url_audio = audioSnapshot.child("url_audio").getValue(String.class);

                        int mood_happy = audioSnapshot.child("mood_happy").getValue(Integer.class);
                        int mood_normal = audioSnapshot.child("mood_normal").getValue(Integer.class);
                        int mood_sad = audioSnapshot.child("mood_sad").getValue(Integer.class);
                        int mood_angry = audioSnapshot.child("mood_angry").getValue(Integer.class);

                        long timestamp = audioSnapshot.child("timestamp").getValue(Long.class);

                        //Создаем объект аудиозапись с полученными данными
                        Audio audio = new Audio(id_audio, id_user, title_audio, performer_audio, url_audio, mood_happy, mood_normal, mood_sad, mood_angry, timestamp);

                        //Передаем объект аудиозапись в список с аудиозаписямм
                        audio_list.add(audio);

                    }

                    // Передаем список с аудиозаписями в адаптер Recycler View
                    audioListAdapter.setList(audio_list);

                    // Уведомить адаптер об изменении данных
                    audioListAdapter.notifyDataSetChanged();

                } else {

                    Toast.makeText(PlaylistActivity.this, "Аудиозаписи не найдены!", Toast.LENGTH_SHORT).show();

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(PlaylistActivity.this, "Ошибка: " + error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }


    private void deletePlaylist (String ID_PLAYLIST) {

        Task<Void> query = database.getReference("users").child(authentication.getCurrentUser().getUid().toString()).child("user_uploads").child("playlist").child(ID_PLAYLIST).removeValue();

        query.addOnSuccessListener(unused -> {

            Toast.makeText(PlaylistActivity.this, "Плейлист удален!", Toast.LENGTH_SHORT).show();

            finish();


        });


    }
}