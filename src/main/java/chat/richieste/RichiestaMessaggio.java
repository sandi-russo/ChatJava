package chat.richieste;
import chat.common.Messaggio;

public class RichiestaMessaggio extends RichiestaGenerale {
    private Messaggio messaggio;

    public RichiestaMessaggio(Messaggio messaggio) {
        super(TipoRichiesta.richiestaMessaggio);
        this.messaggio = messaggio;
    }

    public Messaggio getMessaggio(){
        return messaggio;
    }
}
