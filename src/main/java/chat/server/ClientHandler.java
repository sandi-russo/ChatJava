package chat.server;

import chat.common.*;
import chat.db.MySQLManager;
import chat.richieste.*;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import chat.db.GestioneChat;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import chat.db.GestioneUtente;
import chat.utils.XMLConfigLoaderDB;
import javafx.collections.ObservableList;

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

    private final XMLConfigLoaderDB.DBConfig config;
    private final MySQLManager dbManager;

    private File fileAvatarSelezionato; // lo utilizzo per salvare il file scelto dall'utente

    ColorLogger colorLogger = new ColorLogger();

    public ClientHandler(Socket socket, ConcurrentHashMap<Integer, Chat> chatsRef, List<ClientHandler> clientHandlersRef) throws IOException {
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        chats = chatsRef;
        clientHandlers = clientHandlersRef;
        clientHandlers.add(this);

        this.config = XMLConfigLoaderDB.caricaConfigurazione("server.config.xml");
        this.dbManager = new MySQLManager(config.ip, config.porta, config.nomeDB, config.username, config.password);
        // Qui, il server dovrebbe caricare TUTTE le chat dal db. In questo modo se le carica una volta
        // sola e non c'è bisogno di caricarle ogni volta che un client preme qualcosa nell'interfaccia.
        // gestioneChat.caricaTutteLeChat();  // Non so se usare gestioneChat

        // ^Lo faccio da un'altra parte, lo tengo come appunto
    }

    public void run() {
        // Nel server rispondo SOLO se ricevo qualcosa da qualcuno, quindi non mi serve un thread per mandare e uno per
        // ricevere, me ne serve solo uno che fa tutto. Ovviamente uno per ogni thread.

        try {
            // EFFETTUO L'INIZIALIZZAZIONE PRIMA DEL WHILE PERCHÈ DEVONO AVVENIRE SOLO UNA VOLTA
            // IL CLIENT INVIA L'OGGETTO UTENTE E NOI LO LEGGIAMO

            // inizializzazione(); che ha queste due cose sotto

           /*
            utenteConnesso = (Utente) in.readObject();
            idUtente = utenteConnesso.getId();
            */


            // Qui il client si è appena connesso, il server lo ha identificato e adesso gli dovrebbe mandare tutte le sue chat
            // prese dal db.
            // -Il clienthandler chiede al db tutte le chat e tutti i messaggi. Il clienthandler si salva tutto in chats.
            // -Poi manda tutte le chats associate all'utente del client, così il client se le carica.
            // -Quando ha finito aspetta qualcosa da leggere dal client e va avanti con questo codice (readObject sotto).

            // ASCOLTO DEI MESSAGGI, IL THREAD SI METTE IN ASCOLTO E SI "BLOCCA" SOLO SE RICEVE OGGETTI MESSAGGIO
            // questa parte diventa: elaborate request e ogni cosa che c'è all'interno degli if diventa una ulteriore funzione
            // elaboraRichiesta(GeneralRequest) -> aspetta un oggetto di tipo richiesta -> nello switch case controlla il tipo di
            // richiesta -> in base al tipo di richiesta fa partire la funzione corrispondente

            while (true) {


                // oggettoRicevuto diventa richiestaRicevuta
                //Object oggettoRicevuto = in.readObject();

                // Il client invia insieme alla richiesta un messaggio, la richiesta che il client manda deve contenere una di queste cose:
                // se deve mandare informazioni come un messaggio o il suo nome utente, lo deve fare tramite un attributo della classe:
                // -un utente.      RichiestaUtente (invia al server il suo utente e il tipo della richiesta richiestaUtente
                // -un messaggio.   RichiestaMessaggio
                // -una chat.       RichiestaChat
                // -soltanto il tipo di richiesta, nel caso in cui abbia solo bisogno di avere informazioni


                RichiestaGenerale richiesta = messaggioRicevuto();
                elaboraRichiesta(richiesta);

            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Il client " + idUtente + " si è disconnesso o si è verificato un errore " + e.getMessage());
            // IN CASO DI PROBLEMI O DISCONNESSIONE, RIMUOVO IL CLIENT DALLA LISTA
            clientHandlers.remove(this);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void inviaChatAggiornata(Chat chat) {
        try {
            // Senza il reset invia sempre e solo il primo messaggio della chat e non la chat intera!
            out.reset();  // aggiungo questa riga per resettare la cache dell'ObjectOutputStream

            out.writeObject(chat);
            out.flush();
        } catch (IOException e) {
            colorLogger.logError(e.getMessage());
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

    // permette di avere l'accesso concorrente all'oggetto richiesta, in questo modo, la funzione, verrà eseguita
    // da un thread alla volta
    public synchronized void elaboraRichiesta(IRichiesta richiesta) throws SQLException {
        System.out.println("Tipo richiesta: " + richiesta.getTipo());
        switch (richiesta.getTipo()) {
            case richiestaLogin:
                // connessione al db
                gestisciLogin((RichiestaLogin) richiesta);
                // Gli deve ritornare al client true o false per sapere se le sue info sono giuste, così il client si può salvare il suo utente.
                break;

            case richiestaRegistrazioneUtente:
                System.out.println("Sono in ElaboraRichiesta, prima di GestisciRegistrazione");
                gestisciRegistrazione((RichiestaRegistrazioneUtente) richiesta);
                System.out.println("Sono in ElaboraRichiesta, dopo di GestisciRegistrazione");
                break;
            case richiestaChat:
                // questo diventa una richiesta di tipo inviaChatUtente, deve funzionare con ciò che c'è nel db

                // POI IL CLIENT INVIA LA CHAT, LA LEGGIAMO (non sappiamo farci gli affari nostri)
                RichiestaChat richiestaChat = (RichiestaChat) richiesta;
                //Chat chatIniziale = richiestaChat.getChat();

                // ABBIAMO LETTO L'ID, LO DOBBIAMO SALVARE
                //this.activeChatID = chatIniziale.getId();  // ORA IL CLIENTHANDLER SA QUAL È L'UTENTE "ATTIVO"
                this.activeChatID = richiestaChat.getIdChat();

                System.out.printf("Utente %d: %s connesso. Chat iniziale attiva %d\n", idUtente, utenteConnesso.getId(), activeChatID);
                break;
            case richiestaMessaggio:
                // richiesta di tipo messaggioRicevuto, una volta ricevuta fa tutta questa parte ma in una funzione
                RichiestaMessaggio richiestaMessaggio = (RichiestaMessaggio) richiesta;
                Messaggio nuovoMessaggio = richiestaMessaggio.getMessaggio();

                //Messaggio nuovoMessaggio = (Messaggio) oggettoRicevuto;

                // SE RICEVO UN NUOVO MESSAGGIO CONTROLLO SE QUESTA CHAT ESISTE, IN CASO NEGATIVO, LA CREO
                // USEREMO LA FUNZIONE computeIfAbsent perchè è nettamente più sicuro e "thread-safe"
                chats.computeIfAbsent(nuovoMessaggio.getId_chat_destinataria(), chatID -> {
                    System.out.printf("La chatID ", chatID + " non esiste. La sto creando.");
                    return creaChatPerUtente(utenteConnesso, chatID);
                });

                // A QUESTO PUNTO, LA CHAT È STATA SICURAMENTE CREATA
                // UNA VOLTA CHE ARRIVA UN MESSAGGIO DA UN UTENTE VIENE PRESA QUELLA CHAT DALLA LISTA CHATS
                //Chat chatDaModificare = aggiornaChatDB(chats.get(nuovoMessaggio.getId_chat_destinataria()));

                // Dovremmo fare: se la chat NON CONTIENE MESSAGGI, allora fai aggiornaChatDB.
                Chat chatDaModificare = chats.get(nuovoMessaggio.getId_chat_destinataria());
                if (chatDaModificare != null && !chatDaModificare.contieneMessaggi()) {
                    chatDaModificare = aggiornaChatDB(chatDaModificare);
                }

                System.out.println("L'id della chat scelta è: " + chatDaModificare.getId());

                /* LA CHAT CHE ABBIAMO PRESO È UN **RIFERIMENTO** ALLA CHAT DENTRO CHATS, QUINDI NON SERVE RIMETTERE LA CHAT NELLA MAPPA, PERCHÈ
                 * CONCURRENTHASHMAP CONTIENE UN RIFERIMENTO ALL'OGGETTO, **NON** UNA COPIA */

                // AGGIUNGIAMO L'UTENTE ALLA CHAT SE PER QUALCHE MOTIVO NON FOSSE GIÀ PRESENTE
                if (!chatDaModificare.hashmapUtentiContieneUtente(nuovoMessaggio.getId_mittente())) {
                    chatDaModificare.aggiungiUtente(utenteConnesso);
                }

                // Se il messaggio non è vuoto, lo aggiungo alla chat.
                // Questo è importante perché se il messaggio è vuoto allora il server restituisce comunque la chat
                // al client, così quando il client preme su una chat il server gli manda tutta la chat che già ha.
                if (nuovoMessaggio.getTesto() != null) {
                    chatDaModificare.aggiungiMessaggio(nuovoMessaggio);
                    // QUI AGGIUNGI FUNZIONE CHE AGGIUNGE ANCHE IL MESSAGGIO AL DB

                    salvaMessaggioNelDB(nuovoMessaggio);
                }
                // ADESSO PENSIAMO ALL'INOLTRO
                // IL SERVER SI SCORRE TUTTI LA LISTA DI TUTTI GLI UTENTI CHE SONO CONNESSI E CONTROLLA A CHI MANDARE IL MESSAGGIO

                for (ClientHandler clientHandler : clientHandlers) {
                    if (clientHandler.activeChatID == chatDaModificare.getId()) {
                        System.out.printf("Invio aggiornamento della chat %d al client dell'utente %d.\n", chatDaModificare.getId(), clientHandler.getIdUtente());

                        clientHandler.inviaChatAggiornata(chatDaModificare);
                        // Qui,salva i messaggi nel db facendo una query:
                        // -INSERT INTO messaggii VALUES *tutte le info del messaggio appena mandato con il contenuto, l'id della chat a cui è destinato, il mittente, il tipo di messaggio (che sarà solo stringa in questa funzione credo), l'ora a cui è stato mandato*
                        //
                    }
                }
                break;
            case richiestaConversazioni:
                try {
                    RichiestaConversazioni richiestaConversazioni = (RichiestaConversazioni) richiesta;
                    if (richiesta instanceof RichiestaConversazioni) {

                        //gestisciConversazioni((RichiestaConversazioni) richiesta);
                        gestisciConversazioni(richiestaConversazioni);
                        //ritornaChatConosciute(); // DA AGGIUNGERE
                    }

                } catch (ClassCastException e) {
                    String tipo = richiesta.getClass().getSimpleName();
                    throw new ClassCastException("Oggetto ricevuto non è una RichiestaConversazioni. Ma è di tipo: " + tipo);
                }

                //RichiestaChat richiestaChat = (RichiestaChat) richiesta;

                break;

            case richiestaListaUtenti:
                RichiestaListaUtenti richiestaListaUtenti = (RichiestaListaUtenti) richiesta;
                gestisciRichiestaListaUtenti(richiestaListaUtenti);
                break;

            case richiestaNuovaChat:
                RichiestaNuovaChat richiestaNuovaChat = (RichiestaNuovaChat) richiesta;
                gestisciRichiestaNuovaChat(richiestaNuovaChat);
                break;

            case richiestaMembriGruppo:
                RichiestaMembriGruppo richiestaMembri = (RichiestaMembriGruppo) richiesta;
                gestisciRichiestaMembriGruppo(richiestaMembri);
                break;

            default:
                System.out.println("Richiesta non esiste.");
                break;
        }
    }


    private void gestisciLogin(RichiestaLogin richiesta) {
        System.out.println("Questo utente sta tentando il login " + richiesta.getUsername());

        GestioneUtente gestioneUtente = new GestioneUtente(dbManager);

        try {
            Utente utente = gestioneUtente.login(richiesta.getUsername(), richiesta.getPassword());

            // se ha successo, aggiorno lo stato e invio Utente al client
            this.utenteConnesso = utente;
            this.idUtente = utente.getId();
            System.out.println("L'utente si è connesso, " + utente.getUsername());
            inviaRisposta(utente); // invio l'oggetto al client
        } catch (
                GestioneUtente.LoginException e) { // prendo l'eccezione dalla classe GestioneUtente dove mi dà errore se le credenziali sono errate
            System.err.println("Login fallito");
            inviaRisposta("Errore: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Errore SQL: " + e.getMessage());
            inviaRisposta("Errore SQL");
        }
    }


    private void gestisciRegistrazione(RichiestaRegistrazioneUtente richiesta) {
        GestioneUtente gestioneUtente = new GestioneUtente(dbManager);
        Path cartellaDest = Paths.get("dati_server/avatar");

        try {
            Files.createDirectories(cartellaDest);
        } catch (IOException e) {
            colorLogger.logError("Errore cartella avatar" + e.getMessage());
            inviaRisposta("Errore server");
            return;
        }

        String percorsoAvatarDaSalvare = null;

        if (richiesta.getAvatarBytes() != null && richiesta.getAvatarBytes().length > 0) {
            try {
                String est = richiesta.getAvatarOriginalName()
                        .substring(richiesta.getAvatarOriginalName().lastIndexOf('.'));
                String nomeFile = richiesta.getUsername() + "_" + System.currentTimeMillis() + est;
                Path destinazione = cartellaDest.resolve(nomeFile);
                Files.write(destinazione, richiesta.getAvatarBytes());
                percorsoAvatarDaSalvare = destinazione.toString();
            } catch (IOException e) {
                colorLogger.logError("Errore salvataggio avatar" + e.getMessage());
            }
        }

        if (percorsoAvatarDaSalvare == null)
            percorsoAvatarDaSalvare = cartellaDest.resolve("default.png").toString();

        try {
            gestioneUtente.registraUtente(
                    richiesta.getUsername(),
                    richiesta.getNome(),
                    richiesta.getCognome(),
                    richiesta.getEmail(),
                    richiesta.getPassword(),
                    percorsoAvatarDaSalvare
            );
            inviaRisposta(new RichiestaRegistrazioneUtente());
        } catch (SQLException | GestioneUtente.UserRegistrationException e) {
            colorLogger.logError("Errore registrazione utente" + e.getMessage());
            inviaRisposta("Errore registrazione");
        }
    }


    private void gestisciConversazioni(RichiestaConversazioni richiesta) {
        GestioneChat gestioneChat = new GestioneChat(dbManager);
        int idUtenteRichiedente = richiesta.getIdUtenteRichiedente(); // Ottieni l'ID dalla richiesta
        try {

            ObservableList<Conversazione> conversazioniObservable = gestioneChat.getConversazioniPerUtente(idUtenteRichiedente);
            // chatObservable = gestioneChat.getChatPerUtente(idUtenteRichiedente);

            // converto l'observable in un arraylist
            List<Conversazione> conversazioniList = new ArrayList<>(conversazioniObservable);
            //List<Conversazione> chatList = new ArrayList<>(chatObservable);
            colorLogger.logInfo("Trovate " + conversazioniList.size() + " conversazioni per l'utente con ID " + idUtenteRichiedente);

            // Forse queste richieste è meglio cambiarle in "risposte" per non confondere tutto...
            RichiestaConversazioni risposta = new RichiestaConversazioni(conversazioniList);
            inviaRisposta(risposta);
        } catch (SQLException e) {
            inviaRisposta("Errore nel ritorno delle conversazioni");
            throw new RuntimeException(e);
        }
    }

    private Chat aggiornaChatDB(Chat chat) throws SQLException {
        GestioneChat gestioneChat = new GestioneChat(dbManager);
        List<Messaggio> messaggiDaAggiungere = gestioneChat.getMessaggiPerChat(chat.getId());

        for (Messaggio nuovoMessaggio : messaggiDaAggiungere) {
            chat.aggiungiMessaggio(nuovoMessaggio);
        }

        // Aggiungi anche gli utenti della chat
        List<Utente> utentiChat = gestioneChat.getUtentiPerChat(chat.getId());
        for (Utente utente : utentiChat) {
            chat.aggiungiUtente(utente);
        }

        return chat;
    }

    private void inviaRisposta(Object risposta) {
        try {
            out.reset();
            out.writeObject(risposta);
            out.flush();
        } catch (IOException e) {
            System.err.println("Errore: " + e.getMessage());
        }
    }

    public RichiestaGenerale messaggioRicevuto() throws IOException, ClassNotFoundException {
        Object oggettoRicevuto = in.readObject();
        return (RichiestaGenerale) oggettoRicevuto;
    }


    private void salvaMessaggioNelDB(Messaggio messaggio) throws SQLException {
        if (messaggio == null || messaggio.getTesto() == null || messaggio.getTesto().isEmpty()) {
            colorLogger.logError("Tentativo di salvare un messaggio vuoto o nullo nel database");
            return;
        }

        String sql = "INSERT INTO messaggi (chat_id, mittente_id, contenuto, tipo_messaggio) VALUES (?, ?, ?, ?)";

        try (java.sql.Connection conn = dbManager.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, messaggio.getId_chat_destinataria()); // chat_id
            stmt.setInt(2, messaggio.getId_mittente());          // mittente_id
            stmt.setString(3, messaggio.getTesto());             // contenuto
            stmt.setString(4, "testo");                          // tipo_messaggio (default è 'testo')

            int righeInserite = stmt.executeUpdate();
            if (righeInserite > 0) {
                colorLogger.logInfo("Messaggio salvato nel DB con successo");
            } else {
                colorLogger.logError("Nessuna riga inserita");
            }
        } catch (SQLException e) {
            colorLogger.logError("Errore durante il salvataggio del messaggio nel database " + e.getMessage());
            throw e;
        }
    }


    private void gestisciRichiestaListaUtenti(RichiestaListaUtenti richiesta) {
        try {
            GestioneUtente gestioneUtente = new GestioneUtente(dbManager);
            List<Utente> utenti = gestioneUtente.getListaUtenti(richiesta.getFiltroRicerca());

            // Crea la risposta
            RichiestaListaUtenti risposta = new RichiestaListaUtenti(utenti);
            inviaRisposta(risposta);
        } catch (SQLException e) {
            colorLogger.logError("Errore nel recupero della lista utenti: " + e.getMessage());
            inviaRisposta("Errore nel recupero della lista utenti");
        }
    }


    private void gestisciRichiestaNuovaChat(RichiestaNuovaChat richiesta) throws SQLException {
        GestioneChat gestioneChat = new GestioneChat(dbManager);

        try {
            // Crea la nuova chat nel database con il nome del gruppo se applicabile

            // Questa parte di codice serve a non far creare all'utente una chat privata tra due utenti che esiste già
            if ((richiesta.getIdUtenti().size()) == 1 && richiesta.isGruppo() == false) {
                //fai query per capire se nel result set c'è almeno un risultato. Se c'è già un risultato nel resultset
                //allora NON DEVI CREARE LA CHAT. Altrimenti esce dall'if e crea la chat.
                if (gestioneChat.trovaChatPrivata(richiesta.getIdUtenti().getFirst(), richiesta.getIdCreatore()) != null) {
                    colorLogger.logInfo("La chat tra i due utenti è già esistente");
                    return;
                }
            }

            int idChat = gestioneChat.creaNuovaChat(richiesta.isGruppo(), richiesta.getNomeGruppo());
            // Aggiungi l'utente creatore
            gestioneChat.aggiungiUtenteAChat(idChat, richiesta.getIdCreatore());

            // Aggiungi gli altri utenti
            for (int idUtente : richiesta.getIdUtenti()) {
                gestioneChat.aggiungiUtenteAChat(idChat, idUtente);
            }

            // Ottieni la conversazione appena creata
            Conversazione nuovaConversazione = gestioneChat.getConversazionePerId(idChat, richiesta.getIdCreatore());

            // Invia la risposta
            RichiestaNuovaChat risposta = new RichiestaNuovaChat(nuovaConversazione);
            inviaRisposta(risposta);

        } catch (SQLException e) {
            colorLogger.logError("Errore nella creazione della nuova chat " + e.getMessage());
            throw e;
        }
    }

    private void gestisciRichiestaMembriGruppo(RichiestaMembriGruppo richiesta) {
        try {
            GestioneChat gestioneChat = new GestioneChat(dbManager);
            int idChat = richiesta.getIdChat();

            // Ottieni i membri della chat dal database
            List<Utente> utenti = gestioneChat.getUtentiPerChat(idChat);

            // Crea e invia la risposta
            RichiestaMembriGruppo risposta = new RichiestaMembriGruppo(utenti);
            inviaRisposta(risposta);

        } catch (SQLException e) {
            colorLogger.logError("Errore nel recupero dei membri della chat " + e.getMessage());
            inviaRisposta("Errore nel recupero dei membri della chat");
        }
    }
}
