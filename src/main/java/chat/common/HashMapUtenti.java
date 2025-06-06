package chat.common;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class HashMapUtenti {
    private final Map<Integer, Utente> utenti;

    public HashMapUtenti() {
        utenti = new HashMap<>();
    }

    public Map<Integer, Utente> getUtenti() {
        return utenti;
    }

    public void aggiungiUtente(int id, String username, String nome, String cognome, String email, String avatar, LocalDateTime createdAt) {
        Utente nuovoUtente = new Utente(id, username, nome, cognome, email, avatar, createdAt);
        utenti.put(id, nuovoUtente);
    }

    public void svuota() {
        utenti.clear();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Utente u : utenti.values()) {
            sb.append(u.toString()).append("\n");
        }
        return sb.toString();
    }
}