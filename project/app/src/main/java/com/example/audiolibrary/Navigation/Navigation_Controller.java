package com.example.audiolibrary.Navigation;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.example.audiolibrary.Navigation.screens.FragmentOdin;
import com.example.audiolibrary.R;
import com.example.audiolibrary.RecyclerView.audiolistRecyclerView.Audio;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Navigation_Controller extends AppCompatActivity implements FragmentOdin.OnFragmentSendDataListener {

    // Навигация в приложении
    private NavController navController;

    // Покдлючение модулей Firebase
    private FirebaseAuth authentication = FirebaseAuth.getInstance();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    // Блок с отображением проигрываемой аудиозаписи
    private CardView main;
    private ConstraintLayout playerBlock;
    private LinearLayout playerWarning;


    // Плеер
    private MediaPlayer mediaPlayer;
    private TextView title_current, title_audio_player, perfomer_audio_player, current_audio_play;
    private ImageView player_previous_button, player_playPause_button, player_next_button;
    private FloatingActionButton repeatButton, shuffleButton, addButton, likeButton, dislikeButton;
    private ImageView actionPlayer, closePlayer, image_audio;



    // SeekBar
    private SeekBar seekBar;
    private boolean userIsSeeking = false;
    private TextView time;
    private final Handler handler = new Handler();



    private int position;
    private String user_mood;
    private String type_player;
    private ArrayList<Audio> audio_list = new ArrayList<>();
    private Set<String> addedAudioIds = new HashSet<>();
    private Set<String> likedAudioIds = new HashSet<>();
    private Set<String> dislikedAudioIds = new HashSet<>();


    // Для кнопки repeat
    private int repeatStatus = 0;
    private int clickCount = 0;

    // Для кнопки shuffle
    private boolean isShuffled = false;
    private int originalPosition;


    // Переменные для анализа прослушивания (SeekBar)
    private boolean isSeekbar = false;
    private int last_position_seekBar = 0;
    private int current_position_seekbar = 0;
    private ArrayList<Audio> originalList; // Оригинальный список треков
    private ArrayList<Audio> shuffledList; // Перемешанный список треков


    // Переменные для анализа прослушивания
    private int current_position;
    private int last_position;
    private int total_position;
    private boolean isPause = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigationcontroller);

        init();
        loadAudioList();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
            NavigationUI.setupWithNavController(bottomNavigationView, navController);
        }

        // Получение данных из Intent (открытый плейлист)
        Intent intent = getIntent();
        if (intent != null) {
            this.position = intent.getIntExtra("position", -1);
            this.audio_list = (ArrayList<Audio>) intent.getSerializableExtra("audio_list");
            this.type_player = intent.getStringExtra("type_player");
            this.user_mood = intent.getStringExtra("user_mood");

            selectTrack(position);
        }

        actionPlayer.setOnClickListener(view -> {
            if (mediaPlayer != null && mediaPlayer.getCurrentPosition() != 0) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                } else {
                    mediaPlayer.start();
                }
                updatePlayPauseButton();
            }
        });

        closePlayer.setOnClickListener(view -> {
            if (mediaPlayer != null) {

                mediaPlayer.stop();

                playerWarning.setVisibility(View.VISIBLE);
                playerBlock.setVisibility(View.GONE);
                closePlayer.setVisibility(View.GONE);
                title_current.setText("Слишком тихо");
                current_audio_play.setText("Давай что-нибудь послушаем...");
                image_audio.setVisibility(View.GONE);

                updatePlayPauseButton();

                String message = "Воспроизведение отменено";
                Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();

            }
        });

        // Нужно чтобы не проходили нажатия сквозь cardview
        main.setOnTouchListener((v, event) -> true);


        // Кнопка "Прошлый трек"
        player_previous_button.setOnClickListener(v -> {
            position = (position - 1 + audio_list.size()) % audio_list.size();
            selectTrack(position);
        });


        // Кнопка "Старт/Пауза"
        player_playPause_button.setOnClickListener(view -> {

            if (mediaPlayer.isPlaying()) {

                if (type_player.equals("predict")) {
                    // Метка, что плеер был хотя бы раз остановлен во время воспроизвдения
                    isPause = true;
                    checkActiveListening();
                }
                mediaPlayer.pause();
                updatePlayPauseButton();
            } else {
                mediaPlayer.start();
                updatePlayPauseButton();
            }

        });


        // Кнопка "Следующий трек"
        player_next_button.setOnClickListener(v -> {
            if (type_player.equals("predict")) {
                if (!likedAudioIds.contains(audio_list.get(position).getId_audio()) && !dislikedAudioIds.contains(audio_list.get(position).getId_audio())) {
                    Toast.makeText(this, "Замер позиции аудио: " + audio_list.get(position).getTitle_audio() , Toast.LENGTH_SHORT).show();
                    int last_track_total_position = isSeekbar ?
                            mediaPlayer.getCurrentPosition() / 1000 - last_position_seekBar + total_position :
                            isPause ? total_position : mediaPlayer.getCurrentPosition() / 1000;

                    Audio audio = audio_list.get(position);
                    if (last_track_total_position > 10) {
                        if (last_track_total_position < 20) {
                            minusMood(audio);
                        } else {
                            plusMood(audio);
                        }
                    }

                    String message = "Прошлый трек проигрывался: " + last_track_total_position + " секунд";
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                }
            }

            position = (position + 1) % audio_list.size();
            selectTrack(position);

        });


        // Обработчик событий SeekBar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    String current_time = String.format("%02d:%02d", progress / 60000, (progress / 1000) % 60);
                    time.setText(current_time);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (type_player.equals("predict")) {
                    isSeekbar = true;
                    // Определяем текущую позицию воспроизвдения
                    if (!isPause) {
                        current_position_seekbar = mediaPlayer.getCurrentPosition() / 1000;
                    } else {
                        current_position_seekbar = last_position_seekBar;
                        isPause = false;
                    }
                    // Рассчитываем промежуток между последней позицией и текущей позицией в момент касания
                    int interval = current_position_seekbar - last_position_seekBar;
                    String message = "Длина отрезка воспроизведения (после SeekBar): " + interval;
                    Log.d("Длина отрезка воспроизведения", message);

                    // Убедимся, что добавляем промежуток только один раз
                    if (!userIsSeeking) {
                        total_position += interval;
                    }
                    String message2 = "Общая продолжительность: " + total_position;
                    Log.d("Общая продолжительность", message2);
                }

                userIsSeeking = true;
                mediaPlayer.pause();
                updatePlayPauseButton();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Обновляем lastPosition до текущей позиции в момент отпускания ползунка
                last_position_seekBar = mediaPlayer.getCurrentPosition() / 1000;

                userIsSeeking = false;
                mediaPlayer.start();
                updatePlayPauseButton();
            }
        });

        // Установка слушателя событий буферизации
        mediaPlayer.setOnBufferingUpdateListener((mp, percent) -> seekBar.setSecondaryProgress((int)(mp.getDuration() * (percent / 100.0))));


        // ДОПОЛНИТЕЛЬНЫЙ ФУНКЦИОНАЛ


        addButton.setOnClickListener(view -> {

            Audio audio = audio_list.get(position);
            addAudio(audio);

        });

        likeButton.setOnClickListener(view -> {

            if (type_player.equals("predict")) {
                setLike(audio_list.get(position).getId_audio());
            } else {
                String message = String.format("Достпуно только в режиме предсказания!");
                Toast.makeText(view.getContext(), message, Toast.LENGTH_SHORT).show();
            }

        });

        dislikeButton.setOnClickListener(view -> {
            if (type_player.equals("predict")) {
                setDisLike(audio_list.get(position).getId_audio());
            } else {
                String message = String.format("Достпуно только в режиме предсказания!");
                Toast.makeText(view.getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        shuffleButton.setOnClickListener(view -> {

            shuffleAudioList(audio_list);

        });

        repeatButton.setOnClickListener(view -> {
            clickCount++;
            if (clickCount > 2) {
                clickCount = 0;
            }
            repeatButtonUpdateUI(clickCount);
        });



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Освобождение ресурсов MediaPlayer
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


    public void init () {
        main = findViewById(R.id.main);

        title_current = findViewById(R.id.title_current);


        mediaPlayer = new MediaPlayer();

        playerBlock = findViewById(R.id.playerBlock);
        playerBlock.setVisibility(View.GONE);

        title_audio_player = findViewById(R.id.title_audio_player);
        perfomer_audio_player = findViewById(R.id.performer_audio_player);

        current_audio_play = findViewById(R.id.current_audio_play);
        current_audio_play.setText("Давай что-нибудь послушаем...");

        image_audio = findViewById(R.id.image_audio);


        player_previous_button = findViewById(R.id.player_previous_button);

        player_playPause_button = findViewById(R.id.player_play_button);
        player_playPause_button.setImageResource(R.drawable.ic_player_pause);

        player_next_button = findViewById(R.id.player_next_button);

        seekBar = findViewById(R.id.player_audio_progress);
        time = findViewById(R.id.time_player);



        repeatButton = findViewById(R.id.repeatButton);
        repeatButton.setImageResource(R.drawable.ic_player_repeat_off);
        shuffleButton = findViewById(R.id.shuffleButton);


        addButton = findViewById(R.id.addButton);
        likeButton = findViewById(R.id.like_button);
        dislikeButton = findViewById(R.id.dislike_button);


        playerWarning = findViewById(R.id.playerWarning);


        actionPlayer = findViewById(R.id.action_player);
        closePlayer = findViewById(R.id.delete_player);
        closePlayer.setVisibility(View.GONE);

    }

    @Override
    public void onSendData(int position, String type_player, String user_mood, ArrayList<Audio> audio_list) {
        this.position = position;
        this.audio_list = audio_list;
        this.type_player = type_player;
        this.user_mood = user_mood;


        selectTrack(position);
        playerBlock.setVisibility(View.VISIBLE);
    }

    private void loadAudioList () {
        Query query = database.getReference("users").child(authentication.getCurrentUser().getUid()).child("user_uploads").child("audio");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot main_snapshot) {
                if (main_snapshot.exists()) {
                    for (DataSnapshot childSnapshot : main_snapshot.getChildren()) {
                        String audioKey = childSnapshot.getKey();

                        if (!addedAudioIds.isEmpty()) {
                            addedAudioIds.clear();
                        }
                        addedAudioIds.add(audioKey);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Ошибка проверки наличия у пользователя трека", error.getMessage());
            }
        });
    }



    // Метод вызывается для обновления интерфейса плеера
    private void updateUI (Audio audio) {

        String id_audio = audio.getId_audio();

        check_add(audio);
        check_like(id_audio);
        setInfo(audio);

    }

    private void check_add(Audio audio) {

        int yellowColor = ContextCompat.getColor(getApplicationContext(), R.color.yellow);

        Query query = database.getReference("users").child(authentication.getCurrentUser().getUid()).child("user_uploads").child("audio");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot main_snapshot) {
                boolean isAudioPresent = false; // Переменная для отслеживания наличия аудио
                if (main_snapshot.exists()) {
                    for (DataSnapshot childSnapshot : main_snapshot.getChildren()) {
                        String audioKey = childSnapshot.getKey();
                        if (audioKey.equals(audio.getId_audio())) {
                            isAudioPresent = true;

                            if (!addedAudioIds.isEmpty()) {
                                addedAudioIds.clear();
                            }
                            addedAudioIds.add(audioKey);

                            break;
                        }
                    }
                    if (isAudioPresent) {
                        addButton.setImageResource(R.drawable.ic_ok);
                        addButton.getDrawable().setColorFilter(yellowColor, PorterDuff.Mode.SRC_IN);
                    } else {
                        addButton.setImageResource(R.drawable.ic_add2);
                        addButton.getDrawable().clearColorFilter();
                    }
                } else {
                    addButton.setImageResource(R.drawable.ic_add2);
                    addButton.getDrawable().clearColorFilter();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Ошибка проверки наличия у пользователя трека", error.getMessage());
            }
        });
    }

    private void check_like (String id_audio) {
        int yellowColor = ContextCompat.getColor(getApplicationContext(), R.color.yellow);
        if (likedAudioIds.contains(id_audio)) {
            likeButton.setImageResource(R.drawable.like_full);
            likeButton.getDrawable().setColorFilter(yellowColor, PorterDuff.Mode.SRC_IN);
        } else {
            likeButton.setImageResource(R.drawable.like_empty);
            likeButton.getDrawable().clearColorFilter();
        }
    }

    private void setInfo (Audio audio) {
        title_current.setText("Сейчас играет");
        current_audio_play.setText(audio.getTitle_audio() + " - " + audio.getPerformer_audio());
        title_audio_player.setText(audio.getTitle_audio());
        perfomer_audio_player.setText(audio.getPerformer_audio());
    }



    // Метод вызывается для добавления аудиозаписи в список пользователя
    private void addAudio(Audio audio) {
        int yellowColor = ContextCompat.getColor(getApplicationContext(), R.color.yellow);

        if (!addedAudioIds.contains(audio.getId_audio())) {

            DatabaseReference userRef = database.getReference("users").child(authentication.getCurrentUser().getUid());

            long timestamp = System.currentTimeMillis();

            // Создаем HashMap для новой аудиозаписи
            HashMap<String, Object> new_audio = new HashMap<>();
            new_audio.put("id_audio", audio.getId_audio());
            new_audio.put("id_user", audio.getId_user());
            new_audio.put("title_audio", audio.getTitle_audio());
            new_audio.put("performer_audio", audio.getPerformer_audio());
            new_audio.put("url_audio", audio.getUrl_audio());
            new_audio.put("mood_happy", audio.getMood_happy());
            new_audio.put("mood_sad", audio.getMood_sad());
            new_audio.put("mood_normal", audio.getMood_normal());
            new_audio.put("mood_angry", audio.getMood_angry());
            new_audio.put("timestamp", timestamp);

            // Создаем HashMap для пакетного обновления
            HashMap<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/user_uploads/audio/" + audio.getId_audio(), new_audio);
            childUpdates.put("/user_information/colvo_audio", ServerValue.increment(1));

            // Выполняем пакетное обновление
            userRef.updateChildren(childUpdates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    addButton.setImageResource(R.drawable.ic_ok);
                    addButton.getDrawable().setColorFilter(yellowColor, PorterDuff.Mode.SRC_IN);
                    String message = audio.getTitle_audio() + " добавлена в ваши аудиозаписи";
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                } else {
                    String message = "Произошла ошибка. Попробуйте позже";
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Log.e("Ошибка добавления аудиозаписи", e.getMessage());
            });
        } else {
            String message = audio.getTitle_audio() + " уже добавлена";
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }

    }


    // Метод вызывается для оценивания аудиозаписи из эмоционального плейлиста
    private void setLike (String id_audio) {

        int yellowColor = ContextCompat.getColor(getApplicationContext(), R.color.yellow);

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        if (dislikedAudioIds.contains(id_audio)) {

            String message = "Вы уже отрицательно оценили эту аудиозапись!";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        } else if (!dislikedAudioIds.contains(id_audio) && likedAudioIds.contains(id_audio)) {

        database.getReference("uploads").child("audio").child(id_audio).child(user_mood).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.exists()) {

                        // Получаем строковое представление текущего настроения
                        int mood_value = snapshot.getValue(Integer.class);

                        mood_value = mood_value - 5;

                        Task<Void> query = database.getReference("uploads").child("audio").child(id_audio).child(user_mood).setValue(mood_value);
                        query.addOnSuccessListener(unused -> {
                            if (query.isSuccessful()) {
                                likedAudioIds.remove(audio_list.get(position).getId_audio());
                                likeButton.getDrawable().clearColorFilter();
                                String message = String.format("Вы убрали оценку " + audio_list.get(position).getTitle_audio() + " - " + audio_list.get(position).getPerformer_audio());
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            } else {
                                String message = String.format("Возникла ошибка. Повторите позже");
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(e -> Log.e("Ошибка уменьшения оценки трека", e.getMessage()));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Ошибка уменьшения оценки трека", error.getMessage());
                }
            });

        } else {

            database.getReference("uploads").child("audio").child(id_audio).child(user_mood).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.exists()) {

                        // Получаем строковое представление текущего настроения
                        int mood_value = snapshot.getValue(Integer.class);

                        mood_value = mood_value + 5;

                        Task<Void> query = database.getReference("uploads").child("audio").child(id_audio).child(user_mood).setValue(mood_value);
                        query.addOnSuccessListener(unused -> {
                            if (query.isSuccessful()) {
                                likedAudioIds.add(audio_list.get(position).getId_audio());
                                likeButton.setImageResource(R.drawable.like_full);
                                likeButton.getDrawable().setColorFilter(yellowColor, PorterDuff.Mode.SRC_IN);

                                String message = String.format("Оценка для трека " + audio_list.get(position).getTitle_audio() + " - " + audio_list.get(position).getPerformer_audio() + " сохранена");
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            } else {
                                String message = String.format("Возникла ошибка. Повторите позже");
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(e -> Log.e("Ошибка оценки трека", e.getMessage()));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Ошибка оценки трека", error.getMessage());
                }
            });

        }

    }


    private void setDisLike (String id_audio) {

        int blackColor = ContextCompat.getColor(getApplicationContext(), R.color.black);

        int yellowColor = ContextCompat.getColor(getApplicationContext(), R.color.yellow);

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        if (likedAudioIds.contains(id_audio)) {

            String message = "Вы уже положительно оценили эту аудиозапись!";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        } else if (!likedAudioIds.contains(id_audio) && dislikedAudioIds.contains(id_audio)) {

            database.getReference("uploads").child("audio").child(id_audio).child(user_mood).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.exists()) {

                        // Получаем строковое представление текущего настроения
                        int mood_value = snapshot.getValue(Integer.class);

                        mood_value = mood_value + 5;

                        Task<Void> query = database.getReference("uploads").child("audio").child(id_audio).child(user_mood).setValue(mood_value);
                        query.addOnSuccessListener(unused -> {
                            if (query.isSuccessful()) {
                                dislikedAudioIds.remove(audio_list.get(position).getId_audio());
                                dislikeButton.getDrawable().setColorFilter(blackColor, PorterDuff.Mode.SRC_IN);
                                String message = String.format("Вы убрали оценку " + audio_list.get(position).getTitle_audio() + " - " + audio_list.get(position).getPerformer_audio());
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            } else {
                                String message = String.format("Возникла ошибка. Повторите позже");
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(e -> Log.e("Ошибка оценки трека", e.getMessage()));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Ошибка оценки трека", error.getMessage());
                }
            });

        } else {

            database.getReference("uploads").child("audio").child(id_audio).child(user_mood).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.exists()) {

                        // Получаем строковое представление текущего настроения
                        int mood_value = snapshot.getValue(Integer.class);

                        mood_value = mood_value - 5;

                        Task<Void> query = database.getReference("uploads").child("audio").child(id_audio).child(user_mood).setValue(mood_value);
                        query.addOnSuccessListener(unused -> {
                            if (query.isSuccessful()) {
                                dislikedAudioIds.add(audio_list.get(position).getId_audio());
                                dislikeButton.getDrawable().setColorFilter(yellowColor, PorterDuff.Mode.SRC_IN);

                                String message = String.format("Оценка для трека " + audio_list.get(position).getTitle_audio() + " - " + audio_list.get(position).getPerformer_audio() + " сохранена");
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            } else {
                                String message = String.format("Возникла ошибка. Повторите позже");
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(e -> Log.e("Ошибка оценки трека", e.getMessage()));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Ошибка оценки трека", error.getMessage());
                }
            });

        }
    }


    private void repeatButtonUpdateUI(int clickCount) {

        if ("predict".equals(type_player)) {
            String message = "В режиме \"Эмоциональный плейлист\" недоступно!";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            return;
        }

        int yellowColor = ContextCompat.getColor(getApplicationContext(), R.color.yellow);

        switch (clickCount) {
            case 1:
                repeatButton.setImageResource(R.drawable.ic_player_repeat_on);
                repeatStatus = 1;
                break;
            case 2:
                repeatButton.setImageResource(R.drawable.ic_player_repeat_once);
                repeatStatus = 2;
                break;
            default:
                repeatStatus = 0;
                repeatButton.setImageResource(R.drawable.ic_player_repeat_off);
                break;
        }

        // Применяем цветовой фильтр к текущему Drawable после установки иконки
        if (clickCount == 1 || clickCount == 2) {
            repeatButton.getDrawable().setColorFilter(yellowColor, PorterDuff.Mode.SRC_IN);
        } else {
            // Убираем цветовой фильтр для исходного состояния
            repeatButton.getDrawable().clearColorFilter();
        }
    }


    // Метод вызывается для пермешивания аудиозаписей в списке воспроизведения
    private void shuffleAudioList(ArrayList<Audio> audio_list) {

        if ("predict".equals(type_player)) {
            String message = "В режиме \"Эмоциональный плейлист\" недоступно!";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            return;
        }

        int yellowColor = ContextCompat.getColor(getApplicationContext(), R.color.yellow);

        if (!isShuffled) {
            // Сохраняем оригинальный список и позицию
            originalList = new ArrayList<>(audio_list);
            originalPosition = position;

            // Перемешиваем список и начинаем воспроизведение с начала
            Collections.shuffle(audio_list);
            shuffledList = new ArrayList<>(audio_list);
            isShuffled = true;
            shuffleButton.setImageResource(R.drawable.ic_player_shuffle_on);
            shuffleButton.getDrawable().setColorFilter(yellowColor, PorterDuff.Mode.SRC_IN);
            position = 0; // Начинаем воспроизведение с начальной позиции перемешанного списка
            selectTrack(position);
        } else {
            // Возвращаем исходный список и воспроизведение с изначальной позиции
            audio_list.clear();
            audio_list.addAll(originalList);
            isShuffled = false;
            shuffleButton.setImageResource(R.drawable.ic_player_shuffle_off);
            shuffleButton.getDrawable().clearColorFilter();
            position = originalPosition;
            selectTrack(position);
        }
    }



    // ЧАСТЬ АУДИОПЛЕЕР


    // Метод вызывается для выбора трека из списка для воспроизведения
    private void selectTrack(int position) {

        if (position >= 0 && position < audio_list.size()) {

            // Получение информации о треке один раз, чтобы избежать множественных вызовов
            Audio audio = audio_list.get(position);
            String audio_url = audio.getUrl_audio();

            // Обновление UI
            updateUI(audio);

            // Управление воспроизведением
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }


            resetVariables();
            startPlayingAudio(audio_url);

        }

    }


    // Метод вызывается для сброса переменных анализа режима Predict
    private void resetVariables () {
        isPause = false;
        last_position = 0;
        current_position = 0;
        isSeekbar = false;
        last_position_seekBar = 0;
        current_position_seekbar = 0;
        total_position = 0;
    }


    // Метод вызывается для запуска воспроизведения трека
    private void startPlayingAudio(String urlAudioFile) {
        try {
            mediaPlayer.reset(); // Сброс предыдущего состояния
            mediaPlayer.setDataSource(urlAudioFile);
            mediaPlayer.prepareAsync(); // Асинхронная подготовка

            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                updateSeekBar();
                updatePlayerUI(); // Обновление пользовательского интерфейса плеера
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                repeatAudioList(); // Обработка завершения трека
            });

        } catch (Exception e) {
            Log.e("Ошибка запуска плеера", e.getMessage());
        }
    }


    // Метод вызывается для обновления UI плеера
    private void updatePlayerUI() {
        if (mediaPlayer.isPlaying()) {
            playerWarning.setVisibility(View.GONE);
            playerBlock.setVisibility(View.VISIBLE);
        } else {
            playerWarning.setVisibility(View.VISIBLE);
            playerBlock.setVisibility(View.GONE);
        }

        closePlayer.setVisibility(View.VISIBLE);
        actionPlayer.setImageResource(R.drawable.ic_pause_playerblock);

        image_audio.setVisibility(View.VISIBLE);
        Glide.with(this).asGif().load(R.drawable.sound).into(image_audio);
        updatePlayPauseButton();
    }


    // Метод вызывается при включенной функции repeatButton
    private void repeatAudioList() {
        if (repeatStatus == 0) {
            position++;
            if (position >= audio_list.size()) {
                mediaPlayer.pause();
                updatePlayPauseButton();
                return; // Выход, если достигнут конец списка
            }
        } else if (repeatStatus == 1 && position >= audio_list.size() - 1) {
            position = 0; // Переход к началу списка
        }
        selectTrack(position);
    }


    // Метод вызывается для обновления иконки кнопки воспроизведения/паузы
    private void updatePlayPauseButton() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            actionPlayer.setImageResource(R.drawable.ic_pause_playerblock);
            Glide.with(this).asGif().load(R.drawable.sound).into(image_audio);
            player_playPause_button.setImageResource(R.drawable.ic_player_pause);
        } else {
            actionPlayer.setImageResource(R.drawable.ic_play_playerblock);
            image_audio.setImageResource(R.drawable.sound);
            player_playPause_button.setImageResource(R.drawable.ic_player_play);
        }
    }


    // Метод вызывается для обновления позиции воспроизведения трека на SeekBar
    private void updateSeekBar() {
        if (mediaPlayer != null && mediaPlayer.isPlaying() && !userIsSeeking) {
            int duration = mediaPlayer.getDuration();
            int current_position_seekbar = mediaPlayer.getCurrentPosition();

            seekBar.setMax(duration);
            seekBar.setProgress(current_position_seekbar);

            String current_time = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(current_position_seekbar),
                    TimeUnit.MILLISECONDS.toSeconds(current_position_seekbar) % 60);
            time.setText(current_time);
        }
        handler.postDelayed(this::updateSeekBar, 1000);
    }


    // Метод вызывается для подсчета активной продолжительности прослушивания (только режим Predict)
    private void checkActiveListening() {
        int interval;
        // Получаем текущую позицию воспроизведения трека
        current_position = mediaPlayer.getCurrentPosition() / 1000;
        // Рассчитываем длину отрезка воспроизведения трека
        if (!isSeekbar) {
            interval = current_position - last_position;
        } else {
            interval = current_position - last_position_seekBar;
            isSeekbar = false;
        }
        String message = "Длина отрезка воспроизведения: " + interval;
        Log.d("Длина отрезка воспроизведения", message);
        // Увеличиваем общую продоложительность воспроизвдения на длину отрезка
        if (interval > 0) {
            total_position += interval;
            String message2 = "Общая продолжительность: " + total_position;
            Log.d("Общая продолжительность", message2);
        }
        // Устанавливаем в качестве прошлой позиции текущуюю
        last_position = current_position;
    }


    // Метод вызывается для повышения значения настроения трека (только режим Predict)
    private void plusMood (Audio audio) {
        String id_audio = audio.getId_audio();
        Task<Void> query = database.getReference("uploads").child("audio").child(id_audio).child(user_mood).setValue(ServerValue.increment(1));
        query.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String message = "Повышение значения настроения для трека: " + audio.getTitle_audio() + " - " + audio.getPerformer_audio();
                Log.d("Повышение значения настроения", message);
            } else {
                String message = "Ошибка повышение значения настроения для трека: " + audio.getTitle_audio() + " - " + audio.getPerformer_audio();
                Log.e("Ошибка повышения значения настроения", message);
            }
        }).addOnFailureListener(e -> Log.d("Ошибка повышения значения настроения", e.getMessage()));
    }


    // Метод вызывается для повышения значения настроения трека (только режим Predict)
    private void minusMood (Audio audio) {
        String id_audio = audio.getId_audio();
        Task<Void> query = database.getReference("uploads").child("audio").child(id_audio).child(user_mood).setValue(ServerValue.increment(-1));
        query.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String message = "Понижение значения настроения для трека: " + audio.getTitle_audio() + " - " + audio.getPerformer_audio();
                Log.d("Понижение значения настроения", message);
            } else {
                String message = "Ошибка понижения значения настроения для трека: " + audio.getTitle_audio() + " - " + audio.getPerformer_audio();
                Log.e("Ошибка понижения значения настроения", message);
            }
        }).addOnFailureListener(e -> Log.d("Ошибка понижения значения настроения", e.getMessage()));
    }


}
