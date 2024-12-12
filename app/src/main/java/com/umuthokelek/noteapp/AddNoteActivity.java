package com.umuthokelek.noteapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class AddNoteActivity extends AppCompatActivity {
    private EditText titleEditText;
    private EditText contentEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        titleEditText = findViewById(R.id.edit_note_title);
        contentEditText = findViewById(R.id.edit_note_content);
        Button saveButton = findViewById(R.id.btn_save_note);

        // Gelen verileri kontrol et
        Intent intent = getIntent();
        boolean isUpdate = intent.hasExtra("id");
        if (isUpdate) {
            titleEditText.setText(intent.getStringExtra("title"));
            contentEditText.setText(intent.getStringExtra("content"));
        }

        // Kaydet düğmesi
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