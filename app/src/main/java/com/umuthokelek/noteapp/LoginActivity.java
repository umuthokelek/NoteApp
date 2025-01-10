package com.umuthokelek.noteapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEdit, passwordEdit;
    private ProfileDao profileDao;
    private ExecutorService executorService;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefs = getSharedPreferences("NoteApp", MODE_PRIVATE);
        int savedProfileId = prefs.getInt("profileId", -1);
        Log.d("LoginActivity", "Saved Profile ID: " + savedProfileId);
        
        executorService = Executors.newSingleThreadExecutor();

        if (savedProfileId != -1) {
            startMainActivity(savedProfileId);
            finish();
            return;
        }

        emailEdit = findViewById(R.id.edit_email);
        passwordEdit = findViewById(R.id.edit_password);
        Button loginButton = findViewById(R.id.btn_login);
        Button registerButton = findViewById(R.id.btn_register);

        AppDatabase db = AppDatabase.getInstance(this);
        profileDao = db.profileDao();

        loginButton.setOnClickListener(v -> login());
        registerButton.setOnClickListener(v -> register());
    }

    private void login() {
        String email = emailEdit.getText().toString();
        String password = passwordEdit.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            Profile profile = profileDao.login(email, password);
            runOnUiThread(() -> {
                if (profile != null) {
                    prefs.edit().putInt("profileId", profile.getId()).apply();
                    startMainActivity(profile.getId());
                    finish();
                } else {
                    Toast.makeText(this, "Hatalı email veya şifre", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void register() {
        String email = emailEdit.getText().toString();
        String password = passwordEdit.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            Profile existingProfile = profileDao.findByEmail(email);
            if (existingProfile != null) {
                runOnUiThread(() -> 
                    Toast.makeText(this, "Bu email zaten kayıtlı", Toast.LENGTH_SHORT).show());
                return;
            }

            Profile newProfile = new Profile(email, password);
            profileDao.insert(newProfile);
            Profile profile = profileDao.login(email, password);
            runOnUiThread(() -> {
                prefs.edit().putInt("profileId", profile.getId()).apply();
                startMainActivity(profile.getId());
                finish();
            });
        });
    }

    private void startMainActivity(int profileId) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("profileId", profileId);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
} 