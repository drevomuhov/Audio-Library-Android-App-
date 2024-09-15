package com.example.audiolibrary.ViewPager1.screens2.screens;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.audiolibrary.R;
import com.example.audiolibrary.RecyclerView.audiolistRecyclerView.Audio;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FirstFragmentPredict extends Fragment {

    // Подключение модулей Firebase
    private FirebaseDatabase database = FirebaseDatabase.getInstance();


    private Handler handler = new Handler();
    private Runnable runnable;

    private long startTime = 0;

    // Добавляем переменную для отслеживания, перемещает ли пользователь ползунок
    private boolean userIsSeeking = false;

    private boolean liked_status = false;

    ImageView like_button;

    // Список лайкнутых аудиозаписей
    Set<String> likedAudioIds = new HashSet<>();
    boolean status_like = false;



    // Текущая позиция воспроизведения
    String user_mood;
    int position;
    private String current_play_position_url;
    private ArrayList<Audio> audioList = new ArrayList<>();


    // Плеер
    private MediaPlayer mediaPlayer;
    private TextView title_audio_player, performer_audio_player, time;
    private SeekBar seekBar;
    private ImageView player_previous_button, player_playPause_button, player_next_button, player_like_button;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bs_player_fragment1_predict, container, false);
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

            user_mood = getArguments().getString("user_mood");

            // Получаем ссылку на файл выбранной аудиозаписи
            String current_play_position_url = audioList.get(position).getUrl_audio();

            // Устанавливаем значения названия и исполнителя
            title_audio_player.setText(audioList.get(position).getTitle_audio());
            performer_audio_player.setText(audioList.get(position).getPerformer_audio());

            // Начинаем воспроизведение выбранной аудиозаписи
            startPlayingAudio(current_play_position_url);
        }



        player_like_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Audio selectedAudio = audioList.get(position);
                String id_audio = selectedAudio.getId_audio();

                if (!likedAudioIds.contains(id_audio)) {

                    // Добавляем ID в набор, чтобы избежать повторной оценки
                    likedAudioIds.add(id_audio);
                    status_like = true;

                    setLike(id_audio);

                    player_like_button.setImageResource(R.drawable.like_full);

                } else {

                    Toast.makeText(view.getContext(), "Вы уже оценили эту аудиозапись!", Toast.LENGTH_SHORT).show();

                }
            }
        });



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

        like_button = view.findViewById(R.id.like_button);

        time = view.findViewById(R.id.time_player);
        seekBar = view.findViewById(R.id.player_audio_progress);

        player_like_button = view.findViewById(R.id.player_like_button);
        player_like_button.setImageResource(R.drawable.like_empty);

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
                    startTime = System.currentTimeMillis();
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

            // Обновляем текстовые поля заголовка и исполнителя
            title_audio_player.setText(audioList.get(position).getTitle_audio());
            performer_audio_player.setText(audioList.get(position).getPerformer_audio());

            // Проверяем, был ли трек лайкнут, и обновляем изображение кнопки лайка
            String id_audio = audioList.get(position).getId_audio();
            if (likedAudioIds.contains(id_audio)) {
                player_like_button.setImageResource(R.drawable.like_full);
            } else {
                player_like_button.setImageResource(R.drawable.like_empty);
            }

            // Если медиа проигрывается, останавливаем его
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }

            // Сбрасываем MediaPlayer и запускаем новый трек
            mediaPlayer.reset();
            startPlayingAudio(current_play_position_url);
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop(); // Останавливаем воспроизведение
                long duration = System.currentTimeMillis() - startTime; // Вычисляем длительность
                if (duration < 5000) { // Проверяем если длительность менее 5 секунд
                    Toast.makeText(getContext(), "Трек проигрывался менее 5 секунд", Toast.LENGTH_SHORT).show();
                }
            }
            mediaPlayer.release(); // Освобождаем ресурсы MediaPlayer
            mediaPlayer = null;
        }

        handler.removeCallbacks(runnable);
    }


    // Лайк аудиозаписи
    private void setLike (String id_audio) {

        database.getReference("uploads").child("audio").child(id_audio).child(user_mood).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {

                    // Получаем строковое представление текущего настроения
                    int moodValueString = snapshot.getValue(Integer.class);
                    moodValueString = moodValueString + 5;

                    database.getReference("uploads").child("audio").child(id_audio).child(user_mood).setValue(moodValueString);

                    Toast.makeText(getContext(), "Аудиозапись оценена!", Toast.LENGTH_SHORT).show();

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


}
