package com.example.audiolibrary.Navigation.screens;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.audiolibrary.R;
import com.example.audiolibrary.RecyclerView.audiolistRecyclerView.Audio;
import com.example.audiolibrary.RecyclerView.audiolistRecyclerView.AudioAdapterPredict;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;

public class PlayerPredictActivity extends AppCompatActivity {

    private FirebaseAuth authentication = FirebaseAuth.getInstance();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    TextView title_audio_player, performer_audio_player, time_player, emotional_player_analize_status;
    ImageView image_audio_player, next1_button_player, play_button_player, next2_button_player;
    SeekBar player_audio_progress;
    ToggleButton toggleButton;
    String ID_AUDIO, TITLE_AUDIO, PERFORMER_AUDIO, URL_AUDIO, MOOD_HAPPY, MOOD_SAD, MOOD_NORMAL;



    // Настроение пользователя
    String USER_MOOD;

    AudioAdapterPredict audioAdapterPredict = new AudioAdapterPredict();


    // Настроение аудиозаписи
    String dominantMood, leastDominantMood;


    // Переменная, в которой записывается время открытия активности
    private long startTime;




    // Создание объекта MediaPlayer
    MediaPlayer mediaPlayer = new MediaPlayer();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Установка начального времени при создании активности
        startTime = System.currentTimeMillis();

        setContentView(R.layout.activity_player_predict);

        init();

        // Создаем Handler и Runnable для отложенного выполнения кода
        final Handler handler = new Handler();
        final Runnable checkActivity = new Runnable() {
            @Override
            public void run() {
                // Получаем текущее время
                long currentTime = System.currentTimeMillis();
                // Проверяем, прошло ли 5 секунд с момента startTime
                if (currentTime - startTime >= 5000) {
                    startTimer();
                }
            }
        };

        // Запускаем Runnable с задержкой в 5 секунд
        handler.postDelayed(checkActivity, 5000);





        // ОТСЮДА СТАРТУЕМ!!!!

        // Получаем список аудиозаписей из плейлиста
        Intent intent = getIntent();
        ArrayList<Audio> audioList = (ArrayList<Audio>) intent.getSerializableExtra("audio_list");

        // Получаем позицию выбранной аудиозаписи на воспроизведение
        int clickedPosition = getIntent().getIntExtra("clicked_position", 0);


        // ПЛЕЕР

        // Текущая позиция воспроизведения
        final int[] current_position = {clickedPosition};
        String currentPositionUrl = audioList.get(clickedPosition).getUrl_audio();

        // Запускаем аудиоплеер
        audioPlayer(currentPositionUrl);







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
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();

                // Увеличиваем позицию на 1, проверяем, не вышли ли мы за пределы списка
                int next_position = (current_position[0] + 1) % audioList.size();

                // Обновляем текущую позицию
                current_position[0] = next_position;

                // Устанавливаем название и URL следующего аудио
                title_audio_player.setText(audioList.get(next_position).getTitle_audio());
                String nextAudio = audioList.get(next_position).getUrl_audio();

                try {
                    mediaPlayer.setDataSource(nextAudio);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    // Обработка IllegalStateException
                    Log.e("MediaPlayer", "MediaPlayer не в корректном состоянии", e);
                }
            }
        });

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Вычисление времени существования активности
       // long duration = System.currentTimeMillis() - startTime;

        // Проверка, была ли активность закрыта быстрее 20 секунд
       // if (duration < 10000) {

            //String[] mood = getDominantMood(MOOD_HAPPY, MOOD_SAD, MOOD_NORMAL, USER_MOOD);

           // minusMood(mood);

       // }

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
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
        next2_button_player = findViewById(R.id.next2_button_player);

        play_button_player.setImageResource(R.drawable.ic_player_pause);



        emotional_player_analize_status = findViewById(R.id.emotional_player_analize_status);



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




    // Метод вызывается для запуска таймера на 20 секунд прослушивания
    private void startTimer() {

        // Инициализация Handler и Runnable для таймера
        final Handler handler = new Handler();
        final Runnable startTimer = new Runnable() {
            int count = 0;
            int totalTime = 10000;
            boolean timer_status = false;

            @Override
            public void run() {
                if (mediaPlayer.isPlaying()) {
                    count += 1000; // Увеличиваем счетчик на 1 секунду
                    int percentage = (count * 100) / totalTime; // Рассчитываем процентное значение

                    // Проверяем, прошло ли 20 секунд
                    if (count >= totalTime && !timer_status) {

                        // Предполагаем, что getDominantMood возвращает массив с двумя элементами
                      //  String[] mood = getDominantMood(MOOD_HAPPY, MOOD_SAD, MOOD_NORMAL, USER_MOOD);

                       // plusMood(mood);

                        // Обновляем флаг после выполнения
                        timer_status = true;

                    } else if (count < totalTime) {

                        emotional_player_analize_status.setText("Проверка соотвествия выполнена на: " + percentage + "%");
                    }
                }
                if (!timer_status) {

                    // Обновление таймера каждую секунду
                    handler.postDelayed(this, 1000);

                }
            }
        };

        // Запуск таймера
        handler.post(startTimer);

    }



}