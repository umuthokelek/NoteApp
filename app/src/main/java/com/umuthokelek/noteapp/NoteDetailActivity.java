package com.umuthokelek.noteapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NoteDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        // Toolbar'ı ayarla
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Verileri al ve ekrana yerleştir
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");

        TextView titleTextView = findViewById(R.id.note_detail_title);
        TextView contentTextView = findViewById(R.id.note_detail_content);
        TextView dateTextView = findViewById(R.id.note_detail_date);

        // Başlık ve içeriği yerleştir
        titleTextView.setText(title);
        contentTextView.setText(content);

        long createdAt = intent.getLongExtra("createdAt", 0);
        long updatedAt = intent.getLongExtra("updatedAt", 0);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.getDefault());
        if (updatedAt > createdAt) {
            String dateText = getString(R.string.created_and_edited,
                sdf.format(new Date(createdAt)),
                sdf.format(new Date(updatedAt)));
            dateTextView.setText(dateText);
        } else {
            String dateText = getString(R.string.created_at, sdf.format(new Date(createdAt)));
            dateTextView.setText(dateText);
        }

        // Paylaşım düğmesini ayarla
        FloatingActionButton fabShare = findViewById(R.id.fab_share);
        fabShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_note_title));
            shareIntent.putExtra(Intent.EXTRA_TEXT, title + "\n\n" + content);
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_note_via)));
        });
    }
}
