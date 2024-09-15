package com.example.audiolibrary.RecyclerView.userpageRecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.audiolibrary.R;

import java.util.ArrayList;

public class UserPageAdapter extends RecyclerView.Adapter<UserPageAdapterViewHolder>  {


    ArrayList<UserInfo> userInfoList = new ArrayList();


    // Функция установки array списка аудиозаписей в адаптер
    public void setList (ArrayList<UserInfo>  userInfo_list) {

        //Текущий список list адаптера заменяется на список полученный адаптером из активити (фрагмента) с помощью этой функции
        this.userInfoList = userInfo_list;
        //Обновление отображения в RecyclerView
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserPageAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.userinfo_item, parent, false);
        return new UserPageAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserPageAdapterViewHolder holder, int position) {

        UserInfo userInfo = userInfoList.get(position);

        holder.title_key.setText(userInfo.getKey());
        holder.title_value.setText(userInfo.getValue());

    }

    @Override
    public int getItemCount() {
        if (userInfoList != null) {
            return userInfoList.size();
        } else {
            return 0;
        }
    }
}
