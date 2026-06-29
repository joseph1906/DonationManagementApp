package com.donationapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.donationmanagementapp.R;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 1800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, com.donationapp.activity.MainActivity.class));
            finish();
        }, SPLASH_DELAY_MS);
    }
}