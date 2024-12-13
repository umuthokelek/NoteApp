package com.umuthokelek.noteapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

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
        EditText titleEditText = findViewById(R.id.edit_note_title);
        EditText contentEditText = findViewById(R.id.edit_note_content);
        Button saveButton = findViewById(R.id.btn_save_note);

        // Gelen verileri kontrol et
        Intent intent = getIntent();
        boolean isUpdate = intent.hasExtra("id");

        // Toolbar başlığını ve buton metnini dinamik olarak ayarla
        if (isUpdate) {
            getSupportActionBar().setTitle("Update Note");
            saveButton.setText("Update"); // Buton metnini "Update" yap
            titleEditText.setText(intent.getStringExtra("title"));
            contentEditText.setText(intent.getStringExtra("content"));
        } else {
            getSupportActionBar().setTitle("Add Note");
            saveButton.setText("Add"); // Buton metnini "Save" yap
        }

        // Kaydet butonu tıklama işlevi
        saveButton.setOnClickListener(v -> {
            String title = titleEditText.getText().toString();
            String content = contentEditText.getText().toString();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("title", title);
            resultIntent.putExtra("content", content);

            if (isUpdate) {
                resultIntent.putExtra("id", intent.getIntExtra("id", -1));
            }

            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }


}
