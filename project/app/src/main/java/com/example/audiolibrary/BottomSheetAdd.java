package com.example.audiolibrary;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.audiolibrary.RecyclerView.audiolistRecyclerView.Audio;
import com.example.audiolibrary.RecyclerView.audiolistRecyclerView.AudioAdapterALL;
import com.example.audiolibrary.RecyclerView.audiolistRecyclerView.AudioAdapterPlaylist;
import com.example.audiolibrary.ViewPager1.BottomSheetPlayerPredict;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BottomSheetAdd extends BottomSheetDialogFragment {

    private LinearLayout selectBlock, addAudio_block, addPlaylist_block, infoAudio_block;

    private TextView selectBlock_title, addAudio_block_select_audio_button, infoAudio_block_text, addPlaylist_block_title_field;

    private EditText addAudio_block_title_field, addAudio_block_performer_field;


    // Блок поиска аудиозаписей
    private SearchView search_view;
    private RecyclerView recycler_view;
    private ArrayList<Audio> audio_list = new ArrayList<Audio>();
    private AudioAdapterALL audioAdapterALL = new AudioAdapterALL();


    private Button selectBlock_add_audio_button, selectBlock_add_playlist_button, addAudio_block_upload_audio_button, addPlaylist_block_create_button;


    private AudioAdapterPlaylist audioAdapterPlaylist = new AudioAdapterPlaylist();

    private FirebaseAuth authentication = FirebaseAuth.getInstance();
    private final String currentUser = authentication.getCurrentUser().toString();

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private AudioAdapterALL audioAdapterAll = new AudioAdapterALL();


    // Загрузка файла
    private Uri file_url = null;
    boolean file_is_selected = false;

    private View view;

    public interface OnFragmentSendDataListener {
        void onSendData(int position, String type_player, ArrayList<Audio> audioArrayList);
    }
    private BottomSheetAdd.OnFragmentSendDataListener fragmentSendDataListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            fragmentSendDataListener = (BottomSheetAdd.OnFragmentSendDataListener) context;
        } catch (ClassCastException e) {

        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Получаем ссылку на разметку фрагмента
        view = inflater.inflate(R.layout.bs_add, container, false);

        // Возвращаем разметку фрагмента
        return view;

    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        selectBlock.setVisibility(View.VISIBLE);
        addAudio_block.setVisibility(View.GONE);
        addPlaylist_block.setVisibility(View.GONE);

        infoAudio_block.setVisibility(View.GONE);


        // Отправка выбранного трека в блок с плеером
        audioAdapterAll.setOnItemClickListener((position, type_player, user_mood, audioArrayList) -> {

            openPlayerPredict(position, type_player, user_mood, audioArrayList);

            Toast.makeText(getActivity(), "Получено: " + position, Toast.LENGTH_SHORT).show();
        });



        // Блок выбора действия

        selectBlock_add_audio_button.setOnClickListener(view13 -> {

            selectBlock.setVisibility(View.GONE);
            selectBlock_title.setVisibility(View.GONE);

            addAudio_block.setVisibility(View.VISIBLE);

        });

        selectBlock_add_playlist_button.setOnClickListener(view12 -> {

            selectBlock.setVisibility(View.GONE);
            selectBlock_title.setVisibility(View.GONE);

            addPlaylist_block.setVisibility(View.VISIBLE);

        });


        // БЛОК ДОБАВЛЕНИЯ АУДИОЗАПИСИ

        // Кнопка "Выбрать аудио"
        addAudio_block_select_audio_button.setOnClickListener(view1 -> {

            if (!file_is_selected) {
                chooseAudio();

            } else {
                Toast.makeText(getActivity(), "Файл уже выбран!", Toast.LENGTH_SHORT).show();
            }
        });

        // Кнопка "Опубликовать"
        addAudio_block_upload_audio_button.setOnClickListener(view1 -> {

            String title_field = addAudio_block_title_field.getText().toString().trim();
            String performer_field = addAudio_block_performer_field.getText().toString().trim();

            if (file_url == null) {
                Toast.makeText(getContext(), "Файл не выбран!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (checkTitle(title_field) && checkPerformer(performer_field)) {
                uploadAudio(file_url);
            }

        });


        // БЛОК СОЗДАНИЯ ПЛЕЙЛИСТА

        addPlaylist_block_create_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createPlaylist();
            }
        });


        // БЛОК ПОИСКА АУДИОЗАПИСЕЙ
        search_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_view.setIconified(false);
                loadAudiolist();
                selectBlock_add_audio_button.setVisibility(View.GONE);
                selectBlock_add_playlist_button.setVisibility(View.GONE);
            }
        });
        search_view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                audioAdapterALL.setQuery(query);
                return false;
            }
        });

    }


    // БЛОК ДОБАВЛЕНИЯ АУДИОЗАПИСИ

    private boolean checkTitle(String title_field) {

        if (title_field.isEmpty()) {
            addAudio_block_title_field.setBackgroundResource(R.drawable.edittext_error);
            Toast.makeText(getActivity(), "Название не может быть пустым!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (title_field.length() > 20) {
            addAudio_block_title_field.setBackgroundResource(R.drawable.edittext_error);
            Toast.makeText(getActivity(), "Название не может быть длиннее 20 символов!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!title_field.matches("^[a-zA-Z0-9 ]+$")) {
            addAudio_block_performer_field.setBackgroundResource(R.drawable.edittext_error);
            Toast.makeText(getActivity(), "Исполнитель может содержать только буквы, цифры!", Toast.LENGTH_SHORT).show();
            return false;
        }

        List<String> forbiddenWords = Arrays.asList("хуй", "xuy", "huy", "хуйня", "huynya", "еблан", "лох", "lox", "пидр", "pidr", "пидор", "pidor",
                                                    "eblan", "пизда", "pizda", "хуеосос", "huesos", "сперма", "sperma", "долбаеб", "долбаёб", "dolbaeb", "гитлер");

        for (String word : forbiddenWords) {
            if (title_field.contains(word)) {

                addAudio_block_title_field.setBackgroundResource(R.drawable.edittext_error);

                // Запрещенное слово обнаружено
                Toast.makeText(getActivity(), "Запрещено использовать такое название!", Toast.LENGTH_SHORT).show();
                return false;

            }

        }
        addAudio_block_title_field.setBackgroundResource(R.drawable.edittext_field);
        return true;
    }


    private boolean checkPerformer(String performer_field) {
        if (performer_field.isEmpty()) {
            // Пароль не может быть пустым
            addAudio_block_performer_field.setBackgroundResource(R.drawable.edittext_error);
            Toast.makeText(getActivity(), "Исполнитель не может быть пустым!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (performer_field.length() > 20) {
            addAudio_block_performer_field.setBackgroundResource(R.drawable.edittext_error);
            Toast.makeText(getActivity(), "Исполнитель не может быть больше 20 символов!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!performer_field.matches("^[a-zA-Z0-9 ]+$")) {
            addAudio_block_performer_field.setBackgroundResource(R.drawable.edittext_error);
            Toast.makeText(getActivity(), "Исполнитель может содержать только буквы, цифры!", Toast.LENGTH_SHORT).show();
            return false;
        }

        List<String> forbiddenWords = Arrays.asList("хуй", "xuy", "huy", "хуйня", "huynya", "еблан", "лох", "lox", "пидр", "pidr", "пидор", "pidor",
                "eblan", "пизда", "pizda", "хуеосос", "huesos", "сперма", "sperma", "долбаеб", "долбаёб", "dolbaeb", "гитлер");

        for (String word : forbiddenWords) {
            if (performer_field.contains(word)) {

                addAudio_block_performer_field.setBackgroundResource(R.drawable.edittext_error);

                // Запрещенное слово обнаружено
                Toast.makeText(getActivity(), "Запрещено использовать такого исполнителя!", Toast.LENGTH_SHORT).show();
                return false;

            }

        }

        addAudio_block_performer_field.setBackgroundResource(R.drawable.edittext_field);
        return true;
    }


    // Обработчик с задачей для файлового менеджера (выбор аудиофайла на устройстве)
    private void chooseAudio() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        Intent chooser = Intent.createChooser(intent, "Выберите приложение для открытия аудиофайла");
        PackageManager packageManager = getActivity().getPackageManager();

        if (intent.resolveActivity(packageManager) != null) {
            openActivity.launch(chooser);
        } else {
            Toast.makeText(getActivity(), "Нет приложения для открытия аудиофайла", Toast.LENGTH_SHORT).show();
        }
    }

    // Регистрируем ActivityResultLauncher для получения результата из другой Activity
    ActivityResultLauncher<Intent> openActivity = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

        // Проверяем, что результат из Activity был ОК и данные не null
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {

            // Получаем URI выбранного файла
            Uri fileUri = result.getData().getData();

            if (fileUri != null) {

                // Сохраняем URI в переменную и обновляем UI
                file_url = fileUri;
                file_is_selected = true;
                infoAudio_block.setVisibility(View.VISIBLE);
                infoAudio_block_text.setText("Файл выбран");

                // Используем MediaMetadataRetriever для получения метаданных из аудиофайла
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(getContext(), file_url);

                // Извлекаем название и исполнителя из метаданных
                String title_file = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                String performer_file = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

                // Если название и исполнитель не пусты, обновляем поля ввода
                if (!TextUtils.isEmpty(title_file) && !TextUtils.isEmpty(performer_file)) {
                    addAudio_block_title_field.setText(title_file);
                    addAudio_block_performer_field.setText(performer_file);
                }

                // Освобождаем ресурсы MediaMetadataRetriever
                try {
                    mmr.release();
                } catch (IOException e) {
                    // В случае ошибки при освобождении ресурсов, выбрасываем исключение
                    throw new RuntimeException(e);
                }
            }
        }
    });


    // Метод загрузки файла аудиозаписи в Firebase Storage
    private void uploadAudio(Uri file_url) {

        //Генерируем уникальный ID для аудиозаписи
        String fileID = UUID.randomUUID().toString();

        // Запускаем показ диалогового окна
        Dialog customProgressDialog = createProgressDialog();
        customProgressDialog.show();

        ProgressBar progressBar = customProgressDialog.findViewById(R.id.progressBar);
        TextView percent = customProgressDialog.findViewById(R.id.percent);

        // Получаем ветку Firebase Storage с аудиозаписями текущего пользователя
        StorageReference storageRef = storage.getReference().child("uploads").child("audio_files").child(authentication.getCurrentUser().getUid()).child(fileID);

        // Доабвляем в нее новый файл с аудиозаписью
        storageRef.putFile(file_url).addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            int progressInt = (int) progress;
            progressBar.setProgress(progressInt);
            percent.setText(progressInt + "%");
        }).continueWithTask(task -> {
            // Если произошла ошибка добавления файла
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            // Если ошибки нет, получаем ссылку на файл в Firebase Storage
            return storageRef.getDownloadUrl();
        }).addOnCompleteListener(getActivity(), task -> {

            // Закрываем диалоговое окно с прогресс баром
            customProgressDialog.dismiss();

            if (task.isSuccessful()) {
                // Записываем полученную ссылку на файл в переменную
                String downloadUrl = task.getResult().toString();

                // Вызываем метод добавления аудиозаписи в базу данных
                createAudio(fileID, downloadUrl);

            } else {
                Toast.makeText(getActivity(), "Ошибка загрузки файла!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Метод создания диалогового окна с прогресс баром во время загрузки файла с аудиозаписью в Firebase Storage
    private Dialog createProgressDialog() {
        Dialog progressDialog = new Dialog(getActivity());
        progressDialog.setContentView(R.layout.progress);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressDialog.setCancelable(false);
        return progressDialog;
    }


    // Метод добавления аудиозаписи в базу данных
    private void createAudio(String fileID, String downloadUrl) {

        // Вызываем метод создания HashMap с информацией об аудиозаписи
        HashMap<String, Object> new_audio = createAudioInfoHashMap(fileID, downloadUrl);

        HashMap<String, Object> new_item_feed = createItemFeedInfoHashMap(new_audio);


        // Вызываем метод для создания HashMap с информацией путей обновления в базе данных
        HashMap<String, Object> updates = createDatabaseUpdateMap(fileID, new_audio, new_item_feed);

        // Добавляем данные в базу данных
        database.getReference().updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String message = "Аудиозапись успешно добавлена!";
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            } else {
                String message = "Ошибка добавления аудиозаписи. Повторите позже";
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Log.e(getTag(), "Ошибка доабвления аудиозаписи: " + e.getMessage()));

    }


    // Метод создания HashMap с информацией об аудиозаписи
    private HashMap<String, Object> createAudioInfoHashMap(String fileID, String downloadUrl) {

        long timestamp = System.currentTimeMillis();

        HashMap<String, Object> new_audio = new HashMap<>();
        new_audio.put("id_audio", fileID);
        new_audio.put("id_user", authentication.getCurrentUser().getUid());
        new_audio.put("title_audio", addAudio_block_title_field.getText().toString().trim());
        new_audio.put("performer_audio", addAudio_block_performer_field.getText().toString().trim());
        new_audio.put("url_audio", downloadUrl);
        new_audio.put("mood_happy", 0);
        new_audio.put("mood_sad", 0);
        new_audio.put("mood_normal", 0);
        new_audio.put("mood_angry", 0);
        new_audio.put("timestamp", timestamp);

        return new_audio;

    }


    // Метод создания HashMap с информацией для элемента ItemFeed в "Активности" профиля пользователя
    private HashMap<String, Object> createItemFeedInfoHashMap(HashMap<String, Object> new_audio) {

        String id_item_feed = UUID.randomUUID().toString();
        String string_item_feed = "Загрузил новую аудиозапись ''" + new_audio.get("title_audio") + " - " + new_audio.get("performer_audio") + "''";

        long timestamp = System.currentTimeMillis();

        HashMap<String, Object> new_item = new HashMap<>();
        new_item.put("id_item", id_item_feed);
        new_item.put("string_item", string_item_feed);
        new_item.put("timestamp", timestamp);

        return new_item;

    }


    // Метод вызывается для создания HashMap с информацией путей обновления информации в базе данных (информация об аудиозаписи и раздел User Feed (активность в профиле)
    private HashMap<String, Object> createDatabaseUpdateMap(String fileID, HashMap<String, Object> new_audio, HashMap<String, Object> new_item_feed) {

        // Создаем HashMap для обновления нескольких путей
        HashMap<String, Object> updates = new HashMap<>();

        // Внесение в аудиозаписи пользователя
        updates.put("/users/" + authentication.getCurrentUser().getUid() + "/user_uploads/audio/" + fileID, new_audio);

        // Внесение в общие загрузки (для эмоциональных рекомендаций)
        updates.put("/uploads/audio/" + fileID, new_audio);

        // Внесение инфомрации в раздел "Активность" профиля пользователя
        updates.put("/users/" + authentication.getCurrentUser().getUid() + "/user_feed/" + new_item_feed.get("id_item"), new_item_feed);

        // Обновляем значение количества аудиозаписей пользователя
        updates.put("/users/" + authentication.getCurrentUser().getUid() + "/user_information/colvo_audio", ServerValue.increment(1));

        return updates;

    }



    // БЛОК СОЗДАНИЯ ПЛЕЙЛИСТА

    // Метод вызывается для создания плейлиста
    private void createPlaylist() {

        String title_playlist = addPlaylist_block_title_field.getText().toString();

        if (!title_playlist.isEmpty()) {

            // Получаем путь до расположения плейлистов
            DatabaseReference playlistRef = database.getReference("users").child(authentication.getCurrentUser().getUid()).child("user_uploads").child("playlist");

            // Ищем совпадение хоть с одним плелйистом по полю title_playlist
            playlistRef.orderByChild("info_playlist/title_playlist").equalTo(title_playlist).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot main_snapshot) {

                    // Если совпадение найдено
                    if (main_snapshot.exists()) {

                        Toast.makeText(getContext(), "Плейлист с таким названием уже существует!", Toast.LENGTH_SHORT).show();

                    } else {

                        addPlaylist_block_title_field.setBackgroundResource(R.drawable.edittext_field);

                        // Создаем HashMap с информацией о плейлисте с помощью метода createPlaylistInfoHashMap
                        HashMap<String, Object> new_playlist = createPlaylistInfoHashMap(title_playlist);

                        // Делаем запрос на добавление плейлиста в базу данных
                        playlistRef.child((String) new_playlist.get("id_playlist")).child("info_playlist").setValue(new_playlist).addOnCompleteListener(task -> {

                            if (task.isSuccessful()) {

                                Snackbar.make(view, "Плейлист " + title_playlist + " успешно создан", Snackbar.LENGTH_LONG).addCallback(new Snackbar.Callback() {
                                    @Override
                                    public void onDismissed(Snackbar snackbar, int event) {
                                        if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                                            dismiss();
                                        }
                                    }
                                }).show();
                                //getDialog().dismiss();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(getTag(),error.getMessage());
                }

            });
        } else {

            addPlaylist_block_title_field.setBackgroundResource(R.drawable.edittext_error);
            Toast.makeText(getActivity(), "Введите название плейлиста!", Toast.LENGTH_SHORT).show();
        }

    }


    // Метод вызывается для создания HashMap с данными плейлиста
    public HashMap<String, Object> createPlaylistInfoHashMap(String title_playlist) {

        String IDPlaylist = UUID.randomUUID().toString();
        int colvoaudio = 0;

        HashMap<String, Object> new_playlist = new HashMap<>();
        new_playlist.put("id_playlist", IDPlaylist);
        new_playlist.put("title_playlist", title_playlist);
        new_playlist.put("colvoaudio_playlist", colvoaudio);

        return new_playlist;
    }



    private void openPlayerPredict (int position, String type_player, String user_mood, ArrayList<Audio> audioArrayList) {

        // Создаем bottom sheet
        BottomSheetPlayerPredict bottomSheetPlayerPredict = new BottomSheetPlayerPredict();

        // Собираем данные в Bundle
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        bundle.putString("type_player", type_player);
        bundle.putSerializable("audio_list", audioArrayList);

        // Устанавливаем данные в bottom sheet
        bottomSheetPlayerPredict.setArguments(bundle);

        // Запускаем bottom sheet
        bottomSheetPlayerPredict.show(getActivity().getSupportFragmentManager(), bottomSheetPlayerPredict.getTag());

    }


    private void loadAudiolist () {

        Query query = database.getReference("uploads").child("audio");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot main_snapshot) {

                // Если данные найдены
                if (main_snapshot.exists()) {

                    // Проходимся по дочерним элементам основного снимка записей из базы данных
                    for (DataSnapshot audioSnapshot : main_snapshot.getChildren()) {

                        // Проверка. Если аудиозапись добавлена текущим пользователем, то она пропускается
                        if (audioSnapshot.child("id_user").getValue().toString().equals(authentication.getCurrentUser().getUid().toString())) {
                            continue;
                        }

                        // Записываем полученные данные в переменные
                        String id_audio = audioSnapshot.getKey();
                        String id_user = audioSnapshot.child("id_user").getValue(String.class);
                        String title_audio = audioSnapshot.child("title_audio").getValue(String.class);
                        String performer_audio = audioSnapshot.child("performer_audio").getValue(String.class);
                        String url_audio = audioSnapshot.child("url_audio").getValue(String.class);

                        int mood_happy = audioSnapshot.child("mood_happy").getValue(Integer.class);
                        int mood_normal = audioSnapshot.child("mood_normal").getValue(Integer.class);
                        int mood_sad = audioSnapshot.child("mood_sad").getValue(Integer.class);
                        int mood_angry = audioSnapshot.child("mood_angry").getValue(Integer.class);

                        long timestamp = audioSnapshot.child("mood_sad").getValue(Long.class);

                        //Создаем объект аудиозапись с полученными данными
                        Audio audio = new Audio(id_audio, id_user, title_audio, performer_audio, url_audio, mood_happy, mood_normal, mood_sad, mood_angry, timestamp);

                        //Передаем объект аудиозапись в список с аудиозаписямм
                        audio_list.add(audio);

                    }

                    // Передаем список с аудиозаписями в адапетер Recycler View
                    audioAdapterALL.setList(audio_list);

                    // Уведомить адаптер об изменении данных
                    audioAdapterALL.notifyDataSetChanged();


                } else {
                    Toast.makeText(getContext(), "Аудиозаписи не найдены!", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(getTag(), "Ошибка: " + error.getMessage());
            }
        });
    }


    public void init (View view) {

        // Блок поиска аудиозаписей

        search_view = view.findViewById(R.id.search_view);
        search_view.setVisibility(View.VISIBLE);

        recycler_view = view.findViewById(R.id.recycler_view);
        recycler_view.setAdapter(audioAdapterALL);

        recycler_view.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));


        // Блок выбора действия
        selectBlock = view.findViewById(R.id.selectBlock);

        selectBlock_title = view.findViewById(R.id.selectBlock_title);

        selectBlock_add_audio_button = view.findViewById(R.id.selectBlock_add_audio_button);
        selectBlock_add_playlist_button = view.findViewById(R.id.predictBlock_select_button);

        // Блок добавления аудиозаписи

        addAudio_block = view.findViewById(R.id.addAudio_block);

        addAudio_block_title_field = view.findViewById(R.id.addAudio_block_title_field);
        addAudio_block_performer_field = view.findViewById(R.id.addAudio_block_performer_field);

        addAudio_block_select_audio_button = view.findViewById(R.id.addAudio_block_select_audio_button);
        addAudio_block_upload_audio_button = view.findViewById(R.id.addAudio_block_upload_audio_button);

        // Информация о выбранной аудиозаписи
        infoAudio_block = view.findViewById(R.id.infoAudio_block);

        infoAudio_block_text = view.findViewById(R.id.infoAudio_block_text);


        // Блок добавления плейлиста

        addPlaylist_block = view.findViewById(R.id.addPlaylist_block);

        addPlaylist_block_title_field = view.findViewById(R.id.addPlaylist_block_title_field);

        addPlaylist_block_create_button = view.findViewById(R.id.addPlaylist_block_create_button);

    }



}