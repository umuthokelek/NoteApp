package com.umuthokelek.noteapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NoteDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        TextView titleTextView = findViewById(R.id.note_detail_title);
        TextView contentTextView = findViewById(R.id.note_detail_content);

        // Gelen verileri al
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");

        // Verileri ekrana yerleştir
        titleTextView.setText(title);
        contentTextView.setText(content);

        // Paylaşma işlevi
        FloatingActionButton shareButton = findViewById(R.id.fab_share);
        shareButton.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Title: " + title + "\nContent: " + content);
            startActivity(Intent.createChooser(shareIntent, "Share Note"));
        });
    }
}
