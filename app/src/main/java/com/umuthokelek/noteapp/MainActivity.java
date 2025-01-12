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
    private NoteAdapter adapter; // Notları görüntülemek için RecyclerView adapter
    private NoteDao noteDao; // Veritabanı işlemleri için DAO nesnesi
    private ExecutorService executorService = Executors.newSingleThreadExecutor(); // Arka planda çalışan bir iş parçacığı havuzu
    private List<Note> notes = new ArrayList<>(); // Ekranda gösterilecek notların listesi
    private int currentProfileId; // Mevcut kullanıcının profil kimliği

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Intent ile gelen profil kimliğini alıyoruz
        currentProfileId = getIntent().getIntExtra("profileId", -1);
        if (currentProfileId == -1) {
            // Eğer profil kimliği bulunamazsa LoginActivity'ye yönlendir
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Toolbar'ı tanımlayıp ayarlıyoruz
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Veritabanını ve DAO'yu başlatıyoruz
        AppDatabase db = AppDatabase.getInstance(this);
        noteDao = db.noteDao();

        // RecyclerView'i tanımlayıp layout yöneticisini ayarlıyoruz
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Adapter'i oluşturup notlarla ilişkilendiriyoruz
        adapter = new NoteAdapter(notes, this, new NoteAdapter.OnNoteActionListener() {
            @Override
            public void onUpdate(Note note) {
                // Not güncellemek için AddNoteActivity'yi başlat
                Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
                intent.putExtra("id", note.getId());
                intent.putExtra("title", note.getTitle());
                intent.putExtra("content", note.getContent());
                startActivityForResult(intent, 2); // Güncelleme için requestCode = 2
            }

            @Override
            public void onDelete(Note note) {
                // Not silmeden önce bir onay penceresi göster
                new MaterialAlertDialogBuilder(MainActivity.this)
                        .setTitle(R.string.dialog_delete_note_title) // Dialog başlığı
                        .setMessage(R.string.dialog_delete_note_message) // Dialog mesajı
                        .setPositiveButton(R.string.dialog_yes, (dialog, which) -> {
                            // Onaylandığında notu sil
                            executorService.execute(() -> {
                                noteDao.delete(note); // Veritabanından notu sil
                                notes.remove(note); // Listedeki notu çıkar
                                runOnUiThread(() -> adapter.notifyDataSetChanged()); // RecyclerView'i güncelle
                            });
                        })
                        .setNegativeButton(R.string.dialog_cancel, null) // İptal butonu
                        .show();
            }

            @Override
            public void onNoteClick(Note note) {
                // Not detaylarını görüntülemek için NoteDetailActivity'yi başlat
                Intent intent = new Intent(MainActivity.this, NoteDetailActivity.class);
                intent.putExtra("title", note.getTitle());
                intent.putExtra("content", note.getContent());
                intent.putExtra("createdAt", note.getCreatedAt()); // Oluşturulma tarihi
                intent.putExtra("updatedAt", note.getUpdatedAt()); // Güncellenme tarihi
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(adapter); // RecyclerView'e adapter'i bağla

        // Yeni not eklemek için FAB (FloatingActionButton) işlemi
        FloatingActionButton fab = findViewById(R.id.fab_add_note);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
            startActivityForResult(intent, 1); // Yeni not için requestCode = 1
        });

        // Veritabanından notları yükle
        executorService.execute(() -> {
            notes.clear(); // Mevcut not listesini temizle
            notes.addAll(noteDao.getAllNotesForProfile(currentProfileId)); // Profil ID'sine göre notları yükle
            runOnUiThread(() -> adapter.notifyDataSetChanged()); // RecyclerView'i güncelle
        });
    }

    // AddNoteActivity'den gelen sonuçları işlemek için kullanılır
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String title = data.getStringExtra("title"); // Yeni veya güncellenen notun başlığı
            String content = data.getStringExtra("content"); // Yeni veya güncellenen notun içeriği

            if (requestCode == 1) { // Yeni not ekleme
                Note note = new Note(title, content, currentProfileId);
                executorService.execute(() -> {
                    noteDao.insert(note); // Yeni notu veritabanına ekle
                    notes.clear();
                    notes.addAll(noteDao.getAllNotesForProfile(currentProfileId)); // Notları yeniden yükle
                    runOnUiThread(() -> adapter.notifyDataSetChanged()); // RecyclerView'i güncelle
                });
            } else if (requestCode == 2) { // Not güncelleme
                int id = data.getIntExtra("id", -1); // Güncellenecek notun ID'si

                // Güncellenecek notun listede hangi pozisyonda olduğunu bul
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
                    updatedNote.setUpdatedAt(System.currentTimeMillis()); // Yeni güncelleme tarihi belirle

                    executorService.execute(() -> {
                        noteDao.update(updatedNote); // Güncellenmiş notu veritabanına kaydet
                        notes.clear();
                        notes.addAll(noteDao.getAllNotesForProfile(currentProfileId)); // Notları yeniden yükle
                        runOnUiThread(() -> adapter.notifyDataSetChanged()); // RecyclerView'i güncelle
                    });
                }
            }
        }
    }

    // Menü seçeneklerini yükler
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // Menüdeki seçeneklere tıklama işlemleri
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent); // Profil ekranını başlat
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
