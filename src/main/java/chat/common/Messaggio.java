package chat.common;
import java.time.Instant;

/*
    Creare una data, poi come renderla un timestamp nel db:
    Instant now = Instant.now();
    Timestamp sqlTimestamp = Timestamp.from(now);
    System.out.println(sqlTimestamp);

 */

public class Messaggio {
    private int id;
    private int id_mittente;
    private int id_chat_destinataria;
    private Instant ora;


}
