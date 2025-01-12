package com.umuthokelek.noteapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// NoteAdapter, RecyclerView için bir adaptördür ve Note nesnelerini listelemek için kullanılır.
public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private List<Note> notes; // Notların tutulduğu liste
    private Context context; // Adapter'in kullanıldığı bağlam (örneğin, bir Activity)
    private OnNoteActionListener listener; // Kullanıcının notlar üzerinde yaptığı işlemleri dinlemek için bir arayüz

    // Kullanıcının tıklama, güncelleme ve silme işlemlerini yönetmek için bir arayüz
    public interface OnNoteActionListener {
        void onNoteClick(Note note); // Kısa tıklama işlemi

        void onUpdate(Note note); // Not güncelleme işlemi
        void onDelete(Note note); // Not silme işlemi
    }

    // Adapter'in yapıcı metodu
    public NoteAdapter(List<Note> notes, Context context, OnNoteActionListener listener) {
        this.notes = notes;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // item_note.xml layout dosyasını bağlayarak bir ViewHolder oluştur
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        // Not nesnesini al
        Note note = notes.get(position);

        // Başlığı TextView'e ata
        holder.titleTextView.setText(note.getTitle());

        // İçeriği kısalt ve TextView'e ata
        String content = note.getContent();
        if (content.length() > 50) {
            content = content.substring(0, 50) + "..."; // 50 karakterden fazlasını kes ve "..." ekle
        }
        holder.contentTextView.setText(content);

        // Notun oluşturulma veya güncellenme tarihini formatla ve göster
        String dateText;
        if (note.getUpdatedAt() > note.getCreatedAt()) {
            dateText = context.getString(R.string.last_edited, formatDate(note.getUpdatedAt()));
        } else {
            dateText = context.getString(R.string.created_at, formatDate(note.getCreatedAt()));
        }
        holder.dateTextView.setText(dateText);

        // Not detayını açmak için kısa tıklama işlemi
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNoteClick(note); // Kısa tıklamayı dinleyiciye bildir
            }
        });

        // Uzun tıklama: PopupMenu ile güncelleme ve silme seçeneklerini göster
        holder.itemView.setOnLongClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.itemView);
            popup.inflate(R.menu.note_item_menu); // Menü seçeneklerini bağla

            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_edit) {
                    listener.onUpdate(note); // Güncelleme seçeneği
                    return true;
                } else if (itemId == R.id.action_delete) {
                    listener.onDelete(note); // Silme seçeneği
                    return true;
                }
                return false;
            });

            popup.show(); // Menü gösterilir
            return true;
        });
    }

    // Zaman damgasını formatlayarak okunabilir bir tarih string'i oluşturur
    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    @Override
    public int getItemCount() {
        return notes.size(); // Not listesindeki toplam öğe sayısını döndür
    }

    // ViewHolder, RecyclerView öğeleri için görünümleri tutar
    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView; // Not başlığı için TextView
        TextView contentTextView; // Not içeriği için TextView
        TextView dateTextView; // Notun tarihi için TextView

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            // item_note.xml dosyasındaki TextView bileşenlerini bağla
            titleTextView = itemView.findViewById(R.id.note_title);
            contentTextView = itemView.findViewById(R.id.note_content);
            dateTextView = itemView.findViewById(R.id.note_date);
        }
    }
}
