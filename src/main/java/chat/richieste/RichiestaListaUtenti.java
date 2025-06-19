package chat.richieste;

import chat.richieste.TipoRichiesta;
import chat.common.Utente;

import java.util.List;

public class RichiestaListaUtenti extends RichiestaGenerale {
    private String filtroRicerca;
    private List<Utente> utenti;

    // Costruttore per la richiesta dal client al server
    public RichiestaListaUtenti(String filtroRicerca) {
        super(TipoRichiesta.richiestaListaUtenti);
        this.filtroRicerca = filtroRicerca;
    }

    // Costruttore per la risposta dal server al client
    public RichiestaListaUtenti(List<Utente> utenti) {
        super(TipoRichiesta.richiestaListaUtenti);
        this.utenti = utenti;
    }

    public String getFiltroRicerca() {
        return filtroRicerca;
    }

    public List<Utente> getUtenti() {
        return utenti;
    }
}