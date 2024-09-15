package com.example.audiolibrary.Navigation.screens;


import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.audiolibrary.R;
import com.example.audiolibrary.RecyclerView.audiolistRecyclerView.Audio;
import com.example.audiolibrary.RecyclerView.audiolistRecyclerView.AudioAdapterPredict;
import com.example.audiolibrary.ml.Model;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;


public class FragmentDwa extends Fragment {

    // Подключение модулей Firebase
    private FirebaseDatabase database = FirebaseDatabase.getInstance();


    // Функциональная информация
    private static final int REQUEST_IMAGE_SELECT = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    ArrayList<Audio> audio_list = new ArrayList<>();


    // Блок определения эмоционального состояния
    TextView predict_emotion_field, predict_emotion_field2, predict_emotion_field3, predict_emotion_field4;
    ImageView imageView, predictBlock_emotion_image;
    TextView predictBlock_title2;
    Button add_emotional_button, generate_playlist, restart_button;


    //Блок сгенерированного плейлиста
    CardView playlistBlock;
    EditText playlistBlock_title_field;
    RecyclerView playlistBlock_recyclerView;
    AudioAdapterPredict audioAdapterPredict = new AudioAdapterPredict();
    Button save_playlist;
    boolean button_status = false;

    String USER_MOOD = "не определено";
    String user_mood = null;


