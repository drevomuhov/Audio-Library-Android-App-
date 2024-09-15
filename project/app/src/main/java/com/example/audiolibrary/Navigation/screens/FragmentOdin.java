package com.example.audiolibrary.Navigation.screens;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.audiolibrary.BottomSheetAdd;
import com.example.audiolibrary.R;
import com.example.audiolibrary.RecyclerView.audiolistRecyclerView.Audio;
import com.example.audiolibrary.RecyclerView.audiolistRecyclerView.AudioAdapter;
import com.example.audiolibrary.RecyclerView.playlistRecyclerView.Playlist;
import com.example.audiolibrary.RecyclerView.playlistRecyclerView.PlaylistAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class FragmentOdin extends Fragment {


    // Покдлючение модулей Firebase
    private FirebaseAuth authentication = FirebaseAuth.getInstance();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();


    // Переменная для хранения текущего пользователя
    private final String currentUser = authentication.getCurrentUser().getUid().toString();


    // Шапка с полем поиска и кнопкой "Добавить"
    private ImageView addButton;
    private SearchView search_field_fragment;


    // Блок с плейлистами пользователя
    private LinearLayout audiosBlock_content, audiosBlock_warning, playlistBlock, playlistBlock_content, playlistBlock_warning;
    private TextView playlistBlock_title, openBlock1;
    private boolean status_check1 = true;
    private RecyclerView playlist_recyclerview;
    private PlaylistAdapter playlistAdapter = new PlaylistAdapter();
    private ArrayList<Playlist> playlist_list = new ArrayList<>();


    // Блок с аудиозаписями пользвоателя
    private TextView openBlock2;
    private boolean status_check2 = true;


    private RecyclerView audiolist;

    private AudioAdapter audioAdapter = new AudioAdapter();
    private ArrayList<Audio> audio_list = new ArrayList<>();


    public interface OnFragmentSendDataListener {
        void onSendData(int position, String type_player, String user_mood, ArrayList<Audio> audioArrayList);
    }
    private OnFragmentSendDataListener fragmentSendDataListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            fragmentSendDataListener = (OnFragmentSendDataListener) context;
        } catch (ClassCastException e) {

        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadAudioAndPlayList();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_odin, container, false);
        return view;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        loadAudioAndPlayList();


        // Отправка выбранного трека в блок с плеером
        audioAdapter.setOnItemClickListener((position, type_player, user_mood, audioArrayList) -> {
            fragmentSendDataListener.onSendData(position, type_player, user_mood, audioArrayList);
        });


        openBlock1.setOnClickListener(view12 -> {

            // Изменение видимости audiolist и состояния status_check
            if (!status_check1) {

                playlistBlock_content.setVisibility(View.VISIBLE);
                openBlock1.setText("Скрыть все");
            } else {
                playlistBlock_content.setVisibility(View.GONE);
                openBlock1.setText("Показать все");
            }
            // Переключение значения status_check
            status_check1 = !status_check1;
        });


        openBlock2.setOnClickListener(view1 -> {
            // Изменение видимости audiolist и состояния status_check
            if (!status_check2) {
                audiosBlock_content.setVisibility(View.VISIBLE);
                openBlock2.setText("Скрыть все ");
            } else {
                audiosBlock_content.setVisibility(View.GONE);
                openBlock2.setText("Показать все");
            }
            // Переключение значения status_check
            status_check2 = !status_check2;
        });


        search_field_fragment.setOnClickListener(view13 -> search_field_fragment.setIconified(false));

        search_field_fragment.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                audioAdapter.setQuery(query);
                return false;
            }
        });

        search_field_fragment.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Скрываем блок при получении фокуса
                playlistBlock.setVisibility(View.GONE);
                openBlock1.setText("Показать все");
                status_check1 = false;

                openBlock2.setVisibility(View.GONE);

            } else {
                // Показываем блок при потере фокуса
                playlistBlock.setVisibility(View.VISIBLE);
                openBlock1.setText("Скрыть все");
                status_check1 = true;

                openBlock2.setVisibility(View.VISIBLE);
            }
        });

        addButton.setOnClickListener(view14 -> {

            BottomSheetAdd bottomSheetAdd = new BottomSheetAdd();
            bottomSheetAdd.show(getActivity().getSupportFragmentManager(), bottomSheetAdd.getTag());

        });

    }


    public void init (View view) {

        // Шапка с полем поиска и кнопкой "Добавить"
        search_field_fragment = view.findViewById(R.id.search_field_fragment);
        addButton = view.findViewById(R.id.addButton);


        // Блок с плейлистами пользователя
        playlistBlock = view.findViewById(R.id.playlistBlock);

        playlistBlock_title = view.findViewById(R.id.playlistBlock_title);
        openBlock1 = view.findViewById(R.id.openBlock1);

        playlistBlock_content = view.findViewById(R.id.playlistBlock_content);

        playlistBlock_warning = view.findViewById(R.id.playlistBlock_warning);


        playlist_recyclerview = view.findViewById(R.id.playlist_recyclerview);
        playlist_recyclerview.setAdapter(playlistAdapter);
        playlist_recyclerview.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));


        // Блок с аудиозаписями поользователя
        openBlock2 = view.findViewById(R.id.openBlock2);
        audiolist = view.findViewById(R.id.audio_recyclerview);

        audiosBlock_content = view.findViewById(R.id.audiosBlock_content);

        audiosBlock_warning = view.findViewById(R.id.audiosBlock_warning);

        audiolist.setVisibility(View.VISIBLE);
        audiolist.setAdapter(audioAdapter);

        // Устанавливаем менеджер
        audiolist.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        audiolist.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));


    }


    // Метод загрузки плейлистов и аудиозаписей текущего пользователя
    public void loadAudioAndPlayList() {
        DatabaseReference userUploadsRef = database.getReference()
                .child("users")
                .child(authentication.getCurrentUser().getUid())
                .child("user_uploads");

        userUploadsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    playlist_list.clear();
                    audio_list.clear();

                    // Загрузка плейлистов
                    DataSnapshot playlistSnapshot = dataSnapshot.child("playlist");
                    for (DataSnapshot snapshot : playlistSnapshot.getChildren()) {
                        String uid_playlist = snapshot.getKey();
                        String title_playlist = snapshot.child("info_playlist").child("title_playlist").getValue(String.class);
                        String colvoaudio_playlist = String.valueOf(snapshot.child("info_playlist").child("colvoaudio_playlist").getValue(Integer.class));
                        Playlist playlist = new Playlist(uid_playlist, title_playlist, colvoaudio_playlist);
                        playlist_list.add(playlist);
                    }

                    // Загрузка аудио
                    DataSnapshot audioSnapshot = dataSnapshot.child("audio");
                    for (DataSnapshot snapshot : audioSnapshot.getChildren()) {
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
                        audio_list.add(audio);
                    }

                    // Сортировка аудио по временной метке от самой ближней к текущей до самой дальней
                    Collections.sort(audio_list, (a1, a2) -> Long.compare(a2.getTimestamp(), a1.getTimestamp()));

                    // Обновление адаптеров
                    playlistAdapter.setList(playlist_list);
                    audioAdapter.setList(audio_list);

                    // Управление видимостью предупреждений
                    playlistBlock_warning.setVisibility(playlist_list.isEmpty() ? View.VISIBLE : View.GONE);
                    audiosBlock_warning.setVisibility(audio_list.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(getTag(), error.getMessage());
            }
        });
    }






}