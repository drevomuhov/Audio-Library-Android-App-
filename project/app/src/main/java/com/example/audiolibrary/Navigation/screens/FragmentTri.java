package com.example.audiolibrary.Navigation.screens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.audiolibrary.BottomSheetAddSubscribe;
import com.example.audiolibrary.R;
import com.example.audiolibrary.RecyclerView.friendlistRecyclerView.Friend;
import com.example.audiolibrary.RecyclerView.friendlistRecyclerView.FriendAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FragmentTri extends Fragment {


    // Подключение модулей Firebase
    private FirebaseAuth authentication = FirebaseAuth.getInstance();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();


    // Переменная для хранения текущего пользователя
    private final String currentUser = authentication.getCurrentUser().getUid().toString();


    SearchView search_field;
    ImageView addFriendButton;
    RecyclerView friend_recycler_view;

    TextView no_friends, describe;


    ArrayList<Friend> friend_list = new ArrayList<Friend>();
    FriendAdapter friendAdapter = new FriendAdapter();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Получаем ссылку на разметку фрагмента
        View view = inflater.inflate(R.layout.fragment_tri, container, false);
        // Возвращаем разметку фрагмента
        return view;

    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        init(view);
        LoadFriendList();


        search_field.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_field.setIconified(false);
            }
        });


        search_field.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                friendAdapter.setQuery(query);
                return false;
            }
        });


        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                BottomSheetAddSubscribe bottomSheetAddSubscribe = new BottomSheetAddSubscribe();
                bottomSheetAddSubscribe.show(getActivity().getSupportFragmentManager(), bottomSheetAddSubscribe.getTag());

            }
        });


    }


    public void init (View view) {

        no_friends = view.findViewById(R.id.no_friends);
        describe = view.findViewById(R.id.describe);
        no_friends.setVisibility(View.GONE);
        describe.setVisibility(View.GONE);


        // Верхняя панель (поиск и кнопка)

        search_field = view.findViewById(R.id.search_field_fragment);

        addFriendButton = view.findViewById(R.id.addFriendButton);


        // Список друзей

        friend_recycler_view  = getActivity().findViewById(R.id.friend_recycler_view);

        // Устанавливаем адаптер для Recycler View
        friend_recycler_view.setAdapter(friendAdapter);

        // Устанавливаем менеджер компоновки (располагает элементы списка в вертикальном порядке) для Recycler View
        friend_recycler_view.setLayoutManager(new LinearLayoutManager(getContext()));
        friend_recycler_view.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }



    // УСТАНОВЛЕН ПОСТОЯННЫЙ СЛУШАТЕЛЬ!!! НУЖНО УБРАТЬ И РЕАЛИЗОВАТЬ ОБНОВЛЕНИЕ ЧЕРЕЗ АДАПТЕР!

    // Метод загрузки списка друзей текущего пользователя
    private void LoadFriendList() {
        Query query = database.getReference("users").child(currentUser).child("user_friends");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot main_snapshot) {

                friend_list.clear(); // Очистить список перед заполнением новыми данными

                if (main_snapshot.exists()) {

                    // Проходимся по дочерним элементам основного снимка записей из базы данных
                    for (DataSnapshot friendSnapshot : main_snapshot.getChildren()) {

                        // Записываем полученные данные в переменные
                        String uid_user = friendSnapshot.child("uid_user").getValue().toString();
                        String name_user = friendSnapshot.child("name_user").getValue().toString();
                        String colvoaudio_user = friendSnapshot.child("colvoaudio_user").getValue().toString();
                        String colvofriend_user = friendSnapshot.child("colvofriend_user").getValue().toString();
                        String reg_date_user = friendSnapshot.child("reg_date_user").getValue().toString();

                        //Создаем объект пользователь(друг) с полученными данными
                        Friend friend = new Friend(uid_user, name_user, colvoaudio_user, colvofriend_user, reg_date_user);

                        //Передаем объект пользователь(друг) в список друзей пользователя
                        friend_list.add(friend);
                    }

                    no_friends.setVisibility(View.GONE);
                    describe.setVisibility(View.GONE);

                } else {
                    no_friends.setVisibility(View.VISIBLE);
                    describe.setVisibility(View.VISIBLE);
                }

                // Передаем список с друзьями пользователя в адапетер Recycler View
                friendAdapter.setList(friend_list);

                // Уведомить адаптер об изменении данных
                friendAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}