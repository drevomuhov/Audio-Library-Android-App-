package com.example.audiolibrary.ViewPager1.screens;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.audiolibrary.R;
import com.example.audiolibrary.RecyclerView.audiolistRecyclerView.Audio;

import java.util.ArrayList;

public class FirstFragment extends Fragment {


    private Handler handler = new Handler();
    private Runnable runnable;


    // Добавляем переменную для отслеживания, перемещает ли пользователь ползунок
    private boolean userIsSeeking = false;


    // Текущая позиция воспроизведения
    int position;
    private String current_play_position_url;
    private ArrayList<Audio> audioList = new ArrayList<>();


    // Плеер
    private MediaPlayer mediaPlayer;
    private TextView title_audio_player, performer_audio_player, time;
    private SeekBar seekBar;
    private ImageView player_previous_button, player_playPause_button, player_next_button;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bs_player_fragment1, container, false);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);
        updateSeekBar();

        // Получаем текущую позицию в списке и сам список с аудиозаписями
        if (getArguments() != null && getContext() != null) {

            // Получаем список аудиозаписей
            audioList = (ArrayList<Audio>) getArguments().getSerializable("audio_list");

            // Получаем позицию выбранной аудиозаписи
            position = getArguments().getInt("position");

            // Получаем ссылку на файл выбранной аудиозаписи
            String current_play_position_url = audioList.get(position).getUrl_audio();

            // Устанавливаем значения названия и исполнителя
            title_audio_player.setText(audioList.get(position).getTitle_audio());
            performer_audio_player.setText(audioList.get(position).getPerformer_audio());

            // Начинаем воспроизведение выбранной аудиозаписи
            startPlayingAudio(current_play_position_url );

        }


        player_previous_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ваш код для переключения на предыдущий трек
                changeTrack(position - 1);
            }
        });

        player_playPause_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause(); // Пауза, если воспроизводится
                    player_playPause_button.setImageResource(R.drawable.ic_player_play);
                } else {
                    mediaPlayer.start(); // Воспроизведение, если на паузе
                    player_playPause_button.setImageResource(R.drawable.ic_player_pause);
                }
            }
        });

        player_next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ваш код для переключения на предыдущий трек
                changeTrack(position + 1);
            }
        });


        // Обработчик событий SeekBar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {

                    mediaPlayer.seekTo(progress);

                    // Обновляем иконку кнопки в зависимости от состояния воспроизведения
                    updatePlayPauseButton();

                    // Обновляем поле Time для отображения позиции ползунка
                    time.setText(String.format("%02d:%02d", progress / 60000, (progress / 1000) % 60));

                    // Обновляем иконку кнопки в зависимости от состояния воспроизведения
                    updatePlayPauseButton();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                userIsSeeking = true;

                mediaPlayer.pause();

                // Устанавливаем иконку паузы, так как воспроизведение приостановлено
                player_playPause_button.setImageResource(R.drawable.ic_player_play);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                userIsSeeking = false; // Пользователь закончил перемещение ползунка
                mediaPlayer.start(); // Возобновляем воспроизведение
                updatePlayPauseButton(); // Обновляем иконку кнопки
                // Устанавливаем иконку воспроизведения, так как воспроизведение возобновлено
                player_playPause_button.setImageResource(R.drawable.ic_player_pause);
            }
        });



        // Установка слушателя событий буферизации
        mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
                double ratio = percent / 100.0;
                int bufferingLevel = (int)(mediaPlayer.getDuration() * ratio);
                seekBar.setSecondaryProgress(bufferingLevel);
            }
        });

    }



    public void init (View view) {

        // Плеер
        mediaPlayer = new MediaPlayer();

        title_audio_player = view.findViewById(R.id.title_audio_player);
        performer_audio_player = view.findViewById(R.id.performer_audio_player);

        player_previous_button = view.findViewById(R.id.player_previous_button);

        player_playPause_button = view.findViewById(R.id.player_play_button);
        player_playPause_button.setImageResource(R.drawable.ic_player_pause);

        player_next_button = view.findViewById(R.id.player_next_button);

        time = view.findViewById(R.id.time_player);
        seekBar = view.findViewById(R.id.player_audio_progress);

    }

    // Метод для обновления иконки кнопки воспроизведения/паузы
    private void updatePlayPauseButton() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            player_playPause_button.setImageResource(R.drawable.ic_player_pause);
        } else {
            player_playPause_button.setImageResource(R.drawable.ic_player_play);
        }
    }




    private void startPlayingAudio(String audioUrl) {
        try {
            mediaPlayer.setDataSource(getContext(), Uri.parse(audioUrl)); // Устанавливаем источник аудио
            mediaPlayer.prepareAsync(); // Асинхронная подготовка MediaPlayer
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start(); // Начинаем воспроизведение, когда MediaPlayer готов
                }
            });
        } catch (Exception e) {
            Log.e("MediaPlayer", "Ошибка при загрузке аудиофайла", e);
        }
    }

    private void updateSeekBar() {
        if (mediaPlayer != null) {

            if (mediaPlayer.isPlaying()) {
                seekBar.setMax(mediaPlayer.getDuration()); // Установка максимального значения
                if (!userIsSeeking) { // Проверяем, не перемещает ли пользователь ползунок в данный момент
                    seekBar.setProgress(mediaPlayer.getCurrentPosition()); // Обновление текущей позиции
                    time.setText(String.format("%02d:%02d", mediaPlayer.getCurrentPosition() / 60000, (mediaPlayer.getCurrentPosition() / 1000) % 60));
                }

            }
            // Запуск задачи обновления SeekBar каждую секунду
            handler.postDelayed(() -> updateSeekBar(), 1000);
        }
    }


    // Метод для смены трека
    private void changeTrack(int newPosition) {
        if (newPosition >= 0 && newPosition < audioList.size()) {

            // Обновляем текущую позицию
            position = newPosition;
            // Получаем новый URL
            current_play_position_url = audioList.get(position).getUrl_audio();

            title_audio_player.setText(audioList.get(position).getTitle_audio());
            performer_audio_player.setText(audioList.get(position).getPerformer_audio());

            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();

            }

            mediaPlayer.reset(); // Сбрасываем MediaPlayer
            startPlayingAudio(current_play_position_url); // Запускаем новый трек
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop(); // Останавливаем воспроизведение
            }
            mediaPlayer.release(); // Освобождаем ресурсы MediaPlayer
            mediaPlayer = null;
        }

        handler.removeCallbacks(runnable);
    }



}
