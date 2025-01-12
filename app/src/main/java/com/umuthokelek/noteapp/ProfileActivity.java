package com.umuthokelek.noteapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import java.io.File;
import java.io.FileOutputStream;
import de.hdodenhof.circleimageview.CircleImageView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

// ProfileActivity sınıfı, kullanıcıların profillerini görüntüleyip düzenleyebileceği bir ekran sağlar.
public class ProfileActivity extends AppCompatActivity {
    private ProfileDao profileDao; // Profile işlemleri için DAO
    private ExecutorService executorService; // Arka plan görevlerini çalıştırmak için ExecutorService
    private SharedPreferences prefs; // Kullanıcı oturum bilgilerini saklamak için SharedPreferences
    private static final int PICK_IMAGE = 100; // Görsel seçimi için sabit kod
    private static final int PERMISSION_REQUEST = 200; // İzin talebi için sabit kod
    private EditText nameEdit, phoneEdit, emailEdit, currentPasswordEdit, newPasswordEdit; // Giriş alanları
    private CircleImageView avatarImage; // Profil fotoğrafını göstermek için özel bir ImageView
    private Uri selectedImageUri; // Seçilen görselin URI'si
    private int profileId; // Kullanıcının profil kimliği
    private boolean isEditMode = false; // Düzenleme modunun açık/kapalı durumu
    private View passwordContainer; // Şifre değişikliği alanını temsil eden görünüm
    private ImageButton changeAvatarButton; // Profil fotoğrafını değiştirme butonu
    private Button saveButton; // Kaydet butonu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Toolbar'ı yapılandır
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Kullanıcı oturum bilgilerini al
        prefs = getSharedPreferences("NoteApp", MODE_PRIVATE);
        profileId = prefs.getInt("profileId", -1);

        // View'ları bul ve tanımla
        nameEdit = findViewById(R.id.edit_name);
        phoneEdit = findViewById(R.id.edit_phone);
        emailEdit = findViewById(R.id.edit_email);
        currentPasswordEdit = findViewById(R.id.edit_current_password);
        newPasswordEdit = findViewById(R.id.edit_new_password);
        avatarImage = findViewById(R.id.profile_avatar);
        saveButton = findViewById(R.id.btn_save);
        Button logoutButton = findViewById(R.id.btn_logout);
        changeAvatarButton = findViewById(R.id.btn_change_avatar);
        passwordContainer = findViewById(R.id.password_container);

        // Veritabanı ve DAO nesnelerini başlat
        AppDatabase db = AppDatabase.getInstance(this);
        profileDao = db.profileDao();
        executorService = Executors.newSingleThreadExecutor();

        // Profil bilgilerini yükle
        executorService.execute(() -> {
            Profile profile = profileDao.findById(profileId);
            if (profile != null) {
                runOnUiThread(() -> {
                    // Profil bilgilerini UI bileşenlerine yükle
                    emailEdit.setText(profile.getEmail());
                    nameEdit.setText(profile.getName());
                    phoneEdit.setText(profile.getPhone());

                    // Profil fotoğrafını yükle
                    if (profile.getAvatarUri() != null) {
                        String imageUriStr = profile.getAvatarUri();
                        if (imageUriStr.startsWith("/")) {
                            // Dosya sistemi üzerinden yükleme
                            Bitmap bitmap = BitmapFactory.decodeFile(imageUriStr);
                            if (bitmap != null) {
                                avatarImage.setImageBitmap(bitmap);
                            } else {
                                avatarImage.setImageResource(R.drawable.ic_profile_avatar);
                            }
                        } else {
                            avatarImage.setImageURI(Uri.parse(imageUriStr));
                        }
                    } else {
                        avatarImage.setImageResource(R.drawable.ic_profile_avatar);
                    }
                });
            }
        });

        // Profil fotoğrafı değiştirme butonuna tıklama işlemi
        changeAvatarButton.setOnClickListener(v -> checkPermissionAndPickImage());

        // Kaydet butonuna tıklama işlemi
        saveButton.setOnClickListener(v -> saveProfile());

        // Çıkış yap butonuna tıklama işlemi
        logoutButton.setOnClickListener(v -> {
            prefs.edit().remove("profileId").apply(); // Oturum bilgisini sil
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent); // LoginActivity'ye yönlendir
            finish(); // ProfileActivity'yi kapat
        });

        // Başlangıçta düzenleme modunu kapat
        setEditMode(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu); // Menü seçeneklerini yükle
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Geri butonu tıklanınca geri git
            return true;
        } else if (item.getItemId() == R.id.action_edit) {
            setEditMode(!isEditMode); // Düzenleme modunu değiştir
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setEditMode(boolean enabled) {
        // Düzenleme modunu etkinleştir veya devre dışı bırak
        isEditMode = enabled;
        nameEdit.setEnabled(enabled);
        phoneEdit.setEnabled(enabled);
        changeAvatarButton.setVisibility(enabled ? View.VISIBLE : View.GONE);
        passwordContainer.setVisibility(enabled ? View.VISIBLE : View.GONE);
        saveButton.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }

    private void saveProfile() {
        // Profil bilgilerini kaydetme işlemi
        String currentPassword = currentPasswordEdit.getText().toString();
        String newPassword = newPasswordEdit.getText().toString();
        String name = nameEdit.getText().toString();
        String phone = phoneEdit.getText().toString();

        executorService.execute(() -> {
            Profile profile = profileDao.findById(profileId);
            if (profile != null) {
                // Şifre doğrulama
                if (!currentPassword.isEmpty()) {
                    if (!currentPassword.equals(profile.getPassword())) {
                        runOnUiThread(() -> {
                            Toast.makeText(this, getString(R.string.error_current_password), Toast.LENGTH_SHORT).show();
                            currentPasswordEdit.setText("");
                            newPasswordEdit.setText("");
                        });
                        return;
                    }
                    if (!newPassword.isEmpty()) {
                        profile.setPassword(newPassword);
                    }
                }

                // Diğer bilgileri güncelle
                profile.setName(name);
                profile.setPhone(phone);
                if (selectedImageUri != null) {
                    profile.setAvatarUri(selectedImageUri.toString());
                }

                // Profili veritabanına kaydet
                try {
                    profileDao.update(profile);
                    runOnUiThread(() -> {
                        Toast.makeText(this, getString(R.string.success_profile_updated), Toast.LENGTH_SHORT).show();
                        setEditMode(false); // Düzenleme modunu kapat
                        currentPasswordEdit.setText("");
                        newPasswordEdit.setText("");
                        selectedImageUri = null;
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(this, getString(R.string.error_update_failed), Toast.LENGTH_SHORT).show());
                    e.printStackTrace();
                }
            }
        });
    }

    private void checkPermissionAndPickImage() {
        // Kullanıcıdan izin al ve görsel seçimini başlat
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST);
        } else {
            openImagePicker();
        }
    }

    private void openImagePicker() {
        // Görsel seçmek için bir intent başlat
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, getString(R.string.error_permission_required), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            avatarImage.setImageURI(selectedImageUri); // Seçilen görseli göster
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown(); // ExecutorService'i kapat
    }
}
