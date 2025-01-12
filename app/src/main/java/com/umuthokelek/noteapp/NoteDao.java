package com.umuthokelek.noteapp;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NoteDao {

    // Belirli bir profile ait notları ID'ye göre sıralayıp döndürür
    @Query("SELECT * FROM notes WHERE profileId = :profileId ORDER BY id DESC")
    List<Note> getAllNotesForProfile(int profileId);

    // Tüm notları ID'ye göre sıralayıp döndürür
    @Query("SELECT * FROM notes ORDER BY id DESC")
    List<Note> getAllNotes();

    // Yeni bir not ekler
    @Insert
    void insert(Note note);

    // Mevcut bir notu günceller
    @Update
    void update(Note note);

    // Mevcut bir notu siler
    @Delete
    void delete(Note note);
}