    public interface OnFragmentSendDataListener {
        void onSendData(int position, String type_player, String user_mood, ArrayList<Audio> audioArrayList);
    }
    private FragmentOdin.OnFragmentSendDataListener fragmentSendDataListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            fragmentSendDataListener = (FragmentOdin.OnFragmentSendDataListener) context;
        } catch (ClassCastException e) {

        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dwa, container, false);
        return view;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        // Инициализация переменных
        init(view);

        // Инициализация анимации вращения для кнопки
        Animation animation_rotate = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);


        // Отправка выбранного трека в блок с плеером
        audioAdapterPredict.setOnItemClickListener((position, type_player, user_mood, audioArrayList) -> {
            fragmentSendDataListener.onSendData(position, type_player, user_mood, audioArrayList);
        });


        add_emotional_button.setOnClickListener(view14 -> {

            openSelectDialog();

        });

        generate_playlist.setOnClickListener(view13 -> {

            if (!USER_MOOD.isEmpty() && !USER_MOOD.equals("не определено")) {

                generate_playlist.setVisibility(View.GONE);
                restart_button.setVisibility(View.GONE);
                save_playlist.setVisibility(View.VISIBLE);

                if (USER_MOOD.equals("sad") || USER_MOOD.equals("angry")) {
                    cheerUpDialog(USER_MOOD);
                }
                loadAudioList();
            } else {
                String message = "Невозможно сгенерировать плейлист!";
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        save_playlist.setOnClickListener(view12 -> {

            if (button_status == false) {

                String title_playlist = playlistBlock_title_field.getText().toString();
                audioAdapterPredict.savePlaylist(title_playlist);

                button_status = true;

                save_playlist.setText("Плейлист сохранен");

            }

        });


        restart_button.setOnClickListener(view1 -> {

            restart_button.startAnimation(animation_rotate);

            openSelectDialog();


        });

    }


    // ПОЛУЧЕНИЕ ИЗОБРАЖЕНИЯ ОТ ПОЛЬЗОВАТЕЛЯ


    // Методы вызываются для выбора изображения с камеры или галереи смартфона
    private void captureImage() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    private void selectImage() {
        Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(openGalleryIntent, REQUEST_IMAGE_SELECT);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Bitmap bitmap = null;
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();
                bitmap = (Bitmap) extras.get("data");
            } else if (requestCode == REQUEST_IMAGE_SELECT) {
                Uri selectedImage = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImage);
                } catch (IOException e) {
                    Log.e("onActivityResult", "Error getting Bitmap from image", e);
                }
            }

            if (bitmap != null) {

                add_emotional_button.setVisibility(View.GONE);

                generate_playlist.setVisibility(View.VISIBLE);
                restart_button.setVisibility(View.VISIBLE);

                // Запуск метода для обработки полученного от пользователя изображения
                processingImage(bitmap);

            }
        }
    }


    private void openSelectDialog () {

        // Создаем AlertDialog.Builder для построения диалогового окна
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Выберите источник изображения");

        // Добавляем опции в диалоговое окно
        builder.setItems(new CharSequence[]{"Камера", "Галерея"}, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Обработка выбора пользователя
                switch (which) {
                    case 0: // Камера
                        captureImage();
                        break;
                    case 1: // Галерея
                        selectImage();
                        break;
                }
            }
        });

        // Показываем диалоговое окно
        builder.show();

    }




    // ПОДГОТОВКА ИЗОБРАЖЕНИЯ ПОЛУЧЕННОГО ОТ ПОЛЬЗОВАТЕЛЯ ДЛЯ МОДЕЛИ НЕЙРОСЕТИ


    // Метод вызывается для обработки полученного от пользователя изображения
    private void processingImage(Bitmap originalBitmap) {

        // Настройки Face Detector
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build();

        // Получаем в объект InputImage изображение от пользователя в виде Bitmap
        InputImage image = InputImage.fromBitmap(originalBitmap, 0);
        // Запуск Face Detector с заданными настройками
        FaceDetector detector = FaceDetection.getClient(options);
        detector.process(image).addOnSuccessListener(faces -> {
            if (faces.size() > 0) {
                Face face = faces.get(0);
                Rect bounds = face.getBoundingBox();
                // Обрезаем изображение по области лица
                Bitmap croppedBitmap = cropBitmap(bounds, originalBitmap);
                // Масштабирование до новой ширины и высоты с сохранением соотношения сторон
                Bitmap finalBitmap = scaleBitmap(croppedBitmap);
                imageView.setImageBitmap(finalBitmap);
                predictEmotion(finalBitmap);
            } else {
                String message = "Не удалось обнаружить лицо на изображении. Повторите еще раз";
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
            //detector.close();
        });

    }


    // Метод вызывается для обрезания bitmap по области лица
    private Bitmap cropBitmap (Rect bounds, Bitmap originalBitmap) {

        // Увеличение области на 30%
        int width = bounds.width();
        int height = bounds.height();

        int newWidth = (int) (width * 1.3);
        int newHeight = (int) (height * 1.3);

        int centerX = bounds.centerX();
        int centerY = bounds.centerY();

        int left = Math.max(0, centerX - newWidth / 2);
        int top = Math.max(0, centerY - newHeight / 2);

        int right = Math.min(originalBitmap.getWidth(), centerX + newWidth / 2);
        int bottom = Math.min(originalBitmap.getHeight(), centerY + newHeight / 2);

        Rect enlargedBounds = new Rect(left, top, right, bottom);

        // Вырезание увеличенной области
        Bitmap croppedBitmap = Bitmap.createBitmap(originalBitmap, enlargedBounds.left, enlargedBounds.top, enlargedBounds.width(), enlargedBounds.height());

        return croppedBitmap;
    }


    // Метод вызывается для масштабирования до новой ширины и высоты с сохранением соотношения сторон
    private Bitmap scaleBitmap (Bitmap croppedBitmap) {

        // Определение соотношения сторон
        float aspectRatio = (float) croppedBitmap.getWidth() / croppedBitmap.getHeight();
        int targetWidth, targetHeight;

        if (aspectRatio > 1) {
            // Ширина больше высоты, делаем ширину 224 и высоту по соотношению сторон
            targetWidth = 224;
            targetHeight = (int) (224 / aspectRatio);
        } else {
            // Высота больше ширины, делаем высоту 224 и ширину по соотношению сторон
            targetWidth = (int) (224 * aspectRatio);
            targetHeight = 224;
        }

        // Масштабирование до новой ширины и высоты с сохранением соотношения сторон
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(croppedBitmap, targetWidth, targetHeight, true);

        // Создание итогового изображения 224x224 с добавлением пустых областей
        Bitmap finalBitmap = Bitmap.createBitmap(224, 224, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(finalBitmap);
        int offsetX = (224 - targetWidth) / 2;
        int offsetY = (224 - targetHeight) / 2;
        canvas.drawBitmap(resizedBitmap, offsetX, offsetY, null);

        return finalBitmap;
    }






    // ОПРЕДЕЛЕНИЕ ЭМОЦИОНАЛЬНОГО СОСТОЯНИЯ ПОЛЬЗОВАТЕЛЯ


    // Метод вызывается для определения эмоционального состояния пользователя
    private void predictEmotion(Bitmap image){

        try {

            Model model = Model.newInstance(getActivity().getApplicationContext());

            // Создаем входные данные для модели.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            // Получаем одномерный массив из пикселей изображения.
            int [] intValues = new int[224 * 224];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

            // Проходимся по пикселям и извлекаем значения RGB, добавляем их в ByteBuffer.
            int pixel = 0;
        //    for(int i = 0; i < imageSize; i++){
        //        for(int j = 0; j < imageSize; j++){
        //            int val = intValues[pixel++]; // RGB
        //            byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
        //           byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
        //            byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
        //        }
        //    }

            for (int i = 0; i < 224; i++) {
                for (int j = 0; j < 224
                        ; j++) {
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat((val >> 16 & 0xFF) / 127.5f - 1.0f);
                    byteBuffer.putFloat((val >> 8 & 0xFF) / 127.5f - 1.0f);
                    byteBuffer.putFloat((val & 0xFF) / 127.5f - 1.0f);
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Запускаем модель и получаем результат.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            // Находим индекс класса с наибольшей уверенностью.
            int maxPos = 0;
            float maxConfidence = 0;
            for(int i = 0; i < confidences.length; i++){
                if(confidences[i] > maxConfidence){
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }

            String[] classes = {"happy", "neutral", "sad", "angry"};

            String s = "";
            for(int i = 0; i < classes.length; i++){
                if (i > 0) s += ", "; // Добавляем запятую между элементами
                s += String.format("%s: %.1f%%", classes[i], confidences[i] * 100);
            }

            // Создаем массив строк для хранения вероятностей классов.
            String[] classConfidences = new String[classes.length];

            // Форматируем вероятности и сохраняем в массив.
            for(int i = 0; i < classes.length; i++){
                classConfidences[i] = String.format("%s: %.1f%%", classes[i], confidences[i] * 100);
            }

            // Записываем результат опрделения в переменную эмоционального состояния пользователя
            USER_MOOD = classes[maxPos];
            predictBlock_title2.setText(USER_MOOD);

            predict_emotion_field.setText(classConfidences[0]);
            predict_emotion_field2.setText(classConfidences[1]);
            predict_emotion_field3.setText(classConfidences[2]);
            predict_emotion_field4.setText(classConfidences[3]);

            selectEmotionAction(USER_MOOD);

            // Освобождаем ресурсы модели, если они больше не используются.
            model.close();

        } catch (IOException e) {
            // Обработка исключения
            e.printStackTrace();
        }
    }


    // Метод вызывается для вызова диалогового окна (Повысить настроение?)
    private void cheerUpDialog (String USER_MOOD) {

        String current_mood = USER_MOOD;

        // Создание и отображение диалога
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        if (USER_MOOD.equals("sad")) {
            current_mood = "грустное";
        }

        if (USER_MOOD.equals("angry")) {
            current_mood = "злое";
        }

        builder.setMessage("У вас обнаружено " + current_mood + " настроение. Улучшить?").setPositiveButton("Да", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                String current_mood_start = "happy";

                dialog.cancel();
                selectEmotionAction(current_mood_start);

                Toast.makeText(getContext(), "Выполнен подбор треков для повышения настроения", Toast.LENGTH_SHORT).show();

            }
        }).setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                dialog.cancel();
                selectEmotionAction(USER_MOOD);

            }
        });
        AlertDialog alert = builder.create();
        alert.show();

    }


    // Метод вызывается для выбора действий в зависимости от определнного эмоционального состояния пользователя
    private void selectEmotionAction(String recognizedObjectName) {
        int imageResId;
        String userMood;

        switch (recognizedObjectName) {
            case "happy":
                imageResId = R.drawable.emotion_happy;
                userMood = "mood_happy";
                predictBlock_title2.setText("Радостное");
                audioAdapterPredict.setUserMood(userMood);

                break;
            case "neutral":
                imageResId = R.drawable.emotion_neutral;
                userMood = "mood_normal";
                predictBlock_title2.setText("Нейтральное");
                audioAdapterPredict.setUserMood(userMood);
                break;
            case "sad":
                imageResId = R.drawable.emotion_sad;
                userMood = "mood_sad";
                predictBlock_title2.setText("Грустное");
                audioAdapterPredict.setUserMood(userMood);
                break;
            case "angry":
                imageResId = R.drawable.emotion_angry;
                userMood = "mood_angry";
                predictBlock_title2.setText("Злое");
                audioAdapterPredict.setUserMood(userMood);
                break;
            default:
                Toast.makeText(getContext(), "Не удалось определить эмоциональное состояние!", Toast.LENGTH_SHORT).show();
                return; // Выход из метода, если эмоция не распознана
        }

        user_mood = userMood;

        predictBlock_emotion_image.setImageResource(imageResId);

        audioAdapterPredict.setUserMood(user_mood);

    }






    // СОЗДАНИЕ ПЛЕЙЛИСТА ПОД ЭМОЦИОНАЛЬНОЕ СОСТОЯНИЕ ПОЛЬЗОВАТЕЛЯ


    // Метод вызывается для загрузки списка аудиозаписей подходящих под настроение пользователя
    private void loadAudioList() {

        Query query = database.getReference("uploads").child("audio");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot main_snapshot) {

                if (main_snapshot.exists()) {

                    for (DataSnapshot audioSnapshot : main_snapshot.getChildren()) {

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

                        long timestamp = audioSnapshot.child("timestamp").getValue(Long.class);

                        //Создаем объект аудиозапись с полученными данными
                        Audio audio = new Audio(id_audio, id_user, title_audio, performer_audio, url_audio, mood_happy, mood_normal, mood_sad, mood_angry, timestamp);

                        //Передаем объект аудиозапись в список с аудиозаписямм
                        audio_list.add(audio);

                    }



                    // Создаем плейлист
                    createPlaylist(audio_list, user_mood);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(getTag(), error.getMessage());
            }
        });

    }


    // Метод вызывается для создания плейлиста из 5 подходящих под USER_MOOD аудиозаписей
    public void createPlaylist(ArrayList<Audio> audio_list, String USER_MOOD) {
        ArrayList<Audio> filteredAudios = new ArrayList<>();

        Log.d("DEBUG", "USER_MOOD: " + USER_MOOD); // Логирование текущего USER_MOOD

        for (Audio audio : audio_list) {
            String dominantMood = getDominantEmotion(USER_MOOD, audio);

            Log.d("DEBUG", "Dominant Mood: " + dominantMood); // Логирование доминирующего настроения для каждой аудиозаписи

            if (dominantMood.equals(USER_MOOD) || dominantMood.equals("mood_mixed")) {
                filteredAudios.add(audio);
            }
        }

        // Перемешиваем отфильтрованный список
        Collections.shuffle(filteredAudios);

        // Ограничиваем количество аудиозаписей до 5
        ArrayList<Audio> randomAudios = new ArrayList<>(filteredAudios.subList(0, Math.min(5, filteredAudios.size())));

        // Логирование для проверки содержимого отфильтрованного списка
        Log.d("FILTERED_AUDIOS", "Количество элементов в отфильтрованном списке: " + filteredAudios.size());
        for (Audio audio : filteredAudios) {
            Log.d("FILTERED_AUDIOS", "Добавлена аудиозапись: " + audio.getTitle_audio());
        }

        // Передаем отфильтрованный список в адаптер и обновляем RecyclerView
        audioAdapterPredict.setList(randomAudios);
        audioAdapterPredict.notifyDataSetChanged();


        playlistBlock.setVisibility(View.VISIBLE);
        playlistBlock_recyclerView.setVisibility(View.VISIBLE);

    }


    // Метод вызывается для определения доминирующего настроения аудиозаписи
    private String getDominantEmotion(String USER_MOOD, Audio audio) {

        int mood_happy = audio.getMood_happy();
        int mood_normal = audio.getMood_normal();
        int mood_sad = audio.getMood_sad();
        int mood_angry = audio.getMood_angry(); // Добавляем новое настроение

        // Если все настроения равны, возвращаем USER_MOOD
        if (mood_happy == mood_sad && mood_happy == mood_normal && mood_happy == mood_angry) {
            return USER_MOOD;
        }

        // Если разница между настроениями не превышает 3, возвращаем 'mood_mixed'
        if (Math.abs(mood_happy - mood_sad) <= 3 && Math.abs(mood_happy - mood_normal) <= 3 &&
                Math.abs(mood_happy - mood_angry) <= 3 && Math.abs(mood_sad - mood_normal) <= 3 &&
                Math.abs(mood_sad - mood_angry) <= 3 && Math.abs(mood_normal - mood_angry) <= 3) {
            return "mood_mixed";
        }

        // Определяем доминирующее настроение
        int maxMood = Math.max(Math.max(mood_happy, mood_sad), Math.max(mood_normal, mood_angry));
        if (maxMood == mood_happy) {
            return "mood_happy";
        } else if (maxMood == mood_sad) {
            return "mood_sad";
        } else if (maxMood == mood_normal) {
            return "mood_normal";
        } else {
            return "mood_angry";
        }
    }






    public void init (View view) {

        add_emotional_button = getActivity().findViewById(R.id.add_emotional_button);
        add_emotional_button.setVisibility(View.VISIBLE);


        predictBlock_title2 = view.findViewById(R.id.predictBlock_status);
        predictBlock_title2.setText("не определено");

        predict_emotion_field = getActivity().findViewById(R.id.predict_emotion_field);
        predict_emotion_field2 = view.findViewById(R.id.predict_emotion_field2);
        predict_emotion_field3 = view.findViewById(R.id.predict_emotion_field3);
        predict_emotion_field4 = view.findViewById(R.id.predict_emotion_field4);


        imageView = getActivity().findViewById(R.id.predictBlock_user_image);
        predictBlock_emotion_image = view.findViewById(R.id.predictBlock_emotion_image);

        restart_button = view.findViewById(R.id.restart_button);
        restart_button.setVisibility(View.GONE);



        // Блок сгенерированного плейлиста

        generate_playlist = view.findViewById(R.id.generate_playlist);
        generate_playlist.setVisibility(View.GONE);


        playlistBlock = view.findViewById(R.id.playlistBlock);
        playlistBlock.setVisibility(View.GONE);

        playlistBlock_title_field = view.findViewById(R.id.playlistBlock_title_field);

        playlistBlock_recyclerView = view.findViewById(R.id.playlistBlock_recyclerView);
        playlistBlock_recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        DividerItemDecoration divider = new DividerItemDecoration(playlistBlock_recyclerView.getContext(), LinearLayoutManager.VERTICAL);
        playlistBlock_recyclerView.addItemDecoration(divider);

        playlistBlock_recyclerView.setAdapter(audioAdapterPredict);

        save_playlist = view.findViewById(R.id.save_playlist);
        save_playlist.setVisibility(View.GONE);

    }





}