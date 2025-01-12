package com.umuthokelek.noteapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Index;
import androidx.room.ForeignKey;

import java.io.Serializable;

// Note sınıfı, Room veritabanında "notes" adında bir tablo olarak temsil edilir.
@Entity(
        tableName = "notes", // Tablo adı
        indices = {@Index("profileId")}, // profileId alanına indeks eklenerek sorgular hızlandırılır.
        foreignKeys = @ForeignKey(
                entity = Profile.class, // Bağlantılı tablo: Profile tablosu
                parentColumns = "id", // Profile tablosunun birincil anahtar sütunu
                childColumns = "profileId", // Bu tablodaki foreign key sütunu
                onDelete = ForeignKey.CASCADE // Profile silindiğinde ilgili notlar da silinir.
        )
)
public class Note implements Serializable {
    @PrimaryKey(autoGenerate = true) // Otomatik artan birincil anahtar
    private int id;

    private String title; // Notun başlığı
    private String content; // Notun içeriği
    private int profileId; // Bu notun hangi profile ait olduğunu belirten foreign key
    private long createdAt; // Notun oluşturulma zamanı (timestamp olarak)
    private long updatedAt; // Notun güncellenme zamanı (timestamp olarak)

    // Constructor: Yeni bir not oluşturulurken kullanılır
    public Note(String title, String content, int profileId) {
        this.title = title;
        this.content = content;
        this.profileId = profileId;
        this.createdAt = System.currentTimeMillis(); // Oluşturulma zamanı olarak şimdiki zaman atanır
        this.updatedAt = System.currentTimeMillis(); // Güncelleme zamanı da başlangıçta şimdiki zaman olarak atanır
    }

    // Getter ve Setter metodları: Note nesnesinin alanlarına erişim ve değiştirme için kullanılır
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
