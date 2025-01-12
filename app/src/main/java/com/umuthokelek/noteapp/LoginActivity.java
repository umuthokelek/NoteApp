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

// LoginActivity, kullanıcıların giriş yapması veya kayıt olması için bir ekran sağlar.
public class LoginActivity extends AppCompatActivity {
    private EditText emailEdit, passwordEdit; // Kullanıcı girişleri için EditText'ler
    private ProfileDao profileDao; // Profile veritabanı işlemleri için DAO nesnesi
    private ExecutorService executorService; // Arka planda çalıştırılan işler için ExecutorService
    private SharedPreferences prefs; // Kullanıcı oturum bilgilerini saklamak için SharedPreferences

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // SharedPreferences ile kaydedilen profil ID'sini kontrol et
        prefs = getSharedPreferences("NoteApp", MODE_PRIVATE);
        int savedProfileId = prefs.getInt("profileId", -1); // Daha önce oturum açılmış mı kontrol et
        Log.d("LoginActivity", "Saved Profile ID: " + savedProfileId);

        executorService = Executors.newSingleThreadExecutor(); // Arka plan işleri için tek iş parçacıklı Executor oluştur

        if (savedProfileId != -1) {
            // Eğer oturum açık bir kullanıcı varsa, direkt MainActivity'ye yönlendir
            startMainActivity(savedProfileId);
            finish(); // LoginActivity'yi sonlandır
            return;
        }

        // Kullanıcı arayüzündeki bileşenleri tanımla
        emailEdit = findViewById(R.id.edit_email); // Email giriş alanı
        passwordEdit = findViewById(R.id.edit_password); // Şifre giriş alanı
        Button loginButton = findViewById(R.id.btn_login); // Giriş yap butonu
        Button registerButton = findViewById(R.id.btn_register); // Kayıt ol butonu

        // Veritabanını başlat ve ProfileDao'yu al
        AppDatabase db = AppDatabase.getInstance(this);
        profileDao = db.profileDao();

        // Giriş yap butonuna tıklama işlemi
        loginButton.setOnClickListener(v -> login());

        // Kayıt ol butonuna tıklama işlemi
        registerButton.setOnClickListener(v -> register());
    }

    // Kullanıcı giriş işlemi
    private void login() {
        String email = emailEdit.getText().toString(); // Kullanıcıdan alınan email
        String password = passwordEdit.getText().toString(); // Kullanıcıdan alınan şifre

        if (email.isEmpty() || password.isEmpty()) {
            // Eğer email veya şifre boşsa uyarı mesajı göster
            Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            // Veritabanında kullanıcı bilgilerini kontrol et
            Profile profile = profileDao.login(email, password);
            runOnUiThread(() -> {
                if (profile != null) {
                    // Giriş başarılı, profil ID'sini kaydet ve MainActivity'yi başlat
                    prefs.edit().putInt("profileId", profile.getId()).apply();
                    startMainActivity(profile.getId());
                    finish();
                } else {
                    // Hatalı giriş, kullanıcıya mesaj göster
                    Toast.makeText(this, "Hatalı email veya şifre", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // Kullanıcı kayıt işlemi
    private void register() {
        String email = emailEdit.getText().toString(); // Kullanıcıdan alınan email
        String password = passwordEdit.getText().toString(); // Kullanıcıdan alınan şifre

        if (email.isEmpty() || password.isEmpty()) {
            // Eğer email veya şifre boşsa uyarı mesajı göster
            Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            // Email'in daha önce kaydedilip kaydedilmediğini kontrol et
            Profile existingProfile = profileDao.findByEmail(email);
            if (existingProfile != null) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Bu email zaten kayıtlı", Toast.LENGTH_SHORT).show());
                return;
            }

            // Yeni bir profil oluştur ve veritabanına ekle
            Profile newProfile = new Profile(email, password);
            profileDao.insert(newProfile);
            Profile profile = profileDao.login(email, password); // Kayıt işlemi sonrası kullanıcıyı otomatik giriş yap
            runOnUiThread(() -> {
                prefs.edit().putInt("profileId", profile.getId()).apply(); // Yeni profil ID'sini kaydet
                startMainActivity(profile.getId()); // Ana ekrana yönlendir
                finish();
            });
        });
    }

    // MainActivity'yi başlat ve profil ID'sini ilet
    private void startMainActivity(int profileId) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("profileId", profileId);
        startActivity(intent);
    }

    // Aktivite sonlandırıldığında ExecutorService'i kapat
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
