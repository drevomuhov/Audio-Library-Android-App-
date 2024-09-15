package com.example.audiolibrary.Navigation.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.audiolibrary.Navigation.Navigation_Controller;
import com.example.audiolibrary.R;
import com.example.audiolibrary.RecyclerView.audiolistRecyclerView.Audio;
import com.example.audiolibrary.RecyclerView.audiolistRecyclerView.AudioAdapterALL;
import com.example.audiolibrary.RecyclerView.userfeedRecyclerView.FeedItem;
import com.example.audiolibrary.RecyclerView.userfeedRecyclerView.FeedItemAdapter;
import com.example.audiolibrary.RecyclerView.userpageRecyclerView.UserInfo;
import com.example.audiolibrary.RecyclerView.userpageRecyclerView.UserPageAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserPage extends AppCompatActivity {

    // Покдлючение модулей Firebase
    private FirebaseAuth authentication = FirebaseAuth.getInstance();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    RecyclerView recyclerView, recycler_view_feed;


    ArrayList<UserInfo> userInfoList = new ArrayList();
    UserPageAdapter userPageAdapter = new UserPageAdapter();


    AudioAdapterALL audioAdapterALL = new AudioAdapterALL();

    FeedItemAdapter feedItemAdapter = new FeedItemAdapter();


    ArrayList<String> user_audio_list = new ArrayList<>();
    ArrayList<Audio> user_audio_list_audio = new ArrayList<>();
    ArrayList<String> current_user_audio_list = new ArrayList<>();



    // Данные о профиле пользователя
    private String USER_ID, USER_NAME, USER_AUDIO, USER_FRIEND, USER_REG_DATE;
    private String image_url;
    private ImageView profile_image;
    private TextView name_user_field, match_field;


    private RecyclerView recycler_view_audio;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);

        init();

        loadUserProfileInfo();
        matchesCalculate();
        loadFeed();
        loadUserImage();
        loadAudioList();


        // Записываем полученные данные в список userInfoList (key, value)
        userInfoList.add(new UserInfo("Аудиозаписи", USER_AUDIO));
        userInfoList.add(new UserInfo("Подписки", USER_FRIEND));
        userInfoList.add(new UserInfo("Регистрация", USER_REG_DATE));

        // Передаем список в адаптер userPageAdapter для Recycler View
        userPageAdapter.setList(userInfoList);

        // Устанавливаем занчение имени пользвоателя в поле имени пользователя
        name_user_field.setText(USER_NAME);


        // Отправка выбранного трека в блок с плеером
        audioAdapterALL.setOnItemClickListener((position, type_player, user_mood, audioArrayList) -> {
            Intent intent1 = new Intent(this, Navigation_Controller.class);

            intent1.putExtra("position", position);
            intent1.putExtra("type_player", type_player);
            intent1.putExtra("audio_list", audioArrayList);

            startActivity(intent1);
        });



    }

    public void init () {

        profile_image = findViewById(R.id.profile_image);

        name_user_field = findViewById(R.id.name_user_field);
        match_field = findViewById(R.id.match_field);

        recycler_view_audio = findViewById(R.id.recycler_view_audio);
        recycler_view_audio.setAdapter(audioAdapterALL);
        recycler_view_audio.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(userPageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));


        recycler_view_feed = findViewById(R.id.recycler_view_feed);
        recycler_view_feed.setAdapter(feedItemAdapter);
        recycler_view_feed.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
    }

    private void loadUserProfileInfo () {

        // Получаем данные о пользователе при запуске активности
        Intent intent = getIntent();
        USER_ID = intent.getStringExtra("user_id");
        USER_NAME = intent.getStringExtra("user_name");
        USER_AUDIO = intent.getStringExtra("user_audio");
        USER_FRIEND = intent.getStringExtra("user_friend");
        USER_REG_DATE = intent.getStringExtra("user_reg_date");

        feedItemAdapter.setFeedUserID(USER_ID);
    }

    private void loadUserImage () {
        Query getImage = database.getReference("users").child(USER_ID).child("user_data").child("profile_image");
        getImage.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    image_url = snapshot.getValue(String.class);
                    Glide.with(getApplication()).load(image_url).into(profile_image);
                } else {
                    profile_image.setImageResource(R.drawable.ic_profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Ошибка получения автарки", error.getMessage());
            }
        });
    }


    private void loadFeed () {
        Query query = database.getReference().child("users").child(USER_ID).child("user_feed").orderByChild("timestamp");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot main_snapshot) {
                // Если данные найдены
                if (main_snapshot.exists()) {

                    // Инициализируем список для записи полученных аудиозаписей
                    ArrayList<FeedItem> user_feed_list = new ArrayList<>();

                    // Проходимся по дочерним элементам основного снимка записей из базы данных
                    for (DataSnapshot FeedItem : main_snapshot.getChildren()) {

                        // Записываем полученные данные в переменные
                        String id_item = FeedItem.getKey();
                        String string_item = FeedItem.child("string_item").getValue(String.class);

                        long timestamp = FeedItem.child("timestamp").getValue(Long.class);

                        FeedItem new_feedItem = new FeedItem(id_item, string_item, timestamp);

                        //Передаем объект аудиозапись в список с аудиозаписямм
                        user_feed_list.add(new_feedItem);

                    }

                    // Передаем список с аудиозаписями в адапетер Recycler View
                    feedItemAdapter.setList(user_feed_list);

                    // Уведомить адаптер об изменении данных
                    feedItemAdapter.notifyDataSetChanged();


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Ошибка: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void matchesCalculate() {
        // Получаем список ID аудиозаписей пользователя
        Query query = database.getReference("users").child(USER_ID).child("user_uploads").child("audio");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot main_snapshot) {
                if (main_snapshot.exists()) {
                    for (DataSnapshot audioSnapshot : main_snapshot.getChildren()) {
                        String id_audio = audioSnapshot.getKey();
                        user_audio_list.add(id_audio);
                    }

                    Log.d("matchesCalculate", "Список пользователя получен");
                    // Получаем список ID аудиозаписей текущего пользователя
                    Query query1 = database.getReference("users").child(authentication.getCurrentUser().getUid()).child("user_uploads").child("audio");
                    query1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot main_snapshot) {
                            if (main_snapshot.exists()) {
                                for (DataSnapshot audioSnapshot : main_snapshot.getChildren()) {
                                    String id_audio = audioSnapshot.getKey();
                                    current_user_audio_list.add(id_audio);
                                }
                                Log.d("matchesCalculate", "Список текущего пользователя получен");
                                matchCalculate2(user_audio_list, current_user_audio_list, current_user_audio_list.size());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("matchesCalculate", error.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("matchesCalculate", error.getMessage());
            }
        });
    }

    private void matchCalculate2(ArrayList<String> user_audio_list, ArrayList<String> current_user_audio_list, int currentUserAudioSize) {
        Log.d("matchesCalculate", "Начала расчета совпадения");
        int matches = 0;
        for (String id : user_audio_list) {
            if (current_user_audio_list.contains(id)) {
                matches++;
            }
        }
        double match_percent = (double) matches / currentUserAudioSize * 100;

        String message = "Музыкальное совпадение: " + String.format("%.1f", match_percent) + "%";
        match_field.setText(message);
    }


    private void loadAudioList () {

        Query query = database.getReference("users").child(USER_ID).child("user_uploads").child("audio");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot main_snapshot) {
                for (DataSnapshot snapshot : main_snapshot.getChildren()) {
                    String id_audio = snapshot.getKey();
                    String id_user = snapshot.child("id_user").getValue(String.class);
                    String title_audio = snapshot.child("title_audio").getValue(String.class);
                    String performer_audio = snapshot.child("performer_audio").getValue(String.class);
                    String url_audio = snapshot.child("url_audio").getValue(String.class);
                    int mood_happy = snapshot.child("mood_happy").getValue(Integer.class);
                    int mood_normal = snapshot.child("mood_normal").getValue(Integer.class);
                    int mood_sad = snapshot.child("mood_sad").getValue(Integer.class);
                    int mood_angry = snapshot.child("mood_angry").getValue(Integer.class);
                    long timestamp = snapshot.child("timestamp").getValue(Long.class);

                    Audio audio = new Audio(id_audio, id_user, title_audio, performer_audio, url_audio, mood_happy, mood_normal, mood_sad, mood_angry, timestamp);
                    user_audio_list_audio.add(audio);
                }

                audioAdapterALL.setList(user_audio_list_audio);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }



}