package chat.client;

import chat.client.controller.*;
import chat.common.*;
import chat.richieste.*;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Questo client riceve e basta, viene mandato tutto tramite UI. Quindi forse va pure rimosso il thread che manda.

public class Client {
    ColorLogger colorLogger = new ColorLogger();
    // private Utente utenteClient;
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
            colorLogger.logError("Errore nella connessione al server: " + e.getMessage());
        }
    }

    private void inizializzaConnessione() throws IOException {
        socket = new Socket("26.13.123.24", 5558);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        // Invio dell'utente al server per identificazione
        //out.writeObject(utenteClient);
        connesso = true;
        colorLogger.logInfo("Connesso al server");
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
                    colorLogger.logError("Errore nella ricezione dei messaggi " + e.getMessage());
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
            case RichiestaRegistrazioneUtente ignored -> controlloreRegistrazione.gestisciRegistrazioneConSuccesso();
            case Chat chat -> {
                chatAttuale = chat;

                // AGGIUNGI TUTTI GLI UTENTI DELLA CHAT ALLA MAPPA
                if (chat.getUtenti() != null) {
                    for (Utente utente : chat.getUtenti().getUtenti().values()) {
                        if (utente != null && utente.getId() != 0) {
                            if (!utentiConosciuti.containsKey(utente.getId())) {
                                utentiConosciuti.put(utente.getId(), utente);
                            }
                        }
                    }
                }

                // Aggiorna l'interfaccia grafica con i nuovi messaggi
                if (chatUI != null && chatUI.getIdConversazioneAttuale() == chatAttuale.getId()) {
                    List<Messaggio> messaggi = new ArrayList<>(chatAttuale.getMessaggi().values());
                    chatUI.aggiornaMessaggi(messaggi);
                }

                colorLogger.logInfo("Ricevuta chat con ID " + chatAttuale.getId() + " contenente " + chatAttuale.getMessaggi().size() + " messaggi");
            }
            case RichiestaConversazioni risposta -> {
                if (controlloreGeneralUI != null) {
                    controlloreGeneralUI.gestisciConversazioneConSuccesso(risposta);
                }
            }
            case RichiestaListaUtenti risposta -> {

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
                if (chatUI != null) {
                    chatUI.gestisciMembriGruppoRicevuti(risposta.getMembri());
                }
            }
            default -> System.out.println("Client: l'oggetto ricevuto non è fra quelli che io gestisco");
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


    public void inviaRichiestaAlServer(RichiestaGenerale richiesta) throws IOException, ClassNotFoundException {
        if (!connesso) {
            throw new IOException("Non sono riuscito a connettermi al server");
        }

        // se non sono entrato nell'if, mi sono connesso
        richiesta.inviaRichiesta(out);
        colorLogger.logInfo("Richiesta inviata al server: " + richiesta.getTipo());

    }

    // serve per aprire una chat specifica
    public void attivaChat(int idChat) {
        try {
            RichiestaChat richiestaChat = new RichiestaChat(idChat);
            //chatAttuale = new Chat(idChat);
            out.writeObject(richiestaChat);
            out.flush();
        } catch (IOException e) {
            colorLogger.logError("Errore nella ricezione della chat: " + e.getMessage());
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
            colorLogger.logInfo("Disconnesso dal server");
        } catch (IOException e) {
            colorLogger.logError("Errore durante la disconnessione: " + e.getMessage());
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
                }
            }
        }
    }


    public String getNomeMittente(Messaggio messaggio, HashMap<Integer, Utente> utentiConosciuti, int idUtenteLoggato) {

        if (messaggio.getId_mittente() == idUtenteLoggato) {
            return "Tu";
        }

        Utente mittente = utentiConosciuti.get(messaggio.getId_mittente());
        if (mittente != null) {
            return mittente.getUsername();
        } else {
            colorLogger.logError("Nome mittente non trovato non trovato" + messaggio.getId_mittente());
            return "Utente " + messaggio.getId_mittente();
        }
    }
}