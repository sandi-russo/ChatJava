package chat.client;

import chat.client.controller.*;
import chat.common.Chat;
import chat.common.Messaggio;
import chat.common.Utente;
import chat.richieste.RichiestaChat;
import chat.richieste.RichiestaConversazioni;
import chat.richieste.RichiestaGenerale;
import chat.richieste.RichiestaRegistrazioneUtente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
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
    // ExecutorService per gestire le operazioni asincrone
    // Creo un pool con un singolo thread per la lettura dei messaggi
    private ExecutorService threadPool = Executors.newSingleThreadExecutor();

    //faccio riferimento al controller di Login
    private Login controlloreLogin;
    private Registrazione controlloreRegistrazione;
    private GeneralUI controlloreGeneralUI;

    public Client(GestisciClient gestisciClient) {
        //this.utenteClient = utente;
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
        socket = new Socket("localhost", 5558);
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
                    //System.out.println("Classe effettiva: " + ricevuto.getClass());
                    //System.out.println("È RichiestaRegistrazioneUtente? " + (ricevuto instanceof RichiestaRegistrazioneUtente));
                    //System.out.println("ClassLoader della classe ricevuta: " + ricevuto.getClass().getClassLoader());
                    //System.out.println("ClassLoader della mia classe locale: " + RichiestaRegistrazioneUtente.class.getClassLoader());

                    // se è di tipo conversazione aggiunge una conversazione
                    // se è di tipo chat, aggiunge o modifica una chat
                    // se è di tipo stringa, si vede quale è la String, in base a cosa c'è scritto in quella stringa (esempio: "Risposta: 1 (login effettuato con successo") allora il client reagisce in modo diverso

                    /*
                    if (ricevuto instanceof RichiestaRegistrazioneUtente) {
                        controlloreRegistrazione.gestisciRegistrazioneConSuccesso();
                    }
                    if (ricevuto instanceof Chat) {
                        nuovaChat = (Chat) ricevuto;
                        chatAttuale = nuovaChat;

                        // Aggiorna l'interfaccia grafica con i nuovi messaggi
                        if (chatUI != null && chatUI.getIdConversazioneAttuale() == nuovaChat.getId()) {
                            List<Messaggio> messaggi = new ArrayList<>(nuovaChat.getMessaggi().values());
                            logger.info("Ricevuti {} messaggi da mostrare nella UI", messaggi.size());
                            chatUI.aggiornaMessaggi(messaggi);
                        }

                        logger.info("Ricevuta chat con ID {} contenente {} messaggi",
                                nuovaChat.getId(), nuovaChat.getMessaggi().size());
                    }

                    */

                    /*
                    switch(richiesta.getTipo()){
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
                        default:
                            System.out.println("Richiesta non esiste.");
                            break;
                    }
                    */


                    switch (ricevuto) {
                        case Utente utente -> {
                            if (controlloreLogin != null) {
                                controlloreLogin.gestisciLoginConSuccesso(utente);
                            }
                        }
                        case String messaggioErrore -> {
                            if (controlloreLogin != null) {
                                controlloreLogin.gestisciLoginFallito(messaggioErrore);
                            } else if(controlloreRegistrazione != null) {
                                controlloreRegistrazione.gestisciRegistrazioneFallita(messaggioErrore);
                            } else if(controlloreGeneralUI != null){
                                controlloreGeneralUI.gestisciConversazioneFallito(messaggioErrore);
                            }
                        }
                        case RichiestaRegistrazioneUtente ignored -> {
                            controlloreRegistrazione.gestisciRegistrazioneConSuccesso();
                        }
                        case Chat chat -> {
                            chatAttuale = chat;

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
                        default -> {
                            System.out.println("Client: l'oggetto ricevuto non è fra quelli che io gestisco");
                        }
                    }



                    /*
                    switch (ricevuto){
                        case null -> {
                            System.out.println("Richiesta non esiste.");
                        }
                        case Utente u -> {
                            //Utente utente = (Utente) ricevuto;
                            if (controlloreLogin != null) {
                                controlloreLogin.gestisciLoginConSuccesso(u); // login riuscito e invio il messaggio all'utente
                            }
                        }
                        case String s -> {
                            //String messaggioErrore = (String) ricevuto;

                            if (controlloreLogin != null) {
                                controlloreLogin.gestisciLoginFallito(s); // login fallito, mando un messaggio
                            } else {
                                controlloreRegistrazione.gestisciRegistrazioneFallita(s);
                            }
                        }
                        case
                        }
                    }
                    */

                    // HO MODIFICATO QUESTO
                    /*
                    if (ricevuto instanceof Utente) {
                        Utente utente = (Utente) ricevuto;

                        if (controlloreLogin != null) {
                            controlloreLogin.gestisciLoginConSuccesso(utente); // login riuscito e invio il messaggio all'utente
                        }

                    } else if (ricevuto instanceof String) { // se ricevo una stringa, allora il login è fallito
                        String messaggioErrore = (String) ricevuto;

                        if (controlloreLogin != null) {
                            controlloreLogin.gestisciLoginFallito(messaggioErrore); // login fallito, mando un messaggio
                        } else {
                            controlloreRegistrazione.gestisciRegistrazioneFallita(messaggioErrore);
                        }
                    } else {
                        System.out.println("Client: l'oggetto ricevuto non è fra quelli che io gestisco");
                    }
                */
                }


            } catch (IOException | ClassNotFoundException e) {
                if (connesso) {
                    logger.error("Errore nella ricezione dei messaggi: {}", e.getMessage(), e);
                    disconnetti();
                }
            }
        });
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
}