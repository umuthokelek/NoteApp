package com.umuthokelek.noteapp;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

// RoomDatabase sınıfı, uygulamanın veritabanını temsil eder ve veritabanı işlemleri için bir giriş noktası sağlar.
@Database(
        entities = {Profile.class, Note.class}, // Veritabanında kullanılacak tablolar (Entity sınıfları)
        version = 2 // Veritabanı versiyonu (Her güncellemede artırılmalı)
)
public abstract class AppDatabase extends RoomDatabase {

    // DAO'lar: Veritabanı işlemlerini gerçekleştirmek için kullanılan nesneler
    public abstract NoteDao noteDao();
    public abstract ProfileDao profileDao();

    // AppDatabase sınıfının tek bir örneğini tutmak için static bir alan
    private static AppDatabase instance;

    // Veritabanı versiyonu 1'den 2'ye geçiş için migrasyon işlemleri
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // "notes" tablosuna iki yeni sütun ekle: createdAt ve updatedAt
            database.execSQL("ALTER TABLE notes ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE notes ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0");

            // Mevcut notlar için oluşturulma ve güncellenme tarihlerini şimdiki zamana ayarla
            database.execSQL("UPDATE notes SET createdAt = " + System.currentTimeMillis() +
                    ", updatedAt = " + System.currentTimeMillis());
        }
    };

    // Singleton tasarımı: Veritabanı nesnesini bir kez oluşturur ve tüm uygulama boyunca aynı nesneyi kullanır
    public static AppDatabase getInstance(Context context) {
        if (instance == null) { // Eğer instance null ise yeni bir tane oluştur
            synchronized (AppDatabase.class) { // Çoklu iş parçacıklarında güvenlik için senkronizasyon
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "note_app.db") // Veritabanı adı: note_app.db
                            .addMigrations(MIGRATION_1_2) // Migrasyon işlemini ekle
                            .build(); // Veritabanını oluştur
                }
            }
        }
        return instance; // Mevcut instance'ı döndür
    }
}
