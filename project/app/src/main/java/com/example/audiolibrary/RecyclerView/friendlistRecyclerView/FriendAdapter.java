package com.example.audiolibrary.RecyclerView.friendlistRecyclerView;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FriendAdapter extends RecyclerView.Adapter<FriendViewHolder> {


    // Инициализация списков
    private ArrayList<Friend> current_friend_list = new ArrayList<>();
    private ArrayList<Friend> original_friend_list = new ArrayList<>();
    private ArrayList<Friend> filtered_friend_list = new ArrayList<>();



    ArrayList<String> user_audio_list = new ArrayList<>();
    ArrayList<String> current_user_audio_list = new ArrayList<>();



    // Подключение модулей Firebase
    private FirebaseAuth authentication = FirebaseAuth.getInstance();
    private final String currentUser = authentication.getCurrentUser().getUid().toString();


    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    View view;
    Context context;

    private String image_url;



    // Список удаленных друзей
    Set<String> deletedFriendsIds = new HashSet<>();
    boolean status_delete = false;


    // Конструктор адаптера
    public FriendAdapter() {

    }


    // Метод установки списка друзей в Recycler View с помощью этого адаптера
    public void setList(ArrayList<Friend> friend_list) {

        // Устнавлвиаем полученный список друзей в несколько списков (текущий список и оригинальный (изначально полученный))
        current_friend_list = friend_list;

        original_friend_list = friend_list;

        // Обновляем отображение в RecyclerView
        notifyDataSetChanged();

    }


    // Метод обновления списка друзей в Recycler View (в данном случае при поиске по названию)
    private void updateList (ArrayList<Friend> friend_list) {

        // Обновляем текущий список аудиозаписей
        this.current_friend_list = friend_list;

        //Обновляем отображение в RecyclerView
        notifyDataSetChanged();

    }


    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_item_rv, parent, false);
        return new FriendViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, @SuppressLint("RecyclerView") int position) {

        context = holder.itemView.getContext();

        // Внутри onBindViewHolder
        FirebaseDatabase.getInstance().getReference().child("users").child(current_friend_list.get(position).uid_user).child("user_data").child("profile_image")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String imageUrl = dataSnapshot.getValue(String.class);
                            Glide.with(context)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_profile) // Заглушка на время загрузки
                                    .into(holder.profile_image); // Установка изображения
                        } else {
                            // Узел profile_image не найден, устанавливаем изображение по умолчанию
                            holder.profile_image.setImageResource(R.drawable.ic_profile);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("DBError", "Ошибка при загрузке изображения", databaseError.toException());
                    }
                });



        // Используем локальную переменную для хранения списка ID аудиозаписей пользователя
        List<String> localUserAudioList = new ArrayList<>();

        // Получаем список ID аудиозаписей пользователя
        Query query = database.getReference("users").child(current_friend_list.get(position).uid_user).child("user_uploads").child("audio");
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
                                holder.colvoaudio_friend_rv.setText(message);
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




        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, UserPage.class);

            intent.putExtra("user_id", current_friend_list.get(position).uid_user);
            intent.putExtra("user_name", current_friend_list.get(position).name_user);
            intent.putExtra("user_audio", current_friend_list.get(position).colvoaudio_user);
            intent.putExtra("user_friend", current_friend_list.get(position).colvofriend_user);
            intent.putExtra("user_reg_date", current_friend_list.get(position).reg_date_user);

            context.startActivity(intent);

        });


        holder.name_friend_rv.setText(current_friend_list.get(position).name_user);

        holder.deletefriend_friend_rv.setOnClickListener(view -> {

            int currentPosition = holder.getAdapterPosition();

            Friend selectedFriend = current_friend_list.get(position);

            String id_friend = selectedFriend.uid_user;
            String name_friend = selectedFriend.name_user;


            if (!deletedFriendsIds.contains(id_friend)) {

                //if(currentPosition < current_friend_list.size()) {

                // Добавляем ID в набор, чтобы избежать повторной оценки
                deletedFriendsIds.add(id_friend);
                status_delete = true;

                deleteFriend(view, id_friend, name_friend);

                // Удаляем элемент из списка друзей и уведомляем адаптер

                if(currentPosition < current_friend_list.size()) {
                    current_friend_list.remove(currentPosition);
                    notifyItemRemoved(currentPosition);
                }


            } else {

                Toast.makeText(context, "Вы уже удалили пользвоателя из друзей!", Toast.LENGTH_SHORT).show();

            }


        });
    }


    @Override
    public int getItemCount() {
        if (current_friend_list != null) {
            return current_friend_list.size();
        } else {
            return 0;
        }
    }




    // Метод удаления пользователя из друзей
    private void deleteFriend (View view, String uid_user, String name_user) {

        //Добавляем созданный объект в базу данных в раздел user_friends текущего пользователя
        Task<Void> query = database.getReference("users").child(currentUser).child("user_friends").child(uid_user).removeValue();
        query.addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                        //Уменьшаем значение кол-ва друзей пользователя на один
                        Task<Void> query = database.getReference("users").child(currentUser).child("user_information").child("colvo_friends").setValue(ServerValue.increment(-1));
                        query.addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                Toast.makeText(view.getContext(), "Вы отписались от " + name_user, Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(view.getContext(), "Ошибка уменьшения количества друзей!", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {

                        Toast.makeText(view.getContext(), name_user + " не удалось удалить из друзей", Toast.LENGTH_SHORT).show();

                    }
                }
            });

    }


    // Метод получения запроса от поля поиска
    public void setQuery (String query) {

        searchAudio(original_friend_list, filtered_friend_list, query);

    }


    // Метод фильтрации original списка по запросу пользователя
    private void searchAudio(ArrayList<Friend> original_friend_list, ArrayList<Friend> filtered_friend_list, String query) {

        // Приводим запрос к нижнему регистру для поиска без учета регистра
        String queryLowerCase = query.toLowerCase();

        if (filtered_friend_list != null) {
            filtered_friend_list.clear();
        }

        // Фильтруем список аудио по запросу
        for (Friend friend : original_friend_list) {
            if (friend.getName_user().toLowerCase().contains(queryLowerCase)) {
                filtered_friend_list.add(friend);
            }
        }

        if (filtered_friend_list.isEmpty()) {

            updateList(original_friend_list);

        } else {

            // Устанавливаем отфильтрованный список
            updateList(filtered_friend_list);

        }
    }

}
