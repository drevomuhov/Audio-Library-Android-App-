package com.example.audiolibrary;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.audiolibrary.Navigation.Navigation_Controller;
import com.google.firebase.auth.FirebaseAuth;


public class SplashScreen extends AppCompatActivity {

    // Покдлючение модулей Firebase
    private FirebaseAuth authentication = FirebaseAuth.getInstance();


    private ImageView splashscreen_image;
    private final int SPLASH_SCREEN_DURATION = 4500;
    private ObjectAnimator animation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        init();
        startLogoAnimation();
        startSplashScreenTimer();

    }


    // Метод вызывается для подключения объектов
    private void init () {
        splashscreen_image = findViewById(R.id.splashscreen_image);
    }


    // Метод вызывается для запуска анимации логотипа на экране Splash Screen
    private void startLogoAnimation () {
        animation = ObjectAnimator.ofFloat(splashscreen_image, "alpha", 0f, 1f);
        animation.setDuration(2000);
        animation.start();
    }


    // Метод вызывается для запуска таймера показа экрана Splash Screen
    private void startSplashScreenTimer () {
        new Handler().postDelayed(() -> {
            Intent intent = authentication.getCurrentUser() != null
                    ? new Intent(SplashScreen.this, Navigation_Controller.class)
                    : new Intent(SplashScreen.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, SPLASH_SCREEN_DURATION);
    }

}