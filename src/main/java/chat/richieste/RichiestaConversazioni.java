package chat.richieste;

import chat.common.Chat;
import chat.common.Conversazione;
import javafx.collections.ObservableList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RichiestaConversazioni extends RichiestaGenerale implements Serializable {
    // ma questa roba a che serve? conversazione
    private Conversazione conversazione;
    // L'id viene preso diretttamente dal ClientHandler quindi da qui si potrebbe rimuovere
    private List<Conversazione> conversazioni;
    private int idUtenteRichiedente;

    public RichiestaConversazioni() {
        super(TipoRichiesta.richiestaConversazioni);
        this.conversazioni = new ArrayList<>(); //inizializza la lista vuota
        this.idUtenteRichiedente = -1;
    }

    public RichiestaConversazioni(int idUtente) {
        super(TipoRichiesta.richiestaConversazioni);
        this.idUtenteRichiedente = idUtente;
        this.conversazioni = new ArrayList<>();
    }


    public RichiestaConversazioni(ObservableList<Conversazione> conversazioniFX){
        super(TipoRichiesta.richiestaConversazioni);
        // converto ObservableList in ArrayList per la serializzazione
        this.conversazioni = new ArrayList<>(conversazioniFX);

    }

    // costruttore per il server quando invia le conversazioni come List
    public RichiestaConversazioni(List<Conversazione> conversazioniList) {
        super(TipoRichiesta.richiestaConversazioni);
        this.conversazioni = conversazioniList;
        this.idUtenteRichiedente = -1;
    }

    public List<Conversazione> getConversazioni() {
        return conversazioni;
    }

    public int getIdUtenteRichiedente() {
        return idUtenteRichiedente;
    }

    // Metodo per impostare la lista delle conversazioni (usato dal server prima di inviare)
    public void setConversazioni(List<Conversazione> conversazioni) {
        this.conversazioni = conversazioni;
    }

}
