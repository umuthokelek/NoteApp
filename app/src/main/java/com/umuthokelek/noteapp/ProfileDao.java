package com.umuthokelek.noteapp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

// ProfileDao, Room veritabanı ile Profile tablosu üzerinde işlem yapmak için kullanılan bir Data Access Object (DAO).
@Dao
public interface ProfileDao {

    // Kullanıcının giriş yapması için email ve şifreyi kontrol eden sorgu
    @Query("SELECT * FROM profile WHERE email = :email AND password = :password LIMIT 1")
    Profile login(String email, String password); // Eğer eşleşen bir profil varsa, ilkini döndürür

    // Veritabanında email'e göre bir profil arayan sorgu
    @Query("SELECT * FROM profile WHERE email = :email LIMIT 1")
    Profile findByEmail(String email); // Belirtilen email adresine sahip profili döndürür (tek sonuç)

    // ID'ye göre bir profil arayan sorgu
    @Query("SELECT * FROM profile WHERE id = :id LIMIT 1")
    Profile findById(int id); // Belirtilen ID'ye sahip profili döndürür (tek sonuç)

    // Yeni bir profil eklemek için kullanılan yöntem
    @Insert
    void insert(Profile profile); // Profil nesnesini veritabanına ekler

    // Mevcut bir profili güncellemek için kullanılan yöntem
    @Update
    void update(Profile profile); // Profil nesnesini günceller
}
