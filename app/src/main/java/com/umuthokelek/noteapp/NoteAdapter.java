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

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private List<Note> notes;
    private Context context;
    private OnNoteActionListener listener;

    public interface OnNoteActionListener {
        void onNoteClick(Note note); // Kısa tıklama için

        void onUpdate(Note note);
        void onDelete(Note note);
    }

    public NoteAdapter(List<Note> notes, Context context, OnNoteActionListener listener) {
        this.notes = notes;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);

        holder.titleTextView.setText(note.getTitle());

        // İçeriği kısalt
        String content = note.getContent();
        if (content.length() > 50) {
            content = content.substring(0, 50) + "...";
        }
        holder.contentTextView.setText(content);

        // Tarihi formatla ve göster
        String dateText;
        if (note.getUpdatedAt() > note.getCreatedAt()) {
            dateText = context.getString(R.string.last_edited, formatDate(note.getUpdatedAt()));
        } else {
            dateText = context.getString(R.string.created_at, formatDate(note.getCreatedAt()));
        }
        holder.dateTextView.setText(dateText);

        // Kısa tıklama: NoteDetailActivity'yi açmak için
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNoteClick(note); // Kısa tıklamayı dinleyiciye bildir
            }
        });

        // Uzun tıklama: PopupMenu açmak için
        holder.itemView.setOnLongClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.itemView);
            popup.inflate(R.menu.note_item_menu);
            
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_edit) {
                    listener.onUpdate(note);
                    return true;
                } else if (itemId == R.id.action_delete) {
                    listener.onDelete(note);
                    return true;
                }
                return false;
            });
            
            popup.show();
            return true;
        });
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView contentTextView;
        TextView dateTextView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.note_title);
            contentTextView = itemView.findViewById(R.id.note_content);
            dateTextView = itemView.findViewById(R.id.note_date);
        }
    }
}
