package chat.common;

import java.io.Serializable;
import java.time.LocalDateTime;
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

    public void stampaHashMap() {
        System.out.println("Stampo gli utenti del DB:");
        /*
        Nel for, creo una Map che avrà come dati una chiave di tipo Integer e un valore di tipo Utente.
        Ad uno ad uno uso questa mappa per iterare nei dati all'interno di utenti.
        Grazie a questa HashMap possiamo prendere tutti i dati dal db (vedi *1) e manipolarli come vogliamo
         */
        for (Map.Entry<Integer, Utente> utente : utenti.entrySet()) {
            //Integer chiave = utente.getKey();
            Utente valore = utente.getValue();
            valore.printlnAllDatiUtente();
        }
    }

    public void aggiungiUtente(int id, String username, String nome, String cognome, String email, String avatar, LocalDateTime createdAt) {
        Utente nuovoUtente = new Utente(id, username, nome, cognome, email, avatar, createdAt);
        utenti.put(id, nuovoUtente);
    }

    public void aggiungiUtente(Utente utente) {
        utenti.put(utente.getId(), utente);
    }

    public boolean contieneUtente(int idUtente) {
        return utenti.containsKey(idUtente);
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