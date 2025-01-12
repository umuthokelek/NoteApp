package com.umuthokelek.noteapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.appbar.MaterialToolbar;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.Manifest;
import android.content.pm.PackageManager;
import de.hdodenhof.circleimageview.CircleImageView;

// ProfileActivity, kullanıcıların profil bilgilerini görüntülemesini ve düzenlemesini sağlar.
public class ProfileActivity extends AppCompatActivity {

    // Veri erişimi için DAO nesneleri ve iş parçacığı havuzu tanımları
    private ProfileDao profileDao;
    private ExecutorService executorService;
    private SharedPreferences prefs;

    // Görsel seçim işlemleri için sabit değerler
    private static final int PICK_IMAGE_REQUEST = 101;
    private static final int PERMISSION_REQUEST_CODE = 100;

    // Kullanıcı arayüzü bileşenleri
    private EditText nameEdit, phoneEdit, emailEdit, currentPasswordEdit, newPasswordEdit;
    private CircleImageView avatarImage;
    private Uri selectedImageUri;
    private int profileId;
    private boolean isEditMode = false;
    private View passwordContainer;
    private ImageButton changeAvatarButton;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Toolbar yapılandırması
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // SharedPreferences kullanarak kullanıcı oturum bilgilerini alıyoruz
        prefs = getSharedPreferences("NoteApp", MODE_PRIVATE);
        profileId = prefs.getInt("profileId", -1); // Kullanıcının profil ID'si

        // UI bileşenlerini tanımlıyoruz
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

        // Veritabanı ve DAO nesnesi başlatma
        AppDatabase db = AppDatabase.getInstance(this);
        profileDao = db.profileDao();
        executorService = Executors.newSingleThreadExecutor();

        // Profil bilgilerini arka planda yükleme işlemi
        executorService.execute(() -> {
            Profile profile = profileDao.findById(profileId);
            if (profile != null) {
                runOnUiThread(() -> {
                    // Profil bilgilerini UI bileşenlerine yüklüyoruz
                    emailEdit.setText(profile.getEmail());
                    nameEdit.setText(profile.getName());
                    phoneEdit.setText(profile.getPhone());
                    if (profile.getAvatarUri() != null) {
                        loadAvatarImage(Uri.parse(profile.getAvatarUri()));
                    } else {
                        avatarImage.setImageResource(R.drawable.ic_profile_avatar); // Varsayılan avatar resmi
                    }
                });
            }
        });

        // Profil fotoğrafını değiştirme butonuna tıklama işlemi
        changeAvatarButton.setOnClickListener(v -> checkPermissionAndPickImage());

        // Profil kaydetme butonuna tıklama işlemi
        saveButton.setOnClickListener(v -> saveProfile());

        // Çıkış yap butonuna tıklama işlemi
        logoutButton.setOnClickListener(v -> {
            prefs.edit().remove("profileId").apply(); // Oturum bilgisini temizliyoruz
            startActivity(new Intent(this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });

        // Başlangıçta düzenleme modunu kapalı olarak ayarla
        setEditMode(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Menü seçeneklerini yüklüyoruz
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Geri butonuna basıldığında önceki sayfaya dön
            return true;
        } else if (item.getItemId() == R.id.action_edit) {
            setEditMode(!isEditMode); // Düzenleme modunu aç/kapat
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Kullanıcıdan izin isteme ve resim seçimi başlatma işlemleri
    private void checkPermissionAndPickImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
        } else {
            openImagePicker();
        }
    }

    // Görsel seçici açma işlemi
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Resim seçmek için izin gerekli!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                saveImageToInternalStorage(selectedImageUri);
                loadAvatarImage(selectedImageUri);
            }
        }
    }

    // Profil fotoğrafını dahili depolamaya kaydetme işlemi
    private void saveImageToInternalStorage(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            File directory = new File(getFilesDir(), "profile_images");
            if (!directory.exists()) directory.mkdirs();

            File file = new File(directory, "profile_" + profileId + ".jpg");
            try (FileOutputStream fos = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            }

            executorService.execute(() -> {
                Profile profile = profileDao.findById(profileId);
                if (profile != null) {
                    profile.setAvatarUri(Uri.fromFile(file).toString());
                    profileDao.update(profile);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Profil fotoğrafını yükleme işlemi
    private void loadAvatarImage(Uri uri) {
        try {
            avatarImage.setImageURI(uri);
        } catch (Exception e) {
            avatarImage.setImageResource(R.drawable.ic_profile_avatar);
        }
    }

    // Profil bilgilerini kaydetme işlemi
    private void saveProfile() {
        executorService.execute(() -> {
            Profile profile = profileDao.findById(profileId);
            if (profile != null) {
                profile.setName(nameEdit.getText().toString());
                profile.setPhone(phoneEdit.getText().toString());
                profileDao.update(profile);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Profil güncellendi!", Toast.LENGTH_SHORT).show();
                    setEditMode(false);
                });
            }
        });
    }

    // Düzenleme modunu açma veya kapatma işlemi
    private void setEditMode(boolean enabled) {
        isEditMode = enabled;
        nameEdit.setEnabled(enabled);
        phoneEdit.setEnabled(enabled);
        currentPasswordEdit.setEnabled(enabled);
        newPasswordEdit.setEnabled(enabled);
        changeAvatarButton.setVisibility(enabled ? View.VISIBLE : View.GONE);
        passwordContainer.setVisibility(enabled ? View.VISIBLE : View.GONE);
        saveButton.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }
}
