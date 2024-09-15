package com.example.audiolibrary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.audiolibrary.RecyclerView.userlistRecyclerView.User;
import com.example.audiolibrary.RecyclerView.userlistRecyclerView.UserAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BottomSheetAddSubscribe extends BottomSheetDialogFragment {


    // Подключение модулей Firebase
    private FirebaseAuth authentication = FirebaseAuth.getInstance();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();


    // Переменная для хранения текущего пользователя
    private final String currentUser = authentication.getCurrentUser().getUid().toString();


    SearchView search_field;
    RecyclerView user_recycler_view;


    UserAdapter userAdapter = new UserAdapter();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Получаем ссылку на разметку фрагмента
        View view = inflater.inflate(R.layout.bs_add_subcribe, container, false);

        init(view);
        getFriends();

        // Возвращаем разметку фрагмента
        return view;

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


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
                userAdapter.setQuery(query);
                return false;
            }
        });

    }


    public void init (View view) {

        search_field = view.findViewById(R.id.search_field_fragment);

        user_recycler_view = view.findViewById(R.id.user_recycler_view);

        // Устанавливаем адаптер для Recycler View
        user_recycler_view.setAdapter(userAdapter);

        // Устанавливаем менеджер компоновки (располагает элементы списка в вертикальном порядке) для Recycler View
        user_recycler_view.setLayoutManager(new LinearLayoutManager(getContext()));

        user_recycler_view.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }


    // Метод получения друзей текущего пользователя
    private void getFriends () {

        // Инициализируем список для записи ID друзей пользователя
        ArrayList<String> friendsList = new ArrayList<>();

        // Делаем запрос в базу данных
        Query query = database.getReference("users").child(currentUser).child("user_friends");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot main_snapshot) {

                // Если друзья были найдены
                if (main_snapshot.exists()) {

                    // Записываем ID каждого друга в список друзей пользователя
                        for (DataSnapshot snapshot : main_snapshot.getChildren()) {

                            String user_id = snapshot.getKey().toString().trim();

                            friendsList.add(user_id);

                        }

                    }

                    // Вызваем метод получения всех пользователей из базы джанных
                    getUsers(friendsList);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

    }


    // Метод получения всех пользователей, которые не в друзьях у текущего пользователя
    private void getUsers (ArrayList<String> friendsList) {

        Query query = database.getReference("users");

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot main_snapshot) {

                    if (main_snapshot.exists()) {

                        // Инициализируем список для записи пользователей в базе данных
                        ArrayList<User> usersList = new ArrayList<User>();


                        // Проходимся по дочерним элементам основного снимка записей из базы данных
                        for (DataSnapshot friendSnapshot : main_snapshot.getChildren()) {

                            //Если найденный пользователь имеет id текущего пользователя, то его пропустить
                            if (friendSnapshot.getKey().equals(currentUser)) {
                                continue;
                            }

                            // Если список друзей текущего пользователя не содержит ID найденного пользователя, то этого пользователя добавляют
                            if (!friendsList.contains(friendSnapshot.getKey().toString().trim())) {

                                // Записываем полученные данные в переменные
                                String uid_user = friendSnapshot.getKey().toString();
                                String name_user = friendSnapshot.child("user_data").child("login").getValue().toString();
                                String colvoaudio_user = friendSnapshot.child("user_information").child("colvo_audio").getValue().toString();
                                String colvo_friends = friendSnapshot.child("user_information").child("colvo_friends").getValue().toString();
                                String register_date = friendSnapshot.child("user_information").child("register_date").getValue().toString();

                                //Создаем объект пользователь с полученными данными
                                User user = new User(uid_user,name_user, colvoaudio_user, colvo_friends, register_date);

                                //Передаем объект пользователь в список пользователей
                                usersList.add(user);

                            }

                            // Передаем список с пользователями в адапетер Recycler View
                            userAdapter.setList(usersList);

                            // Уведомить адаптер об изменении данных
                            userAdapter.notifyDataSetChanged();

                        }
                    }
                }


                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getActivity(), "Ошибка: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

    }


}