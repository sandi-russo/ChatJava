package chat.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Chat implements Serializable {
    private int id;
    private boolean is_group;
    private HashMapUtenti utenti;
    private Map<Integer, Messaggio> messaggi = new LinkedHashMap<>();
    private int contatore = 0;

    // il server ha tutte le informazioni, quindi lui crea la chat tramite questo e poi la manda al client
    public Chat(int id, boolean is_group, HashMapUtenti utenti, Map<Integer, Messaggio> messaggi, int contatore) {
        this.id = id;
        this.is_group = is_group;
        this.utenti = utenti;
        this.messaggi = messaggi;
        this.contatore = contatore;
    }

    // Si presuppone che se stai creando una chat, essa NON ABBIA MESSAGGI e il contatore sia a 0.
    public Chat(int id, boolean is_group, HashMapUtenti utenti, int numero_membri) {
        this.id = id;
        this.is_group = is_group;
        this.utenti = utenti;
        // Per adesso tengo il numero di membri ma mi sembra un dato sempre più inutile... Si può ricavare
        // facilmente dalla lista degli utenti che fanno parte della chat quindi salvarlo non ha senso!
    }

    public Chat(int id, boolean is_group, HashMapUtenti utenti) {
        this.id = id;
        this.is_group = is_group;
        this.utenti = utenti;
    }

    // Questa è solo per testing, mi serve che il client abbia una chat con un id per vedere se riesco a farlo funzionare
    public Chat(int id) {
        this.id = id;
    }

    public synchronized void aggiungiMessaggio(Messaggio m) {
        m.setId(contatore++);
        messaggi.put(contatore++, m);
    }

    public Map<Integer, Messaggio> getMessaggi() {
        return messaggi;
    }

    public int getId() {
        return id;
    }

    public void stampaMessaggi() {
        for (Map.Entry<Integer, Messaggio> entry : messaggi.entrySet()) {
            Messaggio msg = entry.getValue();
            System.out.println("[" + msg.getOraFormattata() + "] " +
                    msg.getId_mittente() + ": " +  // *1 PROBLEMA
                    msg.getTesto());
        }
    }

    public boolean hashmapUtentiContieneUtente(int utenteDaVerificare) {
        return utenti.contieneUtente(utenteDaVerificare);
    }

    public void aggiungiUtente(Utente utente) {
        utenti.aggiungiUtente(utente);
    }

    public HashMapUtenti getUtenti() {
        return utenti; // Dove `utenti` è il tuo HashMap interno
    }

    public boolean contieneMessaggi() {
        return messaggi != null && !messaggi.isEmpty();
    }
}