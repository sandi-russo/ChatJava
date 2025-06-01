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


    public void stampaHashMap(){
        System.out.println("Stampo gli utenti del DB:");
        /*
        Nel for, creo una Map che ha come avrà come dati una chiave di tipo Integer e un valore di tipo Utente.
        Ad uno ad uno uso questa mappa per iterare nei dati all'interno di utenti.
        Grazie a questa HashMap possiamo prendere tutti i dati dal db (vedi *1) e manipolarli come vogliamo
         */

        for(Map.Entry<Integer, Utente> utente : utenti.entrySet()){
            Integer chiave = utente.getKey();
            Utente valore = utente.getValue();
            //System.out.println("Chiave: " + chiave + ", Valore: " + valore);
            valore.printlnAllDatiUtente();
        }
    }

    public void aggiungiUtente(int id, String nome, String cognome, String email, String password, String avatar){
        utenti.put(id, new Utente(id, nome, cognome, email, password, avatar));
    }
}


