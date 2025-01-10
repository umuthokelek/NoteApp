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
import androidx.core.content.pm.PackageInfoCompat;
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

public class ProfileActivity extends AppCompatActivity {
    private ProfileDao profileDao;
    private ExecutorService executorService;
    private SharedPreferences prefs;
    private static final int PICK_IMAGE = 100;
    private static final int PERMISSION_REQUEST = 200;
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

        // Toolbar'ı ayarla
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        prefs = getSharedPreferences("NoteApp", MODE_PRIVATE);
        profileId = prefs.getInt("profileId", -1);

        // View'ları bul
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

        AppDatabase db = AppDatabase.getInstance(this);
        profileDao = db.profileDao();
        executorService = Executors.newSingleThreadExecutor();

        // Profil bilgilerini yükle
        executorService.execute(() -> {
            Profile profile = profileDao.findById(profileId);
            if (profile != null) {
                runOnUiThread(() -> {
                    emailEdit.setText(profile.getEmail());
                    nameEdit.setText(profile.getName());
                    phoneEdit.setText(profile.getPhone());
                    if (profile.getAvatarUri() != null) {
                        String imageUriStr = profile.getAvatarUri();
                        if (imageUriStr.startsWith("/")) {
                            // Dahili depolamadan yükle
                            Bitmap bitmap = BitmapFactory.decodeFile(imageUriStr);
                            if (bitmap != null) {
                                avatarImage.setImageBitmap(bitmap);
                            } else {
                                // Dosya okunamazsa varsayılan ikonu göster
                                avatarImage.setImageResource(R.drawable.ic_profile_avatar);
                            }
                        } else {
                            // URI'den yükle
                            avatarImage.setImageURI(Uri.parse(imageUriStr));
                        }
                    } else {
                        // AvatarUri null ise varsayılan ikonu göster
                        avatarImage.setImageResource(R.drawable.ic_profile_avatar);
                    }
                });
            }
        });

        avatarImage.setOnLongClickListener(v -> {
            if (isEditMode) {
                showResetPhotoDialog();
            }
            return true;
        });

        changeAvatarButton.setOnClickListener(v -> checkPermissionAndPickImage());

        saveButton.setOnClickListener(v -> saveProfile());

        logoutButton.setOnClickListener(v -> {
            prefs.edit().remove("profileId").apply();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Başlangıçta düzenleme modunu kapat
        setEditMode(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_edit) {
            setEditMode(!isEditMode);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setEditMode(boolean enabled) {
        isEditMode = enabled;
        nameEdit.setEnabled(enabled);
        phoneEdit.setEnabled(enabled);
        changeAvatarButton.setVisibility(enabled ? View.VISIBLE : View.GONE);
        passwordContainer.setVisibility(enabled ? View.VISIBLE : View.GONE);
        saveButton.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }

    private void saveProfile() {
        String currentPassword = currentPasswordEdit.getText().toString();
        String newPassword = newPasswordEdit.getText().toString();
        String name = nameEdit.getText().toString();
        String phone = phoneEdit.getText().toString();

        executorService.execute(() -> {
            Profile profile = profileDao.findById(profileId);
            if (profile != null) {
                // Şifre değişikliği varsa kontrol et
                if (!currentPassword.isEmpty()) {
                    if (!currentPassword.equals(profile.getPassword())) {
                        runOnUiThread(() -> {
                            Toast.makeText(this, getString(R.string.error_current_password), Toast.LENGTH_SHORT).show();
                            // Şifre alanlarını temizle
                            currentPasswordEdit.setText("");
                            newPasswordEdit.setText("");
                        });
                        return;
                    }
                    if (!newPassword.isEmpty()) {
                        profile.setPassword(newPassword);
                    }
                }

                // Profili güncelle
                profile.setName(name);
                profile.setPhone(phone);
                if (selectedImageUri != null) {
                    profile.setAvatarUri(selectedImageUri.toString());
                }

                try {
                    profileDao.update(profile);
                    runOnUiThread(() -> {
                        Toast.makeText(this, getString(R.string.success_profile_updated), Toast.LENGTH_SHORT).show();
                        
                        // Düzenleme modunu kapat
                        setEditMode(false);
                        
                        // Şifre alanlarını temizle
                        currentPasswordEdit.setText("");
                        newPasswordEdit.setText("");
                        
                        // selectedImageUri'yi sıfırla
                        selectedImageUri = null;
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, getString(R.string.error_update_failed), Toast.LENGTH_SHORT).show();
                        
                        // Hata durumunda tüm alanları eski haline getir
                        nameEdit.setText(profile.getName());
                        phoneEdit.setText(profile.getPhone());
                        currentPasswordEdit.setText("");
                        newPasswordEdit.setText("");
                        
                        // Profil fotoğrafını eski haline getir
                        if (profile.getAvatarUri() != null) {
                            String imageUriStr = profile.getAvatarUri();
                            if (imageUriStr.startsWith("/")) {
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
                        
                        // selectedImageUri'yi sıfırla
                        selectedImageUri = null;
                    });
                    e.printStackTrace();
                }
            }
        });
    }

    private void checkPermissionAndPickImage() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13 ve üzeri için READ_MEDIA_IMAGES izni
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_REQUEST);
            } else {
                openImagePicker();
            }
        } else {
            // Android 12 ve altı için READ_EXTERNAL_STORAGE izni
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST);
            } else {
                openImagePicker();
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
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
            try {
                selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    // Kalıcı izin al
                    getContentResolver().takePersistableUriPermission(
                        selectedImageUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                    
                    // Bitmap'e dönüştür ve boyutlandır
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                    bitmap = getResizedBitmap(bitmap, 500); // max 500px
                    
                    // Resmi göster
                    avatarImage.setImageBitmap(bitmap);
                    
                    // Uri'yi kaydet
                    String imageUriStr = saveImageToInternalStorage(bitmap);
                    
                    // Profili güncelle
                    executorService.execute(() -> {
                        Profile profile = profileDao.findById(profileId);
                        if (profile != null) {
                            profile.setAvatarUri(imageUriStr);
                            profileDao.update(profile);
                        }
                    });
                }
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.error_image_load), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private String saveImageToInternalStorage(Bitmap bitmap) {
        try {
            // Uygulama özel dizininde bir dosya oluştur
            File directory = new File(getFilesDir(), "profile_images");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Benzersiz bir dosya adı oluştur
            String fileName = "profile_" + profileId + ".jpg";
            File file = new File(directory, fileName);

            // Bitmap'i dosyaya kaydet
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();

            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showResetPhotoDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_reset_photo_title)
            .setMessage(R.string.dialog_reset_photo_message)
            .setPositiveButton(R.string.dialog_yes, (dialog, which) -> {
                resetProfilePhoto();
            })
            .setNegativeButton(R.string.dialog_cancel, null)
            .show();
    }

    private void resetProfilePhoto() {
        // Mevcut fotoğrafı sil
        if (profileId != -1) {
            File directory = new File(getFilesDir(), "profile_images");
            File file = new File(directory, "profile_" + profileId + ".jpg");
            if (file.exists()) {
                file.delete();
            }
        }

        // Varsayılan ikonu ayarla
        avatarImage.setImageResource(R.drawable.ic_profile_avatar);
        
        // Veritabanındaki URI'yi temizle
        executorService.execute(() -> {
            Profile profile = profileDao.findById(profileId);
            if (profile != null) {
                profile.setAvatarUri(null);
                profileDao.update(profile);
                runOnUiThread(() -> 
                    Toast.makeText(this, getString(R.string.success_photo_reset), Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
} 