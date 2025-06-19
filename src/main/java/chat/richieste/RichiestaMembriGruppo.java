package chat.richieste;

import chat.common.Utente;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RichiestaMembriGruppo extends RichiestaGenerale implements Serializable {
    private int idChat;
    private List<Utente> membri;

    public RichiestaMembriGruppo(int idChat) {
        super(TipoRichiesta.richiestaMembriGruppo);
        this.idChat = idChat;
        this.membri = new ArrayList<>();
    }

    public RichiestaMembriGruppo(List<Utente> membri) {
        super(TipoRichiesta.richiestaMembriGruppo);
        this.membri = membri;
    }

    public int getIdChat() {
        return idChat;
    }

    public List<Utente> getMembri() {
        return membri;
    }

    @Override
    public void inviaRichiesta(ObjectOutputStream out) throws IOException {
        out.writeObject(this);
        out.flush();
    }

}
