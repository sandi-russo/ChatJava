package chat.common;

/*
    Creare una data, poi come renderla un timestamp nel db:
    Instant now = Instant.now();
    Timestamp sqlTimestamp = Timestamp.from(now);
    System.out.println(sqlTimestamp);

 */

import java.time.Instant;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

// Il messaggio deve implementare Serializable per poter mandare messaggi tramite socket
public class Messaggio implements Serializable {
    private int id;
    private int id_mittente;
    private int id_chat_destinataria;
    private String testo;
    private Instant ora;

    // Quando i client mandano un messaggio non sanno quale id avrà quel messaggio nella chat conviene creare
    // questo nuovo costruttore così i client usano questo, poi il server assegna al messaggio un id.
    public Messaggio(String testo, int mittente, int chat_destinataria) {
        this.testo = testo;
        this.id_mittente = mittente;
        this.id_chat_destinataria = chat_destinataria;
        this.ora = Instant.now();
    }

    // In questo costruttore String = null. Quindi può essere usato per mandare un messaggio vuoto.
    public Messaggio(int mittente, int chat_destinataria) {
        this.id_mittente = mittente;
        this.id_chat_destinataria = chat_destinataria;
        this.ora = Instant.now();
    }

    public Messaggio(String testo, int mittente, int chat_destinataria, Instant ora) {
        this.testo = testo;
        this.id_mittente = mittente;
        this.id_chat_destinataria = chat_destinataria;
        this.ora = ora;
    }

    public int getId() {
        return id;
    }

    public int getId_mittente() {
        return id_mittente;
    }

    public int getId_chat_destinataria() {
        return id_chat_destinataria;
    }

    public String getTesto() {
        return testo;
    }

    // la utilizzo per stampare l'ora nel messaggio nel formato ora:minuti:secondi
    public String getOraFormattata() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy - HH:mm").withZone(ZoneId.systemDefault());
        return formatter.format(ora);
    }

    // Serve al server per assegnare un id al messaggio inviato dal client, visto che i client non sanno quale
    // sarà l'id del messaggio quando lo inviano.
    public void setId(int id) {
        this.id = id;
    }
}


