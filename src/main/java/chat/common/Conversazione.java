package chat.common;

// Questa classe la utilizzo solamente per contenere i dati di una singola conversazione da mostrare nella lista
public class Conversazione {
    private final int idChat;
    private final String nomeVisualizzato;
    private final Utente altroUtente;

    public Conversazione(int idChat, String nomeVisualizzato, Utente altroUtente) {
        this.idChat = idChat;
        this.nomeVisualizzato = nomeVisualizzato;
        this.altroUtente = altroUtente;
    }

    public int getIdChat() { return idChat; }
    public String getNomeVisualizzato() { return nomeVisualizzato; }
    public Utente getAltroUtente() { return altroUtente; }

    // questo metodo lo sfrutta la ListView per mostrare i nomi nella lista
    @Override
    public String toString() {
        return nomeVisualizzato;
    }
}
