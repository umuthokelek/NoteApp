package com.umuthokelek.noteapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Profile sınıfı, Room tarafından "profile" adlı bir tablo olarak temsil edilir.
@Entity(tableName = "profile")
public class Profile {

    @PrimaryKey(autoGenerate = true) // Otomatik artan birincil anahtar
    private int id;
    private String email; // Kullanıcının email adresi
    private String password; // Kullanıcının şifresi
    private String name; // Kullanıcının adı (opsiyonel)
    private String phone; // Kullanıcının telefon numarası (opsiyonel)
    private String avatarUri; // Kullanıcının profil resmi için URI (opsiyonel)

    // Yapıcı metod: Email ve şifre ile yeni bir profil oluşturur
    public Profile(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getter ve Setter metodları: Profil alanlarına erişim ve değiştirme işlemleri için kullanılır
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAvatarUri() { return avatarUri; }
    public void setAvatarUri(String avatarUri) { this.avatarUri = avatarUri; }
}
