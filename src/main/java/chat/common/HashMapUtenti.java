package chat.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class HashMapUtenti implements Serializable {
    private Map<Integer, Utente> utenti;

    public HashMapUtenti() {
        utenti = new HashMap<>();
    }

    public Map<Integer, Utente> getUtenti() {
        return utenti;
    }

    public void setUtenti(Map<Integer, Utente> utenti) {
        this.utenti = utenti;
    }


    public void aggiungiUtente(Utente utente) {
        utenti.put(utente.getId(), utente);
    }

    public boolean contieneUtente(int idUtente) {
        return utenti.containsKey(idUtente);
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