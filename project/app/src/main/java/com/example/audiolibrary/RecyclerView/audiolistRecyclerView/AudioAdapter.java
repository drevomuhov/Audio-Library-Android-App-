package com.example.audiolibrary.RecyclerView.audiolistRecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
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
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class AudioAdapter extends RecyclerView.Adapter<AudioViewHolder> implements PopupMenu.OnMenuItemClickListener {


    // Инициализация списков
    private ArrayList<Audio> current_audio_list = new ArrayList<>();
    private ArrayList<Audio> original_audio_list = new ArrayList<>();
    private ArrayList<Audio> filtered_audio_list = new ArrayList<>();


    // Подключение модулей Firebase
    private FirebaseAuth authentication = FirebaseAuth.getInstance();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();


    private View view;
    private int CurrentPosition;

    private Audio selectedAudio;
    private final String currentUser = authentication.getCurrentUser().getUid().toString();

    private final String type_player = "default";
    private final String user_mood = "default";


    // Конструктор адаптера
    public AudioAdapter() {

    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, String type_player, String user_mood, ArrayList<Audio> audioArrayList);
    }


    // Метод установки списка аудиозаписей
    public void setList(ArrayList<Audio> audio_list) {

        // Устнавлвиаем полученный список аудиозаписей в несколько списков (текущий список и оригинальный (изначально полученный))
        this.current_audio_list = audio_list;
        this.original_audio_list = audio_list;

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
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.audio_item, parent, false);
        return new AudioViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position, type_player, user_mood, current_audio_list);
            }
        });

        //holder.itemView.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View view) {
        //
                // Получаем контекст активности из view
        //        Context context = view.getContext();

        //        BottomSheetPlayer bottomSheetPlayer = BottomSheetPlayer.newInstance(position, current_audio_list);

                // Проверяем, является ли контекст активностью
        //        if (context instanceof AppCompatActivity) {

                    // Показываем Bottom Sheet, передавая активность
        //            bottomSheetPlayer.showBottomSheet((AppCompatActivity) context);

        //        }

        //    }

        //});




        holder.title_audio.setText(current_audio_list.get(position).title_audio);
        holder.performer_audio.setText(current_audio_list.get(position).performer_audio);


        holder.popup_menu_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Получение объекта текущей выбранно аудиозаписи
                selectedAudio = current_audio_list.get(holder.getAdapterPosition());

                if (currentUser.equals(selectedAudio.id_user)) {

                    showPopupMenu(view, position);
                } else {
                    showPopupMenu3(view, position);
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




    private void showPopupMenu3(View view, int position) {

        CurrentPosition = position;

        // Создаем объект PopupMenu
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);

        // Подключаем ресурс меню к объекту PopupMenu
        popupMenu.inflate(R.menu.audio_popup_menu3);

        // Устанавливаем слушатель нажатий на элементы объекта PopupMenu
        popupMenu.setOnMenuItemClickListener(this::onMenuItemClick3);

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

            deleteAudio_full(view.getContext());

            return true;
        } else if (menuItem.getItemId() == R.id.action_popup_addplaylist) {

            addinplaylistItemPopupMenu(view.getContext());

            return true;
        } else {
            return false;
        }
    }
    public boolean onMenuItemClick3(MenuItem menuItem) {

        if (menuItem.getItemId() == R.id.action_popup_delete2) {

            deleteAudio_personal(view.getContext());

            return true;

        } else if (menuItem.getItemId() == R.id.action_popup_addplaylist) {

            addinplaylistItemPopupMenu(view.getContext());

            return true;
        } else {
            return false;
        }
    }




    // Метод вызывается для редактирования названия и исполнителя аудиозаписи (может только владелец)
    private void editItemPopupMenu(Context context) {

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

                // Get the changed values from EditText
                String changed_title = editText_title.getText().toString();
                String changed_performer = editText_performer.getText().toString();

                Query query = database.getReference("users");
                query.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        // Перебираем всех пользователей
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {

                            // Перебираем все аудиозаписи пользователя
                            DataSnapshot userUploadsSnapshot = userSnapshot.child("user_uploads").child("audio");

                            for (DataSnapshot audioSnapshot : userUploadsSnapshot.getChildren()) {

                                // Проверяем, совпадает ли id аудиозаписи
                                if (audioSnapshot.getKey().equals(id_audio)) {

                                    // Обновляем название и исполнителя аудиозаписи
                                    audioSnapshot.getRef().child("title_audio").setValue(changed_title);
                                    audioSnapshot.getRef().child("performer_audio").setValue(changed_performer);

                                }
                            }
                        }

                        // Обновляем также в разделе для Emotion Playlist
                        Task<Void> query1 = database.getReference("uploads").child("audio").child(id_audio).child("title_audio").setValue(changed_title);
                        Task<Void> query2 = database.getReference("uploads").child("audio").child(id_audio).child("performer_audio").setValue(changed_performer);

                        Toast.makeText(context, "Название и исполнитель изменены для всех аудиозаписей", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.w("TAG", "Database error: " + databaseError.getMessage());
                    }
                });


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


    // Метод вызывается для добавления аудиозаписи в плейлист (могут все)
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


    // Метод вызывается для полного удаления аудиозаписи (может только владелец)


    private void deleteAudio_full(Context context) {

        Log.d("Удаление Audio FULL", "Запущен процесс удаления аудиозаписи");

        String currentUser = authentication.getCurrentUser().getUid().toString();
        String id_audio = current_audio_list.get(CurrentPosition).id_audio;
        String title_audio = current_audio_list.get(CurrentPosition).title_audio;


        // Удаление из базы данных ветки для Emotional Playlist
        Task<Void> query_deleteFromUploads = database.getReference("uploads").child("audio").child(id_audio).removeValue();
        query_deleteFromUploads.addOnCompleteListener(task -> {
            Log.d("Удаление Audio FULL", "Аудиозапись успешно удалена из Uploads");
        }).addOnFailureListener(e -> {
            Log.e("Удаление Audio FULL", "Аудиозапись не удалена из Uploads");
        });


        // Удаление из базы данных у всех пользователей, которые добавляли эту аудиозапись себе, а также у того, кто загружал
        Query deleteFromAllAudio = database.getReference("users");
        deleteFromAllAudio.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot main_snapshot) {

                for (DataSnapshot userSnapshot : main_snapshot.getChildren()) {

                    // Перебираем все аудиозаписи пользователя
                    DataSnapshot uploadsSnapshot = userSnapshot.child("user_uploads/audio");

                    for (DataSnapshot audioSnapshot : uploadsSnapshot.getChildren()) {

                        // Проверяем, совпадает ли id аудиозаписи
                        if (audioSnapshot.getKey().equals(id_audio)) {

                            // Удаляем аудиозапись
                            audioSnapshot.getRef().removeValue();
                            Log.d("Удаление Audio FULL", "Аудиозапись успешно удалена из Audio всех Users");
                            // Уменьшаем значение colvo_audio на -1
                            userSnapshot.child("user_information").child("colvo_audio").getRef().setValue(ServerValue.increment(-1));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("TAG", "onCancelled", error.toException());
            }
        });

        Query deleteFromAllPlaylists = database.getReference("users");
        deleteFromAllPlaylists.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot main_snapshot) {
                for (DataSnapshot userSnapshot : main_snapshot.getChildren()) {
                    // Перебираем все аудиозаписи пользователя
                    DataSnapshot uploadsSnapshot = userSnapshot.child("user_uploads/playlist");
                    for (DataSnapshot audioSnapshot : uploadsSnapshot.getChildren()) {
                        for (DataSnapshot snapshot : audioSnapshot.getChildren()) {
                            if (snapshot.getKey().equals("audio_playlist")) {
                                for (DataSnapshot snapshot2 : snapshot.getChildren()) {
                                    if (snapshot2.exists()) {
                                        if (snapshot2.getKey().equals(id_audio)) {
                                            snapshot2.getRef().removeValue().addOnSuccessListener(unused -> {
                                                for (DataSnapshot userSnapshot1 : main_snapshot.getChildren()) {
                                                    DataSnapshot uploadsSnapshot1 = userSnapshot1.child("user_uploads/playlist");
                                                    for (DataSnapshot audioSnapshot1 : uploadsSnapshot1.getChildren()) {
                                                        for (DataSnapshot snapshot1 : audioSnapshot1.getChildren()) {
                                                            if (snapshot1.getKey().equals("info_playlist")) {
                                                                snapshot1.getRef().child("colvoaudio_playlist").setValue(ServerValue.increment(-1));
                                                                Log.d("Удаление Audio FULL", "Аудиозапись успешно удалена из Playlist всех Users");
                                                            }
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("TAG", "onCancelled", error.toException());
            }
        });


        // Удаление аудиозаписи из Firebase Storage
        Task<Void> query_deleteFromStorage = storage.getReference("uploads").child("audio_files").child(currentUser).child(id_audio).delete();
        query_deleteFromStorage.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("Удаление Audio FULL", "Аудиозапись успешно удалена из Storage");
                Toast.makeText(context, "Аудиозапись " + title_audio + " удалена из аудиозаписей", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Ошибка удаления аудиозаписи!", Toast.LENGTH_SHORT).show();
            }

        });

    }
    // Метод вызывается для неполного удаления аудиоазписи (только из его списка аудиоазаписей) (может только не владелец)
    private void deleteAudio_personal (Context context) {

        String currentUser = authentication.getCurrentUser().getUid().toString();
        String id_audio = current_audio_list.get(CurrentPosition).id_audio;
        String title_audio = current_audio_list.get(CurrentPosition).title_audio;


        Task<Void> query = database.getReference("users").child(currentUser).child("user_uploads").child("audio").child(id_audio).removeValue();
        query.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    Task<Void> query_colvo_upgrade = database.getReference("users").child(currentUser).child("user_information").child("colvo_audio").setValue(ServerValue.increment(-1));

                    query_colvo_upgrade.addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Аудиозапись " + title_audio + " удалена из аудиозаписей", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Ошибка удаления аудиозаписи!", Toast.LENGTH_SHORT).show();
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

