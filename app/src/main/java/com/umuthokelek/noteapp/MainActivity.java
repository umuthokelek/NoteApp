package com.umuthokelek.noteapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MainActivity extends AppCompatActivity {
    private NoteAdapter adapter;
    private NoteDao noteDao;
    private ExecutorService executorService = Executors.newSingleThreadExecutor(); // Arka plan iş parçacığı
    private List<Note> notes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar'ı ayarla
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Veritabanını başlat
        AppDatabase db = AppDatabase.getInstance(this);
        noteDao = db.noteDao();

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Adapteri ayarla
        adapter = new NoteAdapter(notes, this, new NoteAdapter.OnNoteActionListener() {
            @Override
            public void onUpdate(Note note) {
                // Not düzenleme işlemi için AddNoteActivity'yi başlat
                Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
                intent.putExtra("id", note.getId());
                intent.putExtra("title", note.getTitle());
                intent.putExtra("content", note.getContent());
                startActivityForResult(intent, 2); // Güncelleme işlemi için requestCode = 2
            }

            @Override
            public void onDelete(Note note) {
                new MaterialAlertDialogBuilder(MainActivity.this)
                        .setTitle("Delete Note")
                        .setMessage("Are you sure you want to delete this note?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            executorService.execute(() -> {
                                noteDao.delete(note);
                                notes.remove(note);
                                runOnUiThread(() -> adapter.notifyDataSetChanged());
                            });
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .show();
            }


            @Override
            public void onNoteClick(Note note) {
                // NoteDetailActivity'yi başlat
                Intent intent = new Intent(MainActivity.this, NoteDetailActivity.class);
                intent.putExtra("title", note.getTitle());
                intent.putExtra("content", note.getContent());
                startActivity(intent); // Detay ekranına geçiş
            }
        });

        recyclerView.setAdapter(adapter);

        // Yeni not ekleme işlemi
        FloatingActionButton fab = findViewById(R.id.fab_add_note);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
            startActivityForResult(intent, 1); // Yeni not ekleme için requestCode = 1
        });

        // Veritabanından notları yükle
        executorService.execute(() -> {
            notes.clear();
            notes.addAll(noteDao.getAllNotes());
            runOnUiThread(() -> adapter.notifyDataSetChanged());
        });
    }

    // Ekleme ve güncelleme işlemlerini tamamla
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String title = data.getStringExtra("title");
            String content = data.getStringExtra("content");

            if (requestCode == 1) { // Yeni not ekleme
                Note note = new Note(title, content);
                executorService.execute(() -> {
                    noteDao.insert(note);
                    notes.clear();
                    notes.addAll(noteDao.getAllNotes());
                    runOnUiThread(() -> adapter.notifyDataSetChanged());
                });
            } else if (requestCode == 2) { // Güncelleme
                int id = data.getIntExtra("id", -1);
                Note updatedNote = new Note(title, content);
                updatedNote.setId(id);

                executorService.execute(() -> {
                    noteDao.update(updatedNote);
                    notes.clear();
                    notes.addAll(noteDao.getAllNotes());
                    runOnUiThread(() -> adapter.notifyDataSetChanged());
                });
            }
        }
    }
}
