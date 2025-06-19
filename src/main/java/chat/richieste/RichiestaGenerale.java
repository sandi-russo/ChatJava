package chat.richieste;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class RichiestaGenerale implements IRichiesta, Serializable {
    public final TipoRichiesta tipo;

    private static final Logger logger = LoggerFactory.getLogger(RichiestaGenerale.class);


    // Il client usa "out" che è ObjectOutputStream questa è settata per andare al clienthanlder, che lo gestisce singolarmente
    // quindi il clienthanlder sa già l'utente che gli sta mandando la richiesta.
    public RichiestaGenerale(TipoRichiesta tipo) {
        this.tipo = tipo;
    }

    @Override
    public TipoRichiesta getTipo() {
        return tipo;
    }

    public void inviaRichiesta(ObjectOutputStream out) throws IOException {
        try {
                out.reset();
                out.writeObject(this);
                out.flush();
        } catch (IOException e) {
            logger.error("Errore nell'invio della richiesta: {}", e.getMessage());
        }
    }
}
