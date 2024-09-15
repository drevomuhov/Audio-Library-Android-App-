package com.example.audiolibrary.Navigation.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.audiolibrary.BottomSheetProfileSettings;
import com.example.audiolibrary.R;
import com.example.audiolibrary.RecyclerView.userfeedRecyclerView.FeedItem;
import com.example.audiolibrary.RecyclerView.userfeedRecyclerView.FeedItemAdapter;
import com.example.audiolibrary.RecyclerView.userpageRecyclerView.UserInfo;
import com.example.audiolibrary.RecyclerView.userpageRecyclerView.UserPageAdapter;
import com.example.audiolibrary.SplashScreen;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class FragmentChetire extends Fragment {

    TextView user_name_field, user_email;

    ImageButton settings_button, exit_button;


    RecyclerView recyclerView;
    ArrayList<UserInfo> userInfoList = new ArrayList();
    UserPageAdapter userPageAdapter = new UserPageAdapter();



    String USER_AUDIO, USER_FRIEND, USER_REG_DATE;


    private FirebaseAuth authentication = FirebaseAuth.getInstance();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    private RecyclerView feed_recycler_view;
    private FeedItemAdapter feedItemAdapter = new FeedItemAdapter();
    private Animation animation_rotate;
    private BottomSheetProfileSettings bottomSheetProfileSettings = new BottomSheetProfileSettings();


    // Ссылка на файл с изображением профиля
    private String image_url;
    private ImageView profile_image;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chetire, container, false);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        if (authentication.getCurrentUser() != null) {
            loadUserData();
            loadUserImage();
            loadUserInfo();
            loadUserFeed();
        } else {
            Intent intent = new Intent(getActivity(), SplashScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            authentication.signOut();
            startActivity(intent);
        }



        settings_button.setOnClickListener(view1 -> {
            settings_button.startAnimation(animation_rotate);
            bottomSheetProfileSettings = new BottomSheetProfileSettings();
            bottomSheetProfileSettings.show(getActivity().getSupportFragmentManager(), bottomSheetProfileSettings.getTag());
        });

        exit_button.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SplashScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            authentication.signOut();
            startActivity(intent);
        });

    }

    public void init (View view) {

        animation_rotate = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);

        profile_image = view.findViewById(R.id.profile_image);

        user_name_field = view.findViewById(R.id.user_name_field);
        user_email = getActivity().findViewById(R.id.user_email);


        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(userPageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));


        feed_recycler_view = view.findViewById(R.id.feed_recycler_view);
        feed_recycler_view.setAdapter(feedItemAdapter);
        feed_recycler_view.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        feedItemAdapter.setFeedUserID(authentication.getCurrentUser().getUid());



        settings_button = getActivity().findViewById(R.id.settings_button);
        exit_button = getActivity().findViewById(R.id.exit_button);

    }

    private void loadUserData () {

        // Получаем данные пользователя из базы данных Firebase Realtime Database
        Query query = database.getReference().child("users").child(authentication.getCurrentUser().getUid()).child("user_data");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot main_snapshot) {

                if (main_snapshot.exists()) {
                    // Записываем полученные данные в переменные
                    String login = main_snapshot.child("login").getValue().toString();
                    String email = main_snapshot.child("email").getValue().toString();

                    // Отображаем данные в текстовые поля на активити (фрагменте)
                    user_name_field.setText(login);
                    user_email.setText(email);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void loadUserImage () {
        Query getImage = database.getReference("users").child(authentication.getCurrentUser().getUid()).child("user_data").child("profile_image");
        getImage.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    image_url = snapshot.getValue(String.class);
                    if (isAdded()) {
                        Glide.with(getActivity()).load(image_url).into(profile_image);
                    }
                } else {
                    profile_image.setImageResource(R.drawable.ic_profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(getTag(), error.getMessage());
            }
        });
    }


    private void loadUserInfo () {
        // Получаем информацию о пользователе из базы данных Firebase Realtime Database
        database.getReference().child("users").child(authentication.getCurrentUser().getUid()).child("user_information").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot main_snapshot) {

                if (main_snapshot.exists()) {
                    // Записываем полученные данные в переменные
                    USER_AUDIO = main_snapshot.child("colvo_audio").getValue().toString();
                    USER_FRIEND = main_snapshot.child("colvo_friends").getValue().toString();
                    USER_REG_DATE = main_snapshot.child("register_date").getValue().toString();


                    // Записываем полученные данные в список userInfoList (key, value)
                    userInfoList.add(new UserInfo("Аудиозаписи", USER_AUDIO));
                    userInfoList.add(new UserInfo("Подписки", USER_FRIEND));
                    userInfoList.add(new UserInfo("Регистрация", USER_REG_DATE));

                    // Передаем список в адаптер userPageAdapter для Recycler View
                    userPageAdapter.setList(userInfoList);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void loadUserFeed () {

        Query query = database.getReference().child("users").child(authentication.getCurrentUser().getUid()).child("user_feed").orderByChild("timestamp");
        query.addValueEventListener(new ValueEventListener() {
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

            }
        });


    }

}