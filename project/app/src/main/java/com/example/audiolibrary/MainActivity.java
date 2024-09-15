package com.example.audiolibrary;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.audiolibrary.Navigation.Navigation_Controller;
import com.google.firebase.auth.FirebaseAuth;


public class MainActivity extends AppCompatActivity {
    private FirebaseAuth authentication = FirebaseAuth.getInstance();


    ImageView imageView;
    Button next_button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        if (authentication.getCurrentUser() != null) {
            Intent intent = new Intent(MainActivity.this, Navigation_Controller.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }


        next_button.setOnClickListener(view -> {

            BottomSheetSettings bottomSheetSettings = new BottomSheetSettings();
            bottomSheetSettings.show(getSupportFragmentManager(), bottomSheetSettings.getTag());

        });

    }

    public void init () {
        imageView = findViewById(R.id.predictBlock_user_image);
        next_button = findViewById(R.id.next_button);
    }

    
}
