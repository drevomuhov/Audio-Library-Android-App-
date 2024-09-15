package com.example.audiolibrary.Navigation.screens;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.audiolibrary.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;


public class PlayerActivity extends AppCompatActivity {

    TextView title_audio_player, performer_audio_player, time_player;
    ImageView next1_button_player, play_button_player, next2_button_player;
    SeekBar player_audio_progress;

    String TITLE_AUDIO, PERFORMER_AUDIO, URL_AUDIO, MOOD_HAPPY, MOOD_SAD, MOOD_NORMAL;




    // Подключение модулей Firebase
    private FirebaseAuth authentication = FirebaseAuth.getInstance();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();



    // Создание объекта MediaPlayer
    MediaPlayer mediaPlayer = new MediaPlayer();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        init();

        // Получаем данные выбранного пользователем плейлиста
        Intent intent = getIntent();
        TITLE_AUDIO = intent.getStringExtra("audio_title");
        PERFORMER_AUDIO = intent.getStringExtra("audio_performer");
        URL_AUDIO = intent.getStringExtra("audio_url");

        MOOD_HAPPY = intent.getStringExtra("audio_mood_happy");
        MOOD_SAD = intent.getStringExtra("audio_mood_sad");
        MOOD_NORMAL = intent.getStringExtra("audio_mood_normal");






        title_audio_player.setText(TITLE_AUDIO);
        performer_audio_player.setText(PERFORMER_AUDIO);





        // Запускаем аудиоплеер
        audioPlayer(URL_AUDIO);


        // Кнопка проигрывания аудиозаписи
        play_button_player.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    play_button_player.setImageResource(R.drawable.ic_player_play);
                } else {
                    mediaPlayer.start();
                    play_button_player.setImageResource(R.drawable.ic_player_pause);
                }

            }
        });

        // Кнопка промотки назад
        next1_button_player.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.seekTo(0);
            }
        });

        // Кнопка промотки вперед
        next2_button_player.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.seekTo(mediaPlayer.getDuration());
            }
        });



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop(); // Остановка воспроизведения
            }
            mediaPlayer.release(); // Освобождение ресурсов MediaPlayer
            mediaPlayer = null;
        }
    }

    private void init() {


        title_audio_player = findViewById(R.id.title_audio_player);
        performer_audio_player = findViewById(R.id.performer_audio_player);
        time_player = findViewById(R.id.time_player);

        player_audio_progress = findViewById(R.id.player_audio_progress);

        next1_button_player = findViewById(R.id.next1_button_player);

        play_button_player = findViewById(R.id.play_button_player);
        play_button_player.setImageResource(R.drawable.ic_player_pause);

        next2_button_player = findViewById(R.id.next2_button_player);

    }


    // Основной метод. Метод аудиоплеера
    private void audioPlayer(String URL_AUDIO) {

        // Установка источника звука (URL)
        try {
            mediaPlayer.setDataSource(URL_AUDIO);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Подготовка медиаплеера
        mediaPlayer.prepareAsync();

        // Обработчик события готовности к воспроизведению
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {

                // Устанавливаем максимальное значение для SeekBar равное продолжительности аудиозаписи
                player_audio_progress.setMax(mediaPlayer.getDuration());

                // Запуск воспроизведения аудиозаписи
                mediaPlayer.start();

                // Вызов метода обновления SeekBar медиаплеера
                updateSeekBar();


                // Слушатель событий для SeekBar медиаплеера
                player_audio_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            mediaPlayer.seekTo(progress);
                            player_audio_progress.setProgress(progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                // Слушатель событий для отображения полосы предварительной загрузки аудиозаписи в медиаплеере
                mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                    @Override
                    public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
                        double ratio = percent / 100;
                        int bufferingLevel = (int)(mediaPlayer.getDuration()*ratio);
                        player_audio_progress.setSecondaryProgress(bufferingLevel);
                    }
                });

            }
        });
    }


    // Метод вызывается для обновления текущей позиции воспроизведения в SeekBar
    private void updateSeekBar () {

        if (mediaPlayer != null) {

            // Создаем перменную для хранения текущей позиции воспроизведения аудио
            int currentPosition = mediaPlayer.getCurrentPosition();

            // Устанавливаем текущую позицию для SeekBar
            player_audio_progress.setProgress(currentPosition);

            // Преобразовываем currentPosition в текстовый формат (00:00)
            int minutes = currentPosition / 1000 / 60;
            int seconds = (currentPosition / 1000) % 60;
            String formattedCurrentPosition = String.format("%02d:%02d", minutes, seconds);

            // Устанавливаем преобразованный currentPosition в TextView
            time_player.setText(formattedCurrentPosition);


            // Инициализируем и устанавливаем повторитель выполнения метода updateSeekBar для обновления позиции SeekBar
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    updateSeekBar();
                }
            };

            // Инициализируем и устанавливаем таймер для повторителя выполнения метода updateSeekBar для обновления позиции SeekBar
            Handler handler = new Handler();
            handler.postDelayed(runnable, 1000);
        }
    }



}


