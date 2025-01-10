package com.umuthokelek.noteapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Index;
import androidx.room.ForeignKey;

import java.io.Serializable;

@Entity(tableName = "notes", 
        indices = {@Index("profileId")},
        foreignKeys = @ForeignKey(
            entity = Profile.class,
            parentColumns = "id",
            childColumns = "profileId",
            onDelete = ForeignKey.CASCADE
        ))
public class Note implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String content;
    private int profileId;
    private long createdAt;
    private long updatedAt;

    public Note(String title, String content, int profileId) {
        this.title = title;
        this.content = content;
        this.profileId = profileId;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getter ve Setter
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
