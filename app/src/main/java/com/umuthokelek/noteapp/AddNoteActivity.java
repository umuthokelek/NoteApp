package com.umuthokelek.noteapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

// AddNoteActivity, not ekleme ve güncelleme işlemlerini yöneten bir aktivitedir.
public class AddNoteActivity extends AppCompatActivity {

    private EditText titleEditText; // Not başlığı için EditText
    private EditText contentEditText; // Not içeriği için EditText

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        // Toolbar'ı ayarla
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Geri butonunu etkinleştir
        }

        // Toolbar üzerindeki geri butonuna tıklanıldığında aktiviteyi geri al
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // UI bileşenlerini tanımla
        titleEditText = findViewById(R.id.edit_title); // Başlık girişi için EditText
        contentEditText = findViewById(R.id.edit_content); // İçerik girişi için EditText
        Button saveButton = findViewById(R.id.btn_save); // Kaydet butonu

        // Intent ile gelen verileri kontrol et (güncelleme mi yoksa yeni not mu?)
        Intent intent = getIntent();
        boolean isUpdate = intent.hasExtra("id"); // "id" alanı varsa güncelleme işlemi yapılacak

        // Toolbar başlığını ve kaydet butonunun metnini dinamik olarak ayarla
        if (isUpdate) {
            getSupportActionBar().setTitle(R.string.title_edit_note); // Toolbar başlığı: "Not Düzenle"
            saveButton.setText(R.string.btn_save_note); // Kaydet butonunun metni

            // EditText alanlarına mevcut not bilgilerini yükle
            titleEditText.setText(intent.getStringExtra("title"));
            contentEditText.setText(intent.getStringExtra("content"));
        } else {
            getSupportActionBar().setTitle(R.string.title_add_note); // Toolbar başlığı: "Not Ekle"
            saveButton.setText(R.string.btn_save_note); // Kaydet butonunun metni
        }

        // Kaydet butonuna tıklama işlevi
        saveButton.setOnClickListener(v -> {
            String title = titleEditText.getText().toString(); // Başlık girdisini al
            String content = contentEditText.getText().toString(); // İçerik girdisini al

            // Boş başlık kontrolü
            if (title.isEmpty()) {
                Toast.makeText(this, R.string.error_empty_title, Toast.LENGTH_SHORT).show(); // Uyarı mesajı göster
                return;
            }

            // Boş içerik kontrolü
            if (content.isEmpty()) {
                Toast.makeText(this, R.string.error_empty_content, Toast.LENGTH_SHORT).show(); // Uyarı mesajı göster
                return;
            }

            // Sonuç intenti oluştur ve verileri ekle
            Intent resultIntent = new Intent();
            resultIntent.putExtra("title", title);
            resultIntent.putExtra("content", content);

            if (isUpdate) {
                // Güncelleme işleminde "id" bilgisini ekle
                resultIntent.putExtra("id", intent.getIntExtra("id", -1));
                Toast.makeText(this, R.string.success_note_updated, Toast.LENGTH_SHORT).show(); // Güncelleme başarı mesajı
            } else {
                Toast.makeText(this, R.string.success_note_saved, Toast.LENGTH_SHORT).show(); // Kaydetme başarı mesajı
            }

            // Aktivite sonucunu belirle ve bitir
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }
}
