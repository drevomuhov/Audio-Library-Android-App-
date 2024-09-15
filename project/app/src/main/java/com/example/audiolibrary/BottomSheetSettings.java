package com.example.audiolibrary;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.audiolibrary.Navigation.Navigation_Controller;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class BottomSheetSettings extends BottomSheetDialogFragment {

    LinearLayout signIn_block, signUp_block;

    EditText signIn_block_block_email_field, signIn_block_block_password_field, signUp_block_email_field, signUp_block_login_field, signUp_block_password_field;

    TextView signIn_block_signUp_button, signUp_block_signIn_button;
    Button signIn_block_signIn_button, signUp_block_signUp_button;

    private FirebaseAuth authentication = FirebaseAuth.getInstance();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bs_signup_signin, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        init(view);

        signIn_block_signIn_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Получение данных о пользователе из формы авторизации
                String email = signIn_block_block_email_field.getText().toString().trim();
                String password = signIn_block_block_password_field.getText().toString().trim();

                // Проверка на заполнение полей
                if (checkEmail(email) == true && checkPassword(password) == true)  {

                    Task<AuthResult> query = authentication.signInWithEmailAndPassword(email, password);

                    query.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {

                                // Успешная аутентификация
                                Intent intent = new Intent(getActivity(), Navigation_Controller.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);


                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(getContext(), " Ошибка авторизации. Неверный e-mail или пароль! | " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            }
        });

        signIn_block_signUp_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                signIn_block.setVisibility(View.GONE);
                signUp_block.setVisibility(View.VISIBLE);

            }
        });

        signUp_block_signUp_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Отключаем кнопку для предотвращения многократного нажатия
                signUp_block_signUp_button.setEnabled(false);

                // Данные о пользователе из формы регистрации
                String email = signUp_block_email_field.getText().toString().trim();
                String login = signUp_block_login_field.getText().toString().trim();
                String password = signUp_block_password_field.getText().toString().trim();

                // Дополнительные данные о пользователе
                String register_date = DateFormat.format("dd-MM-yyyy", new Date()).toString();
                String uploaded_audio = "0";
                String friends = "0";

                // Проверка на заполнение полей
                if (checkEmail(email) && checkLogin(login) && checkPassword(password)) {
                    Task<AuthResult> query = authentication.createUserWithEmailAndPassword(email, password);
                    query.addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            signUpUser(email, login, register_date, uploaded_audio, friends);
                        } else {
                            // Включаем кнопку обратно, если произошла ошибка
                            signUp_block_signUp_button.setEnabled(true);
                        }
                    }).addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Пользователь с таким e-mail уже зарегистрирован! | " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        // Включаем кнопку обратно в случае ошибки
                        signUp_block_signUp_button.setEnabled(true);
                    });
                } else {
                    // Включаем кнопку обратно, если поля не заполнены корректно
                    signUp_block_signUp_button.setEnabled(true);
                }
            }
        });

        signUp_block_signIn_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                signUp_block.setVisibility(View.GONE);
                signIn_block.setVisibility(View.VISIBLE);

            }
        });

    }


    public void init (View view) {

        // Блок авторизации
        signIn_block = view.findViewById(R.id.signIn_block);
        signIn_block.setVisibility(View.GONE);


        signIn_block_block_email_field = view.findViewById(R.id.signIn_block_email_field);
        signIn_block_block_password_field = view.findViewById(R.id.signIn_block_password_field);

        signIn_block_signIn_button = view.findViewById(R.id.signIn_block_signIn_button);
        signIn_block_signUp_button = view.findViewById(R.id.signIn_block_signUp_button);



        // Блок регистрации
        signUp_block = view.findViewById(R.id.signUp_block);
        signUp_block.setVisibility(View.VISIBLE);

        signUp_block_email_field = view.findViewById(R.id.signUp_block_email_field);
        signUp_block_login_field = view.findViewById(R.id.signUp_block_login_field);
        signUp_block_password_field = view.findViewById(R.id.signUp_block_password_field);

        signUp_block_signIn_button = view.findViewById(R.id.signUp_block_signIn_button);
        signUp_block_signUp_button = view.findViewById(R.id.signUp_block_signUp_button);
    }


    // Метод вызывается для проверки поля email на пустоту и соотвествие формату E-mail
    private boolean checkEmail(String email) {

        // Регулярное выражение для проверки email
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

        if (email.isEmpty()) {
            signIn_block_block_email_field.setBackgroundResource(R.drawable.edittext_error);
            signUp_block_email_field.setBackgroundResource(R.drawable.edittext_error);
            Toast.makeText(getActivity(), "Email не может быть пустым!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!email.matches(emailRegex)) {
            signIn_block_block_email_field.setBackgroundResource(R.drawable.edittext_error);
            signUp_block_email_field.setBackgroundResource(R.drawable.edittext_error);
            Toast.makeText(getActivity(), "Некорректный формат email!", Toast.LENGTH_SHORT).show();
            return false;
        }

        signUp_block_email_field.setBackgroundResource(R.drawable.edittext_field);
        signIn_block_block_email_field.setBackgroundResource(R.drawable.edittext_field);
        // Если email соответствует формату, он корректен
        return true;
    }


    // Метод вызывается для проверки поля с логином на пустоту, длину заполнения, использвание букв и цифр и наличие нецензурных слов
    private boolean checkLogin(String login) {

        if (login.isEmpty()) {
            signUp_block_login_field.setBackgroundResource(R.drawable.edittext_error);
            Toast.makeText(getActivity(), "Логин не может быть пустым!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (login.length() < 6) {
            signUp_block_login_field.setBackgroundResource(R.drawable.edittext_error);
            Toast.makeText(getActivity(), "Логин должен содержать минимум 6 символов!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (login.length() > 20) {
            signUp_block_login_field.setBackgroundResource(R.drawable.edittext_error);
            Toast.makeText(getActivity(), "Логин не может быть длиннее 20 символов!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!login.matches("^[a-zA-Z0-9 ]+$")) {
            signUp_block_login_field.setBackgroundResource(R.drawable.edittext_error);
            Toast.makeText(getActivity(), "Исполнитель может содержать только буквы, цифры!", Toast.LENGTH_SHORT).show();
            return false;
        }

        List<String> forbiddenWords = Arrays.asList("хуй", "xuy", "huy", "хуйня", "huynya", "еблан", "лох", "lox", "пидр", "pidr", "пидор", "pidor",
                "eblan", "пизда", "pizda", "хуеосос", "huesos", "сперма", "sperma", "долбаеб", "долбаёб", "dolbaeb", "гитлер"); // Список запрещенных слов

        for (String word : forbiddenWords) {

            if (login.contains(word)) {

                signUp_block_login_field.setBackgroundResource(R.drawable.edittext_error);

                // Запрещенное слово обнаружено
                Toast.makeText(getActivity(), "Запрещено использовать такой логин!", Toast.LENGTH_SHORT).show();
                return false;

            }

        }

        signUp_block_login_field.setBackgroundResource(R.drawable.edittext_field);

        // Если цикл завершился и запрещенные слова не найдены, логин корректен
        return true;
    }


    // Метод вызывается для проверки поля с паролем на пустоту, длину заполнения и простоту
    private boolean checkPassword(String password) {
        if (password.isEmpty()) {
            // Пароль не может быть пустым
            signUp_block_password_field.setBackgroundResource(R.drawable.edittext_error);
            signIn_block_block_password_field.setBackgroundResource(R.drawable.edittext_error);
            Toast.makeText(getActivity(), "Пароль не может быть пустым!", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Проверка на длину пароля
        if (password.length() < 6) {
            signUp_block_password_field.setBackgroundResource(R.drawable.edittext_error);
            signIn_block_block_password_field.setBackgroundResource(R.drawable.edittext_error);
            Toast.makeText(getActivity(), "Пароль не может быть меньше 6 символов!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() > 50) {
            signUp_block_password_field.setBackgroundResource(R.drawable.edittext_error);
            signIn_block_block_password_field.setBackgroundResource(R.drawable.edittext_error);
            Toast.makeText(getActivity(), "Пароль не может быть больше 50 символов!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.matches("^[a-zA-Z0-9]+$") || password.contains(" ")) {
            signUp_block_login_field.setBackgroundResource(R.drawable.edittext_error);
            Toast.makeText(getActivity(), "Пароль может содержать только буквы и цифры, без пробелов!", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Проверка на простоту пароля (например, не должен состоять только из одинаковых символов)
        char firstChar = password.charAt(0);
        for (int i = 1; i < password.length(); i++) {
            if (password.charAt(i) != firstChar) {
                signUp_block_password_field.setBackgroundResource(R.drawable.edittext_field);
                signIn_block_block_password_field.setBackgroundResource(R.drawable.edittext_field);
                return true; // Если найден хотя бы один другой символ, пароль не слишком простой
            }
        }
        signUp_block_password_field.setBackgroundResource(R.drawable.edittext_error);
        signIn_block_block_password_field.setBackgroundResource(R.drawable.edittext_error);
        Toast.makeText(getActivity(), "Пароль слишком простой!", Toast.LENGTH_SHORT).show();
        return false; // Если цикл завершился успешно, пароль слишком простой
    }


    // Метод вызывается для регистрации нового пользователя в базе данных
    private void signUpUser(String email, String login, String register_date, String uploaded_audio, String friends) {

        HashMap<String, Object> userData = new HashMap<>();

        userData.put("user_data", new HashMap<String, String>() {{
            put("email", email);
            put("login", login);
        }});

        userData.put("user_information", new HashMap<String, String>() {{
            put("register_date", register_date);
            put("colvo_audio", uploaded_audio);
            put("colvo_friends", friends);
        }});

        // Запрос в базу данных на регистрацию пользователя в базе данных
        Task<Void> query = database.getReference().child("users").child(authentication.getCurrentUser().getUid()).setValue(userData);
        query.addOnSuccessListener(unused -> {
            String message = "Регистрация прошла успешно";
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), Navigation_Controller.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Не удалось зарегистрироваться!", Toast.LENGTH_SHORT).show());
    }


}


