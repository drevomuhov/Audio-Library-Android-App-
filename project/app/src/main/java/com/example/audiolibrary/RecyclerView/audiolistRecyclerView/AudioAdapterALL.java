package com.example.audiolibrary.RecyclerView.audiolistRecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.audiolibrary.R;
import com.example.audiolibrary.RecyclerView.playlistRecyclerView.Playlist;
import com.example.audiolibrary.RecyclerView.playlistRecyclerView.PlaylistAdapterSelect;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class AudioAdapterALL extends RecyclerView.Adapter<AudioViewHolder> implements PopupMenu.OnMenuItemClickListener {


    // Инициализация списков
    private ArrayList<Audio> current_audio_list = new ArrayList<>();
    private ArrayList<Audio> original_audio_list = new ArrayList<>();
    private ArrayList<Audio> filtered_audio_list = new ArrayList<>();


    // Подключение модулей Firebase
    private FirebaseAuth authentication = FirebaseAuth.getInstance();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();


    private View view;
    private int CurrentPosition;

    private Audio selectedAudio;
    private final String currentUser = authentication.getCurrentUser().getUid().toString();



    private final String type_player = "default_all";
    private final String user_mood = "default_all";

    private AudioAdapterPredict.OnItemClickListener listener;

    public void setOnItemClickListener(AudioAdapterPredict.OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, String type_player, String user_mood, ArrayList<Audio> list);
    }



    // Конструктор адаптера
    public AudioAdapterALL() {

    }


    // Метод установки списка аудиозаписей
    public void setList(ArrayList<Audio> audio_list) {

        // Устнавлвиаем полученный список аудиозаписей в несколько списков (текущий список и оригинальный (изначально полученный))
        current_audio_list = audio_list;

        original_audio_list = audio_list;

        // Обновляем отображение в RecyclerView
        notifyDataSetChanged();

    }


    // Метод обновления списка аудиозаписей в Recycler View (в данном случае при поиске по названию)
    private void updateList (ArrayList<Audio> audio_list) {

        // Обновляем текущий список аудиозаписей
        this.current_audio_list = audio_list;

        //Обновляем отображение в RecyclerView
        notifyDataSetChanged();

    }


    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.audio_item_all, parent, false);
        return new AudioViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, @SuppressLint("RecyclerView") int position) {


        holder.title_audio.setText(current_audio_list.get(position).title_audio);
        holder.performer_audio.setText(current_audio_list.get(position).performer_audio);


        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position, type_player, user_mood, current_audio_list);
                Toast.makeText(view.getContext(), "Позиция: " + position, Toast.LENGTH_SHORT).show();
            }
        });

        holder.popup_menu_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // Получение объекта текущей выбранно аудиозаписи
                selectedAudio = current_audio_list.get(holder.getAdapterPosition());

                if (currentUser .equals(selectedAudio.id_user)) {
                    showPopupMenu(view, position);
                } else {
                    showPopupMenu2(view, position);
                }

            }
        });
    }


    @Override
    public int getItemCount() {
        if (current_audio_list != null) {
            return current_audio_list.size();
        } else {
            return 0;
        }
    }


    // Метод отображения PopupMenu для аудиозаписи в RecyclerView
    private void showPopupMenu(View view, int position) {

        CurrentPosition = position;

        // Создаем объект PopupMenu
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);

        // Подключаем ресурс меню к объекту PopupMenu
        popupMenu.inflate(R.menu.audio_popup_menu);

        // Устанавливаем слушатель нажатий на элементы объекта PopupMenu
        popupMenu.setOnMenuItemClickListener(this);

        // Отображаем PopupMenu
        popupMenu.show();

    }


    // Метод обработки нажатий на элементы меню в PopupMenu
    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {

        if (menuItem.getItemId() == R.id.action_popup_edit) {

            editItemPopupMenu(view.getContext());

            return true;
        } else if (menuItem.getItemId() == R.id.action_popup_delete) {

            deleteItemPopupMenu(view.getContext());

            return true;
        } else if (menuItem.getItemId() == R.id.action_popup_addplaylist) {

            addinplaylistItemPopupMenu(view.getContext());

            return true;
        } else {
            return false;
        }
    }


    // Метод вызывается для редактирования названия и исполнителя аудиозаписи
    private void editItemPopupMenu(Context context) {

        String id_user = current_audio_list.get(CurrentPosition).id_user;
        String currentUser = authentication.getCurrentUser().getUid().toString();

        if (!id_user.equals(currentUser)) {
            Toast.makeText(context, "Вы не можете редактировать эту аудиозапись!", Toast.LENGTH_SHORT).show();
        } else {

            // Получаем информацию об аудиозаписи
            String id_audio = current_audio_list.get(CurrentPosition).id_audio;
            String title_audio = current_audio_list.get(CurrentPosition).title_audio;
            String performer_audio = current_audio_list.get(CurrentPosition).performer_audio;

            // Создаем объект AlertDialog.Builder
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

            // Устанавливаем заголовок диалога
            builder.setTitle("Редактировать аудиозапись");

            // Создаем View для диалогового окна
            View dialog_view = LayoutInflater.from(context).inflate(R.layout.edit_audio_dialog, null);

            // Устанавливаем View в диалоговое окно
            builder.setView(dialog_view);

            // Получаем ссылки на EditText
            EditText editText_title = dialog_view.findViewById(R.id.editText_title);
            EditText editText_performer = dialog_view.findViewById(R.id.editText_performer);

            // Задаем значение
            editText_title.setText(title_audio);
            editText_performer.setText(performer_audio);


            // Добавляем кнопки "Сохранить" и "Отмена"
            builder.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    // Получаем измененные значения из EditText
                    String changed_title = editText_title.getText().toString();
                    String changed_performer = editText_performer.getText().toString();

                    // Передаем эти значения в базу данных
                    Task<Void> query1 = database.getReference().child("users").child(authentication.getCurrentUser().getUid()).child("user_uploads").child("audio").child(id_audio).child("title_audio").setValue(changed_title);
                    Task<Void> query2 = database.getReference().child("users").child(authentication.getCurrentUser().getUid()).child("user_uploads").child("audio").child(id_audio).child("performer_audio").setValue(changed_performer);

                    Toast.makeText(context, "Аудиозапись изменена", Toast.LENGTH_SHORT).show();


                }

            });

            builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    // Отмена изменений
                    dialog.cancel();
                }
            });

            // Отображение диалогового окна
            AlertDialog dialog = builder.create();
            dialog.show();


        }

    }


    // Метод вызывается для удаления аудиозаписи
    private void deleteItemPopupMenu(Context context) {

        String id_user = current_audio_list.get(CurrentPosition).id_user;
        String currentUser = authentication.getCurrentUser().getUid().toString();

        if (!id_user.equals(currentUser)) {
            Toast.makeText(context, "Вы не можете удалить эту аудиозапись!", Toast.LENGTH_SHORT).show();
        } else {

            // Получаем информацию об ID аудиозаписи
            String id_audio = current_audio_list.get(CurrentPosition).id_audio;

            // Удаляем аудиозапись по ID из базы данных
            Task<Void> query1 = database.getReference().child("users").child(authentication.getCurrentUser().getUid()).child("user_uploads").child("audio").child(id_audio).removeValue();


            query1.addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                        Toast.makeText(context, "Аудиозапись удалена", Toast.LENGTH_SHORT).show();

                    } else {

                        Toast.makeText(context, "Ошибка удаления аудиозаписи!", Toast.LENGTH_SHORT).show();

                    }
                }
            });

        }

    }


    // Метод вызывается для добавления аудиозаписи в плейлист
    private void addinplaylistItemPopupMenu(Context context) {

        // Получаем объект выбранной аудиозаписи
        selectedAudio = current_audio_list.get(CurrentPosition);

        // Получаем ID объекта
        String ID_AUDIO = selectedAudio.id_audio.toString();

        PlaylistAdapterSelect playlistAdapterSelect = new PlaylistAdapterSelect();
        playlistAdapterSelect.getSelectedAudio(ID_AUDIO, selectedAudio, context);

        // Создаем диалоговое окно для выбора плейлиста
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setCancelable(true);
        View dialog_view = LayoutInflater.from(context).inflate(R.layout.select_playlist_dialog, null);
        builder.setView(dialog_view);

        // Получаем ссылки на элементы компоновки
        RecyclerView playlist_recyclerview_dialog = dialog_view.findViewById(R.id.playlist_recyclerview_dialog);
        playlist_recyclerview_dialog.setLayoutManager(new LinearLayoutManager(dialog_view.getContext(), LinearLayoutManager.HORIZONTAL, false));
        playlist_recyclerview_dialog.setAdapter(playlistAdapterSelect);

        // Инициализируем список для записи полученных плейлистов
        ArrayList<Playlist> playlist_list = new ArrayList<Playlist>();

        Query query = database.getReference("users").child(currentUser).child("user_uploads").child("playlist");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot main_snapshot) {

                if (main_snapshot.exists()) {

                    for (DataSnapshot playlistSnapshot : main_snapshot.getChildren()) {

                        // Записываем полученные данные в переменные
                        String uid_playlist = playlistSnapshot.getKey();
                        String title_playlist = playlistSnapshot.child("info_playlist").child("title_playlist").getValue().toString();
                        String colvoaudio_playlist = playlistSnapshot.child("info_playlist").child("colvoaudio_playlist").getValue().toString();

                        //Создаем объект плейлист с полученными данными
                        Playlist playlist = new Playlist(uid_playlist, title_playlist, colvoaudio_playlist);

                        //Передаем объект плейлист в список с плейлистами
                        playlist_list.add(playlist);

                    }

                    // Передаем список с плейлистами в адапетер Recycler View
                    playlistAdapterSelect.setList(playlist_list);

                    // Уведомить адаптер об изменении данных
                    playlistAdapterSelect.notifyDataSetChanged();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        // Передача созданного диалога в другой адаптер для закрытия после успешного добавления аудиозаписи в плейлист
        playlistAdapterSelect.getDialog(dialog, context);

        dialog.show();

    }



    // Метод отображения PopupMenu для аудиозаписи в RecyclerView
    private void showPopupMenu2(View view, int position) {

        CurrentPosition = position;

        // Создаем объект PopupMenu
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);

        // Подключаем ресурс меню к объекту PopupMenu
        popupMenu.inflate(R.menu.audio_popup_menu2);

        // Устанавливаем слушатель нажатий на элементы объекта PopupMenu
        popupMenu.setOnMenuItemClickListener(this::onMenuItemClick2);

        // Отображаем PopupMenu
        popupMenu.show();

    }


    // Метод обработки нажатий на элементы меню в PopupMenu

    public boolean onMenuItemClick2(MenuItem menuItem) {

        if (menuItem.getItemId() == R.id.action_popup_addaudio) {

            addAudio(view.getContext());

            return true;

        } else if (menuItem.getItemId() == R.id.action_popup_addplaylist) {

            addinplaylistItemPopupMenu(view.getContext());

            return true;

        } else {

            return false;
        }
    }


    private void addAudio (Context context) {

        // Получаем информацию об аудиозаписи
        String id_audio = current_audio_list.get(CurrentPosition).id_audio;
        String id_user = current_audio_list.get(CurrentPosition).id_user;
        String title_audio = current_audio_list.get(CurrentPosition).title_audio;
        String performer_audio = current_audio_list.get(CurrentPosition).performer_audio;
        String url_audio = current_audio_list.get(CurrentPosition).url_audio;

        int mood_happy = current_audio_list.get(CurrentPosition).mood_happy;
        int mood_sad = current_audio_list.get(CurrentPosition).mood_sad;
        int mood_normal = current_audio_list.get(CurrentPosition).mood_normal;
        int mood_angry = current_audio_list.get(CurrentPosition).mood_angry;

        long timestamp = current_audio_list.get(CurrentPosition).timestamp;

        // Создаем объект Аудиозапись для добавления в плелйист с ID полученным из переменной ID_PLAYLIST[0]
        HashMap<String, Object> audio_file = new HashMap<>();
        audio_file.put("id_audio", id_audio);
        audio_file.put("id_user", id_user);
        audio_file.put("title_audio", title_audio);
        audio_file.put("performer_audio", performer_audio);
        audio_file.put("url_audio", url_audio);
        audio_file.put("mood_happy", mood_happy);
        audio_file.put("mood_sad", mood_sad);
        audio_file.put("mood_normal", mood_normal);
        audio_file.put("mood_angry", mood_angry);
        audio_file.put("timestamp", timestamp);


        String currentUser = authentication.getCurrentUser().getUid().toString();

        Task<Void> query = database.getReference("users").child(currentUser).child("user_uploads").child("audio").child(id_audio).setValue(audio_file);

        query.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                   Task<Void> query_colvo_update = database.getReference("users").child(currentUser).child("user_information").child("colvo_audio").getRef().setValue(ServerValue.increment(1));
                   query_colvo_update.addOnCompleteListener(new OnCompleteListener<Void>() {
                       @Override
                       public void onComplete(@NonNull Task<Void> task) {
                           if (task.isSuccessful()) {
                               Toast.makeText(context, "Аудиозапись " + title_audio + " добавлена", Toast.LENGTH_SHORT).show();
                           } else {
                               Toast.makeText(context, "Не удалось добавить аудиозапись!", Toast.LENGTH_SHORT).show();
                           }
                       }
                   });
                }
            }
        });

    }



    // Метод получения запроса от поля поиска
    public void setQuery (String query) {

        searchAudio(original_audio_list, filtered_audio_list, query);

    }


    // Метод фильтрации списка original по запросу пользователя
    private void searchAudio(ArrayList<Audio> original_audio_list, ArrayList<Audio> filtered_audio_list, String query) {

        // Приводим запрос к нижнему регистру для поиска без учета регистра
        String queryLowerCase = query.toLowerCase();

        if (filtered_audio_list != null) {
            filtered_audio_list.clear();
        }

        // Фильтруем список аудио по запросу
        for (Audio audio : original_audio_list) {
            if (audio.getTitle_audio().toLowerCase().contains(queryLowerCase)) {
               filtered_audio_list.add(audio);
            }
        }

        if (filtered_audio_list.isEmpty()) {

            updateList(original_audio_list);

        } else {

            // Устанавливаем отфильтрованный список
            updateList(filtered_audio_list);

        }
    }

}

