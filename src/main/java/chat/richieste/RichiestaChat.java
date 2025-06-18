package chat.richieste;

import chat.common.Chat;
import chat.common.Utente;

import java.io.Serializable;

public class RichiestaChat extends RichiestaGenerale implements Serializable {
    private int idChat;

    public RichiestaChat(int idChat) {
        super(TipoRichiesta.richiestaChat);
        this.idChat = idChat;
    }

    public int getIdChat(){
        return idChat;
    }
}
