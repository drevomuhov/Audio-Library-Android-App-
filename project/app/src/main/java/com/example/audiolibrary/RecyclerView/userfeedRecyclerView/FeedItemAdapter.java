package com.example.audiolibrary.RecyclerView.userfeedRecyclerView;


import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.audiolibrary.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class FeedItemAdapter extends RecyclerView.Adapter<FeedItemViewHolder> {

    // Переменные для хранения принимаемых данных в адаптер

    private ArrayList <FeedItem> list;


    // Подключение модулей Firebase
    private FirebaseAuth authentication = FirebaseAuth.getInstance();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();


    private String feed_user_id;

    private View view;

    // Конструктор адаптера
    public FeedItemAdapter() {

    }

    public void setFeedUserID (String feed_user_id) {
        this.feed_user_id = feed_user_id;
    }


    // Функция установки array списка аудиозаписей в адаптер
    public void setList (ArrayList <FeedItem> list) {
        //Текущий список list адаптера заменяется на список полученный адаптером из активити (фрагмента) с помощью этой функции
        this.list = list;
        //Обновление отображения в RecyclerView
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public FeedItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_item, parent, false);
        return new FeedItemViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull FeedItemViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Context context = holder.itemView.getContext();

        if (authentication.getCurrentUser().getUid() != feed_user_id) {

            holder.delete_item_feed.setVisibility(View.GONE);

        }


        holder.item_string.setText(list.get(position).string_item);

        holder.delete_item_feed.setOnClickListener(view -> {
            // Получаем позицию и ID элемента в адаптере
            int position1 = holder.getAdapterPosition();
            String id_item_feed = list.get(position1).id_item;

            deleteItemFeed(context, position1, id_item_feed);
        });


    }

    private void deleteItemFeed(Context context, int position, String id_item_feed) {
        if (position >= 0 && position < list.size()) {
            // Проверяем, что позиция в допустимом диапазоне
            Task<Void> query = database.getReference("users")
                    .child(authentication.getCurrentUser().getUid())
                    .child("user_feed")
                    .child(id_item_feed)
                    .removeValue();

            query.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String message = "Запись удалена";
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                } else {
                    String message = "Ошибка удаления. Попробуйте позже";
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> Log.e("Error ItemFeed Delete", e.getMessage()));
        } else {
            Log.e("Error ItemFeed Delete", "Недопустимая позиция: " + position);
        }
    }




    @Override
    public int getItemCount() {
        if (list != null) {
            return list.size();
        } else {
            return 0;
        }
    }

}
