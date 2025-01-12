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

// NoteDetailActivity, bir notun detaylarını görüntülemek ve paylaşmak için kullanılan bir aktivitedir.
public class NoteDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        // Toolbar'ı ayarla
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Geri butonunu etkinleştir
        }

        // Toolbar'daki geri butonuna tıklama işlevi
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Intent'ten gelen verileri al
        Intent intent = getIntent();
        String title = intent.getStringExtra("title"); // Notun başlığı
        String content = intent.getStringExtra("content"); // Notun içeriği

        long createdAt = intent.getLongExtra("createdAt", 0); // Notun oluşturulma zamanı
        long updatedAt = intent.getLongExtra("updatedAt", 0); // Notun güncellenme zamanı

        // UI bileşenlerini bul
        TextView titleTextView = findViewById(R.id.note_detail_title); // Not başlığı TextView'i
        TextView contentTextView = findViewById(R.id.note_detail_content); // Not içeriği TextView'i
        TextView dateTextView = findViewById(R.id.note_detail_date); // Notun tarihi TextView'i

        // Başlık ve içeriği ekrana yerleştir
        titleTextView.setText(title);
        contentTextView.setText(content);

        // Tarih bilgilerini formatlayarak ekrana yerleştir
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.getDefault());
        if (updatedAt > createdAt) {
            // Eğer not güncellenmişse, oluşturulma ve güncellenme tarihlerini göster
            String dateText = getString(R.string.created_and_edited,
                    sdf.format(new Date(createdAt)),
                    sdf.format(new Date(updatedAt)));
            dateTextView.setText(dateText);
        } else {
            // Eğer not güncellenmemişse sadece oluşturulma tarihini göster
            String dateText = getString(R.string.created_at, sdf.format(new Date(createdAt)));
            dateTextView.setText(dateText);
        }

        // Paylaşım düğmesini bul ve tıklama işlevi ekle
        FloatingActionButton fabShare = findViewById(R.id.fab_share);
        fabShare.setOnClickListener(v -> {
            // Paylaşma intenti oluştur
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_note_title)); // Konu başlığı
            shareIntent.putExtra(Intent.EXTRA_TEXT, title + "\n\n" + content); // Paylaşılacak içerik

            // Paylaşma işlemi için uygun bir uygulama seçici aç
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_note_via)));
        });
    }
}
