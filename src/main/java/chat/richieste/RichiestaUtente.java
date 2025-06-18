package chat.richieste;

import chat.common.Utente;

public class RichiestaUtente extends RichiestaGenerale {
    // Questa classe serve in modo che il client mandi un oggetto RichiestaUtente al server,
    // il server legge l'utente nella richiesta e usa l'utente in una delle sue funzioni.
    // Per iniziare, implementiamo la RichiestaUtente nel Login.
    private Utente utente;

    public RichiestaUtente(int idUtente) {
        super(TipoRichiesta.richiestaUtente);
    }

    public Utente getUtente(){
        return utente;
    }
}
