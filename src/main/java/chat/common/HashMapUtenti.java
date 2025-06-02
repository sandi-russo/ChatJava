package chat.common;

import java.util.HashMap;
import java.util.Map;

public class HashMapUtenti {
    Map<Integer, Utente> utenti;

    public HashMapUtenti() {
        utenti = new HashMap<>();
    }


    public Map<Integer, Utente> getUtenti() {
        return utenti;
    }

    public void setUtenti(Map<Integer, Utente> utenti) {
        this.utenti = utenti;
    }


    public String stampaHashMap(){
        StringBuilder sb = new StringBuilder();
        sb.append("Stampo gli utenti del DB:\n");

        for(Map.Entry<Integer, Utente> utente : utenti.entrySet()){
            Utente valore = utente.getValue();
            sb.append(valore.getId()).append(" - ")
                    .append(valore.getNome()).append(" ")
                    .append(valore.getCognome()).append(" - ")
                    .append(valore.getEmail()).append("\n");
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Utente u : utenti.values()) {
            sb.append(u.toString()).append("\n");
        }
        return sb.toString();
    }


    public void aggiungiUtente(int id, String nome, String cognome, String email, String password, String avatar){
        utenti.put(id, new Utente(id, nome, cognome, email, password, avatar));
    }
}


