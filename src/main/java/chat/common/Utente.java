package chat.common;

import java.io.Serializable;
import java.time.LocalDateTime;


public class Utente {
    private final int id;
    private String username;
    private final String nome;
    private final String cognome;
    private final String email;
    private final String avatar; // Percorso dell'immagine
    private final LocalDateTime createdAt;

    public Utente(int id, String username, String nome, String cognome, String email, String avatar, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.avatar = avatar;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

    public String getEmail() {
        return email;
    }

    public String getAvatar() {
        return avatar;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return username + " (" + nome + " " + cognome + ")";
    }


}
