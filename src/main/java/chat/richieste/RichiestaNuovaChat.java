package chat.richieste;

import chat.common.Conversazione;

import java.util.List;

public class RichiestaNuovaChat extends RichiestaGenerale {
    private List<Integer> idUtenti;  // Lista degli ID degli utenti da aggiungere alla chat
    private int idCreatore;
    private boolean isGruppo;
    private String nomeGruppo;
    private Conversazione nuovaConversazione;

    public RichiestaNuovaChat(List<Integer> idUtenti, int idCreatore, boolean isGruppo, String nomeGruppo) {
        super(TipoRichiesta.richiestaNuovaChat);
        this.idUtenti = idUtenti;
        this.idCreatore = idCreatore;
        this.isGruppo = isGruppo;
        this.nomeGruppo = nomeGruppo;
    }

    // Costruttore per la risposta dal server al client
    public RichiestaNuovaChat(Conversazione nuovaConversazione) {
        super(TipoRichiesta.richiestaNuovaChat);
        this.nuovaConversazione = nuovaConversazione;
    }

    public List<Integer> getIdUtenti() {
        return idUtenti;
    }

    public int getIdCreatore() {
        return idCreatore;
    }

    public boolean isGruppo() {
        return isGruppo;
    }

    public String getNomeGruppo() {
        return nomeGruppo;
    }

    public Conversazione getNuovaConversazione() {
        return nuovaConversazione;
    }
}
