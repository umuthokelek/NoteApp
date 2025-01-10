package com.umuthokelek.noteapp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface ProfileDao {
    @Query("SELECT * FROM profile WHERE email = :email AND password = :password LIMIT 1")
    Profile login(String email, String password);

    @Query("SELECT * FROM profile WHERE email = :email LIMIT 1")
    Profile findByEmail(String email);

    @Query("SELECT * FROM profile WHERE id = :id LIMIT 1")
    Profile findById(int id);

    @Insert
    void insert(Profile profile);

    @Update
    void update(Profile profile);
} 