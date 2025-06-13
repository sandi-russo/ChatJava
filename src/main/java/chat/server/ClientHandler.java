package chat.server;

import chat.common.Chat;
import chat.common.HashMapUtenti;
import chat.common.Messaggio;
import chat.common.Utente;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class ClientHandler implements Runnable {

    private static ConcurrentHashMap<Integer, Chat> chats;
    private static List<ClientHandler> clientHandlers;

    private ObjectInputStream in;
    private ObjectOutputStream out;

    private Utente utenteConnesso;
    // L'id utente è anche l'id del clientHandler.
    // -1 perché non è stato ancora assegnato un utente.
    private int idUtente = -1;

    private int activeChatID = -1;

    public ClientHandler(Socket socket, ConcurrentHashMap<Integer, Chat> chatsRef, List<ClientHandler> clientHandlersRef) throws IOException {
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        chats = chatsRef;
        clientHandlers = clientHandlersRef;
        clientHandlers.add(this);
    }

    public void run() {
        // Nel server rispondo SOLO se ricevo qualcosa da qualcuno, quindi non mi serve un thread per mandare e uno per
        // ricevere, me ne serve solo uno che fa tutto. Ovviamente uno per ogni thread.

        try {
            // EFFETTUO L'INIZIALIZZAZIONE PRIMA DEL WHILE PERCHÈ DEVONO AVVENIRE SOLO UNA VOLTA
            // IL CLIENT INVIA L'OGGETTO UTENTE E NOI LO LEGGIAMO
            utenteConnesso = (Utente) in.readObject();
            idUtente = utenteConnesso.getId();

            // ASCOLTO DEI MESSAGGI, IL THREAD SI METTE IN ASCOLTO E SI "BLOCCA" SOLO SE RICEVE OGGETTI MESSAGGIO
            while (true) {
                Object oggettoRicevuto = in.readObject();
                if (oggettoRicevuto instanceof Chat) {
                    // POI IL CLIENT INVIA LA CHAT, LA LEGGIAMO (non sappiamo farci gli affari nostri)
                    Chat chatIniziale = (Chat) oggettoRicevuto;

                    // ABBIAMO LETTO L'ID, LO DOBBIAMO SALVARE
                    this.activeChatID = chatIniziale.getId(); // ORA IL CLIENT SA QUAL È L'UTENTE "ATTIVO"

                    System.out.printf("Utente %d: %s connesso. Chat iniziale attiva %d\n", idUtente, utenteConnesso.getId(), activeChatID);
                } else if (oggettoRicevuto instanceof Messaggio) {
                    Messaggio nuovoMessaggio = (Messaggio) oggettoRicevuto;

                    // SE RICEVO UN NUOVO MESSAGGIO CONTROLLO SE QUESTA CHAT ESISTE, IN CASO NEGATIVO, LA CREO
                    // USEREMO LA FUNZIONE comuteIfAbsent perchè è nettamente più sicuro e "thread-safe"
                    chats.computeIfAbsent(nuovoMessaggio.getId_chat_destinataria(), chatID -> {
                        System.out.printf("La chatID ", chatID + " non esiste. La sto creando.");
                        return creaChatPerUtente(utenteConnesso, chatID);
                    });

                    // A QUESTO PUNTO, LA CHAT È STATA SICURAMENTE CREATA
                    // UNA VOLTA CHE ARRIVA UN MESSAGGIO DA UN UTENTE VIENE PRESA QUELLA CHAT DALLA LISTA CHATS
                    Chat chatDaModificare = chats.get(nuovoMessaggio.getId_chat_destinataria());
                    System.out.println("L'id della chat scelta è: " + chatDaModificare.getId());

                    /* LA CHAT CHE ABBIAMO PRESO È UN **RIFERIMENTO** ALLA CHAT DENTRO CHATS, QUINDI NON SERVE RIMETTERE LA CHAT NELLA MAPPA, PERCHÈ
                     * CONCURRENTHASHMAP CONTIENE UN RIFERIMENTO ALL'OGGETTO, **NON** UNA COPIA */

                    // AGGIUNGIAMO L'UTENTE ALLA CHAT SE PER QUALCHE MOTIVO NON FOSSE GIÀ PRESENTE
                    if (!chatDaModificare.hashmapUtentiContieneUtente(nuovoMessaggio.getId_mittente())) {
                        chatDaModificare.aggiungiUtente(utenteConnesso);
                    }

                    // IL MESSAGGIO CHE È APPENA ARRIVATO VIENE AGGIUNTO ALLA CHAT
                    chatDaModificare.aggiungiMessaggio(nuovoMessaggio);

                    // ADESSO PENSIAMO ALL'INOLTRO
                    // IL SERVER SI SCORRE TUTTI LA LISTA DI TUTTI GLI UTENTI CHE SONO CONNESSI E CONTROLLA A CHI MANDARE IL MESSAGGIO

                    for (ClientHandler clientHandler : clientHandlers) {
                        if (clientHandler.activeChatID == chatDaModificare.getId()) {
                            System.out.printf("Invio aggiornamento della chat %d al client dell'utente %d.\n", chatDaModificare.getId(), clientHandler.getIdUtente());
                            clientHandler.inviaChatAggiornata(chatDaModificare);
                        }
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Il client " + idUtente + " si è disconnesso o si è verificato un errore " + e.getMessage());
            // IN CASO DI PROBLEMI O DISCONNESSIONE, RIMUOVO IL CLIENT DALLA LISTA
            clientHandlers.remove(this);
        }
    }

    public void inviaChatAggiornata(Chat chat) {
        try {
            // Senza il reset invia sempre e solo il primo messaggio della chat e non la chat intera!
            out.reset();  // aggiungo questa riga per resettare la cache dell'ObjectOutputStream

            out.writeObject(chat);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getIdUtente() {
        return idUtente;
    }

    public static Chat creaChatPerUtente(Utente utenteCreatore, int idChatDaCreare) {
        // CREIAMO L'HASHMAP DEGLI UTENTI CHE POSSONO SCRIVERE E RICEVERE IN QUESTA CHAT
        HashMapUtenti utentiChat = new HashMapUtenti();

        // L'UTENTE CHE HA CREATO LA CHAT VIENE AGGIUNTO AD ESSA
        utentiChat.aggiungiUtente(utenteCreatore);

        // VIENE CREATO L'OGGETTO CHAT, VIENE PRESO L'ID DAL MESSAGGIO MANDATO DALL'UTENTE E  POI AGGIUNTO ALL'HASHMAP
        Chat chat = new Chat(idChatDaCreare, true, utentiChat);

        // NON METTEREMO NELLA LISTA "CHATS" PERCHÈ LO FARÀ IN AUTOMATICO "COMPUTEIFABSENT"
        return chat;
    }
}



