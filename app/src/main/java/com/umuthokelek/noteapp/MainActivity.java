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
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    private NoteAdapter adapter;
    private NoteDao noteDao;
    private ExecutorService executorService = Executors.newSingleThreadExecutor(); // Arka plan iş parçacığı
    private List<Note> notes = new ArrayList<>();
    private int currentProfileId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentProfileId = getIntent().getIntExtra("profileId", -1);
        if (currentProfileId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

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
                        .setTitle(R.string.dialog_delete_note_title)
                        .setMessage(R.string.dialog_delete_note_message)
                        .setPositiveButton(R.string.dialog_yes, (dialog, which) -> {
                            executorService.execute(() -> {
                                noteDao.delete(note);
                                notes.remove(note);
                                runOnUiThread(() -> adapter.notifyDataSetChanged());
                            });
                        })
                        .setNegativeButton(R.string.dialog_cancel, null)
                        .show();
            }


            @Override
            public void onNoteClick(Note note) {
                // NoteDetailActivity'yi başlat
                Intent intent = new Intent(MainActivity.this, NoteDetailActivity.class);
                intent.putExtra("title", note.getTitle());
                intent.putExtra("content", note.getContent());
                intent.putExtra("createdAt", note.getCreatedAt());  // Oluşturulma tarihini ekle
                intent.putExtra("updatedAt", note.getUpdatedAt());  // Güncellenme tarihini ekle
                startActivity(intent);
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
            notes.addAll(noteDao.getAllNotesForProfile(currentProfileId));
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
                Note note = new Note(title, content, currentProfileId);
                executorService.execute(() -> {
                    noteDao.insert(note);
                    notes.clear();
                    notes.addAll(noteDao.getAllNotesForProfile(currentProfileId));
                    runOnUiThread(() -> adapter.notifyDataSetChanged());
                });
            } else if (requestCode == 2) { // Güncelleme
                int id = data.getIntExtra("id", -1);
                
                // Güncellenecek notun pozisyonunu bul
                int notePosition = -1;
                for (int i = 0; i < notes.size(); i++) {
                    if (notes.get(i).getId() == id) {
                        notePosition = i;
                        break;
                    }
                }

                if (notePosition != -1) {
                    Note updatedNote = new Note(title, content, currentProfileId);
                    updatedNote.setId(id);
                    updatedNote.setCreatedAt(notes.get(notePosition).getCreatedAt()); // Eski oluşturulma tarihini koru
                    updatedNote.setUpdatedAt(System.currentTimeMillis()); // Yeni güncelleme tarihi
                    
                    executorService.execute(() -> {
                        noteDao.update(updatedNote);
                        notes.clear();
                        notes.addAll(noteDao.getAllNotesForProfile(currentProfileId));
                        runOnUiThread(() -> adapter.notifyDataSetChanged());
                    });
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
