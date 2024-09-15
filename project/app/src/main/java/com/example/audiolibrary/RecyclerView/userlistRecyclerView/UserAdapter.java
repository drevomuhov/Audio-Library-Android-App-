package com.example.audiolibrary.RecyclerView.userlistRecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.audiolibrary.Navigation.screens.UserPage;
import com.example.audiolibrary.R;
import com.example.audiolibrary.RecyclerView.friendlistRecyclerView.FriendAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserAdapter extends RecyclerView.Adapter<UserViewHolder> {


    // Инициализация списков
    private ArrayList<User> current_user_list = new ArrayList<>();
    private ArrayList<User> original_user_list = new ArrayList<>();
    private ArrayList<User> filtered_user_list = new ArrayList<>();


    Set<String> addedFriendsIds = new HashSet<>();

    
    FriendAdapter friendAdapter = new FriendAdapter();


    // Подключение модулей Firebase
    private FirebaseAuth authentication = FirebaseAuth.getInstance();
    private final String currentUser = authentication.getCurrentUser().getUid().toString();

    private FirebaseDatabase database = FirebaseDatabase.getInstance();


    ArrayList<String> user_audio_list = new ArrayList<>();
    ArrayList<String> current_user_audio_list = new ArrayList<>();



    // Конструктор адаптера
    public UserAdapter() {

    }




    // Метод установки списка пользвоателей в Recycler View с помощью этого адаптера
    public void setList(ArrayList<User> user_list) {

        // Устнавлвиаем полученный список аудиозаписей в несколько списков (текущий список и оригинальный (изначально полученный))
        current_user_list = user_list;

        original_user_list = user_list;

        // Обновляем отображение в RecyclerView
        notifyDataSetChanged();

    }


    // Метод обновления списка пользователей в Recycler View (в данном случае при поиске по названию)
    private void updateList (ArrayList<User> user_list) {

        // Обновляем текущий список аудиозаписей
        this.current_user_list = user_list;

        //Обновляем отображение в RecyclerView
        notifyDataSetChanged();

    }


    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item_rv, parent, false);
        return new UserViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Context context = holder.itemView.getContext();

        // Внутри onBindViewHolder
        FirebaseDatabase.getInstance().getReference().child("users").child(current_user_list.get(position).uid_user).child("user_data").child("profile_image")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String imageUrl = dataSnapshot.getValue(String.class);
                            Glide.with(context)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_profile) // Заглушка на время загрузки
                                    .into(holder.profile_image); // Установка изображения
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("DBError", "Ошибка при загрузке изображения", databaseError.toException());
                    }
                });


        // Используем локальную переменную для хранения списка ID аудиозаписей пользователя
        // Используем локальную переменную для хранения списка ID аудиозаписей пользователя
        List<String> localUserAudioList = new ArrayList<>();

        // Получаем список ID аудиозаписей пользователя
        Query query = database.getReference("users").child(current_user_list.get(position).uid_user).child("user_uploads").child("audio");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot main_snapshot) {
                if (main_snapshot.exists()) {
                    for (DataSnapshot audioSnapshot : main_snapshot.getChildren()) {
                        String id_audio = audioSnapshot.getKey();
                        localUserAudioList.add(id_audio);
                    }
                    // После получения списка ID аудиозаписей пользователя, получаем список ID аудиозаписей текущего пользователя
                    Query query1 = database.getReference("users").child(authentication.getCurrentUser().getUid()).child("user_uploads").child("audio");
                    query1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot currentUserSnapshot) {
                            if (currentUserSnapshot.exists()) {
                                List<String> localCurrentUserAudioList = new ArrayList<>();
                                for (DataSnapshot audioSnapshot : currentUserSnapshot.getChildren()) {
                                    String id_audio = audioSnapshot.getKey();
                                    localCurrentUserAudioList.add(id_audio);
                                }
                                // Расчет совпадений
                                int matches = 0;
                                for (String id : localCurrentUserAudioList) {
                                    if (localUserAudioList.contains(id)) {
                                        matches++;
                                    }
                                }
                                // Вычисляем процент совпадения на основе количества аудиозаписей текущего пользователя
                                double matchPercent = (double) matches / localCurrentUserAudioList.size() * 100;
                                String message = "Музыкальное совпадение: " + String.format("%.1f", matchPercent) + "%";
                                holder.userBlock_colvo_audio_info.setText(message);
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

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, UserPage.class);

                intent.putExtra("user_id", current_user_list.get(position).uid_user);

                intent.putExtra("user_name", current_user_list.get(position).name_user);

                intent.putExtra("user_audio", current_user_list.get(position).colvoaudio_user);
                intent.putExtra("user_friend", current_user_list.get(position).colvo_friends);
                intent.putExtra("user_reg_date", current_user_list.get(position).register_date);

                context.startActivity(intent);

            }
        });

        //holder.profile_image.setImageResource(R.drawable.ic_profile);

        holder.userBlock_name_user.setText(current_user_list.get(position).name_user);

        holder.userBlock_add_friend_button.setOnClickListener(view -> {

            int selected_position = holder.getAdapterPosition();

            if (!addedFriendsIds.contains(current_user_list.get(position).getUid_user())) {
                addFriend(selected_position, view, holder);
            } else {
                String message = String.format("Вы уже подписаны на %s", current_user_list.get(position).getName_user());
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }

        });

    }


    @Override
    public int getItemCount() {
        if (current_user_list != null) {
            return current_user_list.size();
        } else {
            return 0;
        }
    }


    // Метод добавления пользователя в друзья
    private void addFriend(int position, View view, UserViewHolder holder) {

        // Получаем UID пользователя, на которого подписываемся
        String user_id = current_user_list.get(position).getUid_user();

        // Создаем объект друга с полученной информацией для передачи в базу данных
        HashMap<String, Object> friendUpdates = new HashMap<>();
        friendUpdates.put("uid_user", user_id);
        friendUpdates.put("name_user", current_user_list.get(position).getName_user());
        friendUpdates.put("colvoaudio_user", current_user_list.get(position).getColvoaudio_user());
        friendUpdates.put("colvofriend_user", current_user_list.get(position).getColvo_friends());
        friendUpdates.put("reg_date_user", current_user_list.get(position).getRegister_date());

        // Подготавливаем обновления для базы данных
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("/users/" + currentUser + "/user_friends/" + user_id, friendUpdates);
        updates.put("/users/" + currentUser + "/user_information/colvo_friends", ServerValue.increment(1));

        // Выполняем обновление одним запросом
        database.getReference().updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Добавляем id пользователя в список добавленных друзей
                addedFriendsIds.add(user_id);
                // Устнавливаем значок галочка
                holder.userBlock_add_friend_button.setBackgroundResource(R.drawable.ic_ok);

                String message = String.format("Вы подписались на %s", current_user_list.get(position).getName_user());
                Toast.makeText(view.getContext(), message, Toast.LENGTH_SHORT).show();

            } else {

                String message = String.format("Не удалось подписаться на %s" + current_user_list.get(position).getName_user());
                Toast.makeText(view.getContext(), message, Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(e -> Log.e("Ошибка подписки на пользователя", e.getMessage()));

    }




    // Метод получения запроса от поля поиска
    public void setQuery (String query) {

        searchAudio(original_user_list, filtered_user_list, query);

    }


    // Метод фильтрации original списка по запросу пользователя
    private void searchAudio(ArrayList<User> original_user_list, ArrayList<User> filtered_user_list, String query) {

        // Приводим запрос к нижнему регистру для поиска без учета регистра
        String queryLowerCase = query.toLowerCase();

        if (filtered_user_list != null) {
            filtered_user_list.clear();
        }

        // Фильтруем список аудио по запросу
        for (User user : original_user_list) {
            if (user.getName_user().toLowerCase().contains(queryLowerCase)) {
                filtered_user_list.add(user);
            }
        }

        if (filtered_user_list.isEmpty()) {

            updateList(original_user_list);

        } else {

            // Устанавливаем отфильтрованный список
            updateList(filtered_user_list);

        }
    }
}
