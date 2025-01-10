package com.umuthokelek.noteapp;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Profile.class, Note.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract NoteDao noteDao();
    public abstract ProfileDao profileDao();

    private static AppDatabase instance;

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE notes ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE notes ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0");
            // Mevcut notlar için şimdiki zamanı ata
            database.execSQL("UPDATE notes SET createdAt = " + System.currentTimeMillis() + 
                           ", updatedAt = " + System.currentTimeMillis());
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "note_app.db")
                            .addMigrations(MIGRATION_1_2)  // Migrasyon ekle
                            .build();
                }
            }
        }
        return instance;
    }
}
