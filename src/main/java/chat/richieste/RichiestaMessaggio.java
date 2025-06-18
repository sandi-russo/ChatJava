package chat.richieste;
import chat.common.Messaggio;

import java.io.Serializable;

public class RichiestaMessaggio extends RichiestaGenerale implements Serializable {
    private Messaggio messaggio;

    public RichiestaMessaggio(Messaggio messaggio) {
        super(TipoRichiesta.richiestaMessaggio);
        this.messaggio = messaggio;
    }

    public Messaggio getMessaggio(){
        return messaggio;
    }
}
