package chat.richieste;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class RichiestaRegistrazioneUtente extends RichiestaGenerale {
    private String username;
    private String nome;
    private String cognome;
    private String email;
    private String password;
    private final byte[] avatarBytes;
    private final String avatarOriginalName;

    // Usata quando il client invia la richiesta
    public RichiestaRegistrazioneUtente(String username, String nome, String cognome, String email, String password, File avatar) throws IOException {
        super(TipoRichiesta.richiestaRegistrazioneUtente);
        this.username = username;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.password = password;

        if (avatar != null) {
            this.avatarBytes = Files.readAllBytes(avatar.toPath());
            this.avatarOriginalName = avatar.getName();
        } else {
            this.avatarBytes = null;
            this.avatarOriginalName = null;
        }

    }

    // In questo modo possiamo usarla come risposta del server al client, non ha bisogno di info al suo interno perché
    // al client basta riconoscere che è stata ricevuta con successo
    public RichiestaRegistrazioneUtente() {
        super(TipoRichiesta.richiestaRegistrazioneUtente);
        this.username = this.nome = this.cognome = this.email = this.password = null;
        this.avatarBytes = null;
        this.avatarOriginalName = null;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
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

    public byte[] getAvatarBytes() {
        return avatarBytes;
    }

    public String getAvatarOriginalName() {
        return avatarOriginalName;
    }
}
