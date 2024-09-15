package com.example.audiolibrary;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BottomSheetProfileSettings extends BottomSheetDialogFragment {


    // Подключение модулей Firebase
    private FirebaseAuth authentication = FirebaseAuth.getInstance();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private BottomSheetProfileSettings bottomSheetProfileSettings;


    // Блок изображения пользователя
    private ImageView profile_image;
    private CardView profileImageBlock;
    private FloatingActionButton add_profile_image_button;


    // Блок данных пользователя
    private EditText login_field_settings;
    private Button save_settings_button;
    private TextView delete_profile_button;


    // Хранение данных пользователя
    private String user_login_database;
    private String image_url;
    Bitmap croppedBitmap;
    private Uri fileUri;
    private static final int PICK_IMAGE_REQUEST = 1;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bs_profile_settings, container, false);
        init(view);
        if (authentication.getCurrentUser() != null) {
            loadProfileData();
        } else {
            Intent intent = new Intent(getActivity(), SplashScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            authentication.signOut();
            startActivity(intent);
        }
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (authentication.getCurrentUser() != null) {
            init(view);
            loadProfileData();
        } else {
            Intent intent = new Intent(getActivity(), SplashScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            authentication.signOut();
            startActivity(intent);
        }

        save_settings_button.setOnClickListener(view1 -> {
            saveChanges();
        });

        add_profile_image_button.setOnClickListener(view12 -> {
            getImageFromUser();
        });

        delete_profile_button.setOnClickListener(view13 -> {
            deleteProfile();
        });


    }


    public void init(View view) {

        // Блок настроек изображения пользователя
        profileImageBlock = view.findViewById(R.id.profileImageBlock);
        profile_image = view.findViewById(R.id.profile_image);
        add_profile_image_button = view.findViewById(R.id.add_profile_image_button);

        // Блок настроек данных пользователя
        login_field_settings = view.findViewById(R.id.login_field_settings);
        save_settings_button = view.findViewById(R.id.save_settings_button);

        delete_profile_button = view.findViewById(R.id.delete_button);

    }


    // Метод вызывается для получения данных профиля пользователя
    private void loadProfileData() {
        Query getLogin = database.getReference("users").child(authentication.getCurrentUser().getUid()).child("user_data").child("login");
        getLogin.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    user_login_database = snapshot.getValue(String.class);
                    login_field_settings.setText(user_login_database);
                } else {
                    String message = "Ошибка получения данных! Попробуйте позже";
                    Log.e("Ошибка loadProfileData()", message);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(getTag(), error.getMessage());
            }
        });


        Query getImage = database.getReference("users").child(authentication.getCurrentUser().getUid()).child("user_data").child("profile_image");
        getImage.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    image_url = snapshot.getValue(String.class);
                    if (isAdded() && getActivity() != null) {
                        if (!image_url.isEmpty()) {
                            Glide.with(getContext().getApplicationContext())
                                    .load(image_url)
                                    .into(profile_image);
                        }
                    } else {
                        Log.e("Profile Image", "Фрагмент не прикреплен к активности");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(getTag(), error.getMessage());
            }
        });
    }


    // Метод вызывается для сохранения изменений в данных профиля пользователя
    private void saveChanges() {
        String user_login = login_field_settings.getText().toString().trim();

        if (fileUri == null && user_login.equals(user_login_database)) {
            String message = "Изменений нет";
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }

        if (checkLoginField(user_login) && !user_login.equals(user_login_database)) {
            Task<Void> saveLogin = database.getReference("users").child(authentication.getCurrentUser().getUid()).child("user_data").child("login").setValue(user_login);
            saveLogin.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String message = "Логин изменен";
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                } else {
                    String message = "Ошибка изменения логина. Попробуйте позже";
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (fileUri != null) {
            String id_user = authentication.getCurrentUser().getUid();

            // Проверяем существование и непустоту child("profile_image")
            Query check_profile_image = database.getReference("users").child(id_user).child("user_data").child("profile_image");
            check_profile_image.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.getValue(String.class) != null && !dataSnapshot.getValue(String.class).isEmpty()) {
                        // Удаляем файл в хранилище по URL
                        String imageUrl = dataSnapshot.getValue(String.class);
                        StorageReference existingImageRef = storage.getReferenceFromUrl(imageUrl);
                        existingImageRef.delete().addOnSuccessListener(aVoid -> {
                            // Начинаем загрузку нового изображения
                            uploadNewImage(id_user, croppedBitmap);
                        }).addOnFailureListener(e -> {
                            // Обработка ошибки удаления
                            String message = "Ошибка удаления старого изображения. Попробуйте позже";
                            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        // Начинаем загрузку нового изображения (без удаления старого)
                        uploadNewImage(id_user, croppedBitmap);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Обработка ошибки чтения данных
                    String message = "Ошибка чтения данных из базы. Попробуйте позже";
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                }
            });
        }

    }


    private void uploadNewImage(String id_user, Bitmap croppedBitmap) {
        String uniqueImageId = UUID.randomUUID().toString();
        StorageReference imageRef = storage.getReference().child("uploads/profile_images/" + id_user + "/" + uniqueImageId + ".jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String newFileUrl = uri.toString();
                // Обновляем image_url в базе данных
                database.getReference("users")
                        .child(id_user)
                        .child("user_data")
                        .child("profile_image")
                        .setValue(newFileUrl)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String message = "Изображение изменено";
                                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                            } else {
                                String message = "Ошибка изменения изображения. Попробуйте позже";
                                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                            }
                        });
            });
        }).addOnFailureListener(e -> {
            // Обработка ошибки загрузки нового изображения
            String message = "Ошибка загрузки нового изображения. Попробуйте позже";
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        });
    }


    // Метод вызывается для проверки данных полученных от пользователя в поле "Логин"
    private boolean checkLoginField(String login) {

        if (login.isEmpty()) {
            login_field_settings.setBackgroundResource(R.drawable.edittext_error);
            String message = "Логин не может быть пустым!";
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (login.length() < 6) {
            login_field_settings.setBackgroundResource(R.drawable.edittext_error);
            String message = "Минимум 6 символов!";
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (login.length() > 20) {
            login_field_settings.setBackgroundResource(R.drawable.edittext_error);
            String message = "Не длиннее 20 символов!";
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!login.matches("^[a-zA-Z0-9 ]+$")) {
            login_field_settings.setBackgroundResource(R.drawable.edittext_error);
            String message = "Только буквы, цифры!";
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            return false;
        }

        // Список запрещенных слов
        List<String> forbiddenWords = Arrays.asList("хуй", "xuy", "huy", "хуйня", "huynya", "еблан", "лох", "lox", "пидр", "pidr", "пидор", "pidor",
                "eblan", "пизда", "pizda", "хуеосос", "huesos", "сперма", "sperma", "долбаеб", "долбаёб", "dolbaeb", "гитлер");

        for (String word : forbiddenWords) {
            if (login.contains(word)) {
                login_field_settings.setBackgroundResource(R.drawable.edittext_error);
                String message = "Запрещено использовать такой логин!";
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        login_field_settings.setBackgroundResource(R.drawable.edittext_field);
        // Если цикл завершился и запрещенные слова не найдены, логин корректен
        return true;
    }


    // Метод вызывается для получения изображения от пользователя
    private void getImageFromUser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Выберите изображение"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            fileUri = data.getData();
            if (fileUri != null) {
                processImage(fileUri);
            }
        }
    }

    private void processImage(Uri imageUri) {
        try {
            Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);

            // Вычисляем размер обрезанного изображения
            int targetSize = 200;

            // Выбираем минимальный размер (ширина или высота) для обрезки
            int cropSize = Math.min(originalBitmap.getWidth(), originalBitmap.getHeight());

            if (cropSize >= targetSize) {
                // Вычисляем координаты для обрезки изображения
                int centerX = originalBitmap.getWidth() / 2;
                int centerY = originalBitmap.getHeight() / 2;

                // Обрезаем изображение
                croppedBitmap = Bitmap.createBitmap(originalBitmap, centerX - cropSize / 2, centerY - cropSize / 2, cropSize, cropSize);

                // Устанавливаем обрезанное изображение в ImageView
                profile_image.setImageBitmap(croppedBitmap);
            } else {
                // Изображение слишком маленькое для обрезки
                String message = "Изображение не подходит. Минимальный размер 200x200px";
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Метод вызывается для удаления профиля пользователя
    private void deleteProfile() {
        String message = "ЗАПРОС НА УДАЛЕНИЕ ПРОФИЛЯ СОЗДАН";
        Log.d("Удаление профиля", message);
        deleteUser();
    }


    // Метод вызывается для проверки наличия у пользователя загруженных в систему аудиозаписей
    private void checkUploads() {
        String message = "ПОИСК ЗАГРУЖЕННЫХ В СИСТЕМУ АУДИОЗАПИСЕЙ У ПОЛЬЗОВАТЕЛЯ";
        Log.d("Удаление профиля", message);

        // Поиск загруженных аудиозаписей пользователем
        Query query = database.getReference("users").child(authentication.getCurrentUser().getUid()).child("user_uploads").child("audio");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot main_snapshot) {
                boolean audioExists = false;
                StringBuilder audioListBuilder = new StringBuilder();
                for (DataSnapshot snapshot : main_snapshot.getChildren()) {
                    String id_user = snapshot.child("id_user").getValue(String.class);
                    String title_audio = snapshot.child("title_audio").getValue(String.class);
                    String performer_audio = snapshot.child("performer_audio").getValue(String.class);
                    // Проверка поля id_user у каждой аудиозаписи в аудиозаписях пользователя на соответсвие id текущего пользователя
                    if (id_user != null && id_user.equals(authentication.getCurrentUser().getUid())) {
                        audioExists = true;
                        audioListBuilder.append(title_audio + " - " + performer_audio).append("\n");
                    }
                }
                // Если загруженные аудиозаписи были найдены
                if (audioExists) {
                    String message = "Аудиозаписи были найдены";
                    Log.d("Удаление профиля", message);

                    // Show dialog with track details
                    showAudioListDialog(audioListBuilder.toString());

                } else {
                    String message = "Аудиозаписи не были найдены";
                    Log.d("Удаление профиля", message);
                    String message2 = "Запущено удаление профиля";
                    Toast.makeText(getContext(), message2, Toast.LENGTH_SHORT).show();
                    deleteAllImageFilesFromStorage();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Check Uploads", "Ошибка при проверке аудиозаписей: " + databaseError.getMessage());
            }
        });
    }

    private void showAudioListDialog(String audioList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("");

        // Inflate and set the custom view
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_audio_list, null);
        builder.setView(dialogView);

        TextView tvAudioList = dialogView.findViewById(R.id.tvAudioList);
        tvAudioList.setText(audioList);

        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    // Метод вызывается для удаления аватарки пользователя из Firebase Storage
    private void deleteAllImageFilesFromStorage () {
        String message = "УДАЛЕНИЕ АВАТАРКИ ПОЛЬЗОВАТЕЛЯ ИЗ FIREBASE STORAGE";
        Log.d("Удаление профиля", message);
        StorageReference storageReference = storage.getReference("uploads").child("profile_images").child(authentication.getCurrentUser().getUid());
        storageReference.listAll().addOnSuccessListener(listResult -> {
            if (listResult.getItems().isEmpty()) {
                Log.d("Удаление профиля", "Аватарки нет у пользователя: " + authentication.getCurrentUser().getUid());
                // Переход к следующему пункту
                deleteALlInDatabase();
                return;
            }
            for (StorageReference fileRef : listResult.getItems()) {
                // Удаление каждого файла
                fileRef.delete().addOnSuccessListener(aVoid -> Log.d("Удаление профиля", "Аватарка пользователя удалена: " + fileRef.getPath())).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e("Delete Image", "Ошибка при удалении файла: " + fileRef.getPath() + " - " + exception.getMessage());
                    }
                });
            }
            deleteALlInDatabase();
        }).addOnFailureListener(exception -> Log.e("Удаление профиля", "Ошибка при удалении аватарки: " + exception.getMessage()));
    }


    // Метод вызывается для удаления всех данных пользователя из Firebase Realtime Database
    private void deleteALlInDatabase () {

        FirebaseUser current_user = authentication.getCurrentUser();

        String message = "УДАЛЕНИЕ ВСЕХ ДАННЫХ ИЗ FIREBASE REALTIME DATABASE";
        Log.d("Удаление профиля", message);
        Task<Void> query = database.getReference("users").child(authentication.getCurrentUser().getUid()).removeValue();
        query.addOnSuccessListener(unused -> {
            if (query.isSuccessful()) {
                Log.d("Удаление профиля", "Удаление данных из базы данных выполнено");
                current_user.delete().addOnSuccessListener(unused1 -> {
                    String message1 = "Профиль полностью удален";
                    Toast.makeText(getActivity(), message1, Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(getActivity(), SplashScreen.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    authentication.signOut();
                    startActivity(intent);
                }).addOnFailureListener(e -> {
                    // Handle the failure of the delete operation
                    String message1 = "Ошибка при удалении профиля: " + e.getMessage();
                    Toast.makeText(getActivity(), message1, Toast.LENGTH_SHORT).show();
                });
            } else {
                Log.d("Удаление профиля", "Удаление данных из базы данных выполнено");
            }
        });
    }


    // Метод вызывается для удаления пользвоателя из Firebase Authentication
    private void deleteUser() {
        String message = "УДАЛЕНИЕ ПОЛЬЗОВАТЕЛЯ ИЗ FIREBASE AUTHETICATION";
        Log.d("Удаление профиля", message);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            String message1 = "Нет пользователя для удаления";
            Toast.makeText(getActivity(), message1, Toast.LENGTH_SHORT).show();
            return;
        }


        EditText passwordInput = new EditText(getActivity());
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Удаление профиля");
        builder.setMessage("Пожалуйста, введите ваш пароль для подтверждения:");
        builder.setView(passwordInput);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String password = passwordInput.getText().toString();

            if (password.isEmpty()) {
                Toast.makeText(getActivity(), "Пароль не может быть пустым", Toast.LENGTH_SHORT).show();
                return;
            }
            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), password);

            currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                    checkUploads();

                } else {
                    // Handle the failure of the re-authentication operation
                    String message1 = "Ошибка при повторной аутентификации: " + task.getException().getMessage();
                    Toast.makeText(getActivity(), message1, Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());

        builder.show();
    }


}

