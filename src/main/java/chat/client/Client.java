package chat.client;

import chat.client.controller.*;
import chat.common.*;
import chat.richieste.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Questo client riceve e basta, viene mandato tutto tramite UI. Quindi forse va pure rimosso il thread che manda.

public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    private Utente utenteClient;
    private Chat chatAttuale;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private boolean connesso = false;
    private ChatUI chatUI;
    private GestisciClient gestisciClient;
    private HashMap<Integer, Utente> utentiConosciuti;

    // ExecutorService per gestire le operazioni asincrone
    // Creo un pool con un singolo thread per la lettura dei messaggi
    private ExecutorService threadPool = Executors.newSingleThreadExecutor();

    //faccio riferimento al controller di Login
    private Login controlloreLogin;
    private Registrazione controlloreRegistrazione;
    private GeneralUI controlloreGeneralUI;

    public Client(GestisciClient gestisciClient) {
        //this.utenteClient = utente;
        this.utentiConosciuti = new HashMap<>();
        this.gestisciClient = gestisciClient;
        try {
            inizializzaConnessione();
            //ricezioneChatsUtente(); //La funzione legge tante chats quanto il resultset della query, con il for each.
            avviaThreadLettura();
        } catch (IOException e) {
            logger.error("Errore nella connessione al server: {}", e.getMessage());
        }
    }

    private void inizializzaConnessione() throws IOException {
        socket = new Socket("26.13.123.24", 5558);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        // Invio dell'utente al server per identificazione
        //out.writeObject(utenteClient);
        connesso = true;
        logger.info("Connesso al server");
    }

    // il thread di lettura riceve i messaggi dal server (HO APPORTATO ALCUNE MODIFICHE E HO LASCIATO LE VECCHIE COSE PERCHÈ
    // NON SO SE LE VOGLIAMO MODIFICARE !!!!! DA FARE VEDERE AD ANTONIO)
    private void avviaThreadLettura() {
        threadPool.submit(() -> {
            Chat nuovaChat;
            try {
                while (connesso) {
                    Object ricevuto = in.readObject();
                    elaboraOggettoRicevuto(ricevuto);
                }
            } catch (IOException | ClassNotFoundException e) {
                if (connesso) {
                    logger.error("Errore nella ricezione dei messaggi: {}", e.getMessage(), e);
                    disconnetti();
                }
            }
        });
    }

    public void elaboraOggettoRicevuto(Object ricevuto){
        switch (ricevuto) {
            case Utente utente -> {
                if (controlloreLogin != null) {
                    controlloreLogin.gestisciLoginConSuccesso(utente);
                }
            }
            case String messaggioErrore -> {
                if (controlloreLogin != null) {
                    controlloreLogin.gestisciLoginFallito(messaggioErrore);
                } else if (controlloreRegistrazione != null) {
                    controlloreRegistrazione.gestisciRegistrazioneFallita(messaggioErrore);
                } else if (controlloreGeneralUI != null) {
                    controlloreGeneralUI.gestisciConversazioneFallito(messaggioErrore);
                }
            }
            case RichiestaRegistrazioneUtente ignored -> {
                controlloreRegistrazione.gestisciRegistrazioneConSuccesso();
            }
            case Chat chat -> {
                chatAttuale = chat;

                // AGGIUNGI TUTTI GLI UTENTI DELLA CHAT ALLA MAPPA
                if (chat.getUtenti() != null) {
                    for (Utente utente : chat.getUtenti().getUtenti().values()) {
                        if (utente != null && utente.getId() != 0) {
                            if (!utentiConosciuti.containsKey(utente.getId())) {
                                utentiConosciuti.put(utente.getId(), utente);
                                logger.info("Aggiunto utente dalla chat: {} (ID: {})", utente.getUsername(), utente.getId());
                            }
                        }
                    }
                }

                // Aggiorna l'interfaccia grafica con i nuovi messaggi
                if (chatUI != null && chatUI.getIdConversazioneAttuale() == chatAttuale.getId()) {
                    List<Messaggio> messaggi = new ArrayList<>(chatAttuale.getMessaggi().values());
                    logger.info("Ricevuti {} messaggi da mostrare nella UI", messaggi.size());
                    chatUI.aggiornaMessaggi(messaggi);
                }

                logger.info("Ricevuta chat con ID {} contenente {} messaggi",
                        chatAttuale.getId(), chatAttuale.getMessaggi().size());
            }
            case RichiestaConversazioni risposta -> {
                if (controlloreGeneralUI != null) {
                    controlloreGeneralUI.gestisciConversazioneConSuccesso(risposta);
                }
            }
            case RichiestaListaUtenti risposta -> {
                logger.info("Ricevuta risposta richiestaListaUtenti con {} utenti",
                        risposta.getUtenti() != null ? risposta.getUtenti().size() : 0);

                if (controlloreGeneralUI != null) {
                    controlloreGeneralUI.gestisciListaUtentiConSuccesso(risposta);
                }
            }
            case RichiestaNuovaChat risposta -> {
                if (controlloreGeneralUI != null) {
                    controlloreGeneralUI.gestisciNuovaChatConSuccesso(risposta);
                }
            }

            case RichiestaMembriGruppo risposta -> {
                logger.info("Ricevuta risposta con {} membri della chat",
                        risposta.getMembri() != null ? risposta.getMembri().size() : 0);
                if (chatUI != null) {
                    chatUI.gestisciMembriGruppoRicevuti(risposta.getMembri());
                }
            }
            default -> {
                System.out.println("Client: l'oggetto ricevuto non è fra quelli che io gestisco");
            }
        }
    }

    public HashMap<Integer, Utente> getUtentiConosciuti() {
        return utentiConosciuti;
    }

    // serve per richiamare il controller di Login, in questo modo, subito il login, mi va a chiamare la classe Login
    public void setControlloreLogin(Login controller) {
        this.controlloreLogin = controller;
    }

    // serve per richiaamre il controller di registrazione dopo che l'utente è stato salvato nel db
    public void setControlloreRegistrazione(Registrazione controller) {
        this.controlloreRegistrazione = controller;
    }

    public void setControlloreGeneralUI(GeneralUI controller) {
        this.controlloreGeneralUI = controller;
    }

    public void inviaMessaggio(Messaggio messaggio) {
        try {
            if (connesso) {
                out.writeObject(messaggio);
                out.flush();
                logger.info("Messaggio inviato al server: {}", messaggio.getTesto());
            }
        } catch (IOException e) {
            logger.error("Errore nell'invio del messaggio: {}", e.getMessage());
        }
    }

    public void inviaRichiestaAlServer(RichiestaGenerale richiesta) throws IOException, ClassNotFoundException {
        if (!connesso) {
            throw new IOException("Non sono riuscito a connettermi al server");
        }

        // se non sono entrato nell'if, mi sono connesso
        richiesta.inviaRichiesta(out);
        logger.info("Richiesta inviata al server: {}", richiesta.getTipo());

    }

    // serve per aprire una chat specifica
    public void attivaChat(int idChat) {
        try {
            RichiestaChat richiestaChat = new RichiestaChat(idChat);
            //chatAttuale = new Chat(idChat);
            out.writeObject(richiestaChat);
            out.flush();
            logger.info("Attivata chat con ID: {}", idChat);
        } catch (IOException e) {
            logger.error("Errore nell'attivazione della chat: {}", e.getMessage());
        }
    }

    // impostiamo il controller dell'interfaccia grafica
    public void setChatUI(ChatUI chatUI) {
        this.chatUI = chatUI;
    }

    public Utente getUtenteDaGestisciClient() {
        return gestisciClient.getUtenteLoggato();
    }

    // disconnetto il client dal server
    public void disconnetti() {
        try {
            connesso = false;
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            threadPool.shutdown();
            logger.info("Disconnesso dal server");
        } catch (IOException e) {
            logger.error("Errore durante la disconnessione: {}", e.getMessage());
        }
    }

    public void aggiornaUtentiConosciuti(List<Chat> chats) {
        for (Chat chat : chats) {
            HashMapUtenti utentiChat = chat.getUtenti();
            for (Utente utente : utentiChat.getUtenti().values()) {
                if (!utentiConosciuti.containsKey(utente.getId())) {
                    utentiConosciuti.put(utente.getId(), utente);
                }
            }
        }
    }

    public void popolaUtentiDaConversazioni(List<Conversazione> conversazioni) {
        if (conversazioni == null) {
            return;
        }
        for (Conversazione conv : conversazioni) {
            Utente altroUtente = conv.getAltroUtente();
            // Aggiungiamo l'utente alla mappa solo se non è nullo e ha un ID valido
            if (altroUtente != null && altroUtente.getId() != 0) {
                if (!utentiConosciuti.containsKey(altroUtente.getId())) {
                    utentiConosciuti.put(altroUtente.getId(), altroUtente);
                    logger.info("Aggiunto utente conosciuto: {} (ID: {})", altroUtente.getUsername(), altroUtente.getId());
                }
            }
        }
    }


    public String getNomeMittente(Messaggio messaggio, HashMap<Integer, Utente> utentiConosciuti, int idUtenteLoggato) {
        logger.info("Cercando mittente per ID: {}", messaggio.getId_mittente());
        logger.info("Utenti conosciuti: {}", utentiConosciuti.keySet());

        if (messaggio.getId_mittente() == idUtenteLoggato) {
            return "Tu";
        }

        Utente mittente = utentiConosciuti.get(messaggio.getId_mittente());
        if (mittente != null) {
            logger.info("Trovato mittente: {}", mittente.getUsername());
            return mittente.getUsername();
        } else {
            logger.warn("Nome mittente non trovato per ID: {}", messaggio.getId_mittente());
            return "Utente " + messaggio.getId_mittente();
        }
    }
}