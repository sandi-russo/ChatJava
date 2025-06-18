package chat.richieste;

import chat.common.Chat;
import chat.common.Utente;

public class RichiestaChat extends RichiestaGenerale{
    private Chat chat;

    public RichiestaChat(Chat chat) {
        super(TipoRichiesta.richiestaChat);
        this.chat = chat;
    }

    public Chat getChat(){
        return chat;
    }
}
