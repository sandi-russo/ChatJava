package chat.client;

import chat.client.controller.ChatUI;
import chat.common.Chat;
import chat.common.Messaggio;
import chat.common.Utente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    private Utente utenteClient;
    private Chat chatAttuale;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private boolean connesso = false;
    private ChatUI chatUI;

    // ExecutorService per gestire le operazioni asincrone
    // Creo un pool con un singolo thread per la lettura dei messaggi
    private ExecutorService threadPool = Executors.newSingleThreadExecutor();

    public Client(Utente utente) {
        this.utenteClient = utente;
        try {
            inizializzaConnessione();
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
        out.writeObject(utenteClient);
        connesso = true;
        logger.info("Connesso al server come {}", utenteClient.getUsername());
    }

    // il thread di lettura riceve i messaggi dal server
    private void avviaThreadLettura() {
        threadPool.submit(() -> {
            Chat nuovaChat;
            try {
                while (connesso) {
                    Object ricevuto = in.readObject();
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
                }
            } catch (IOException | ClassNotFoundException e) {
                if (connesso) {
                    logger.error("Errore nella ricezione dei messaggi: {}", e.getMessage(), e);
                    disconnetti();
                }
            }
        });
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

    // serve per aprire una chat specifica
    public void attivaChat(int idChat) {
        try {
            chatAttuale = new Chat(idChat);
            out.writeObject(chatAttuale);
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