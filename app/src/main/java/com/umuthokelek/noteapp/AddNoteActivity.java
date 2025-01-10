package com.umuthokelek.noteapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class AddNoteActivity extends AppCompatActivity {

    private EditText titleEditText;
    private EditText contentEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        // Toolbar'ı ayarla
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // UI bileşenlerini tanımla
        titleEditText = findViewById(R.id.edit_title);
        contentEditText = findViewById(R.id.edit_content);
        Button saveButton = findViewById(R.id.btn_save);

        // Gelen verileri kontrol et
        Intent intent = getIntent();
        boolean isUpdate = intent.hasExtra("id");

        // Toolbar başlığını ve buton metnini dinamik olarak ayarla
        if (isUpdate) {
            getSupportActionBar().setTitle(R.string.title_edit_note);
            saveButton.setText(R.string.btn_save_note);
            titleEditText.setText(intent.getStringExtra("title"));
            contentEditText.setText(intent.getStringExtra("content"));
        } else {
            getSupportActionBar().setTitle(R.string.title_add_note);
            saveButton.setText(R.string.btn_save_note);
        }

        // Kaydet butonu tıklama işlevi
        saveButton.setOnClickListener(v -> {
            String title = titleEditText.getText().toString();
            String content = contentEditText.getText().toString();

            // Boş kontrolleri
            if (title.isEmpty()) {
                Toast.makeText(this, R.string.error_empty_title, Toast.LENGTH_SHORT).show();
                return;
            }
            if (content.isEmpty()) {
                Toast.makeText(this, R.string.error_empty_content, Toast.LENGTH_SHORT).show();
                return;
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra("title", title);
            resultIntent.putExtra("content", content);

            if (isUpdate) {
                resultIntent.putExtra("id", intent.getIntExtra("id", -1));
                Toast.makeText(this, R.string.success_note_updated, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.success_note_saved, Toast.LENGTH_SHORT).show();
            }

            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }


}
