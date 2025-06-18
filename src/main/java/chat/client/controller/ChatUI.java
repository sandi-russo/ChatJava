package chat.client.controller;

import chat.common.Chat;
import chat.common.Conversazione;
import chat.common.Messaggio;
import chat.common.Utente;
import chat.richieste.RichiestaMessaggio;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ChatUI {
    private static final Logger logger = LoggerFactory.getLogger(ChatUI.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    private BorderPane PannelloPrincipale;
    @FXML
    private ListView<Messaggio> ListaMessaggi;
    @FXML
    private TextField CellaMessaggio;
    @FXML
    private Button btnInvio;
    @FXML
    private ImageView chatAvatar;
    @FXML
    private Label chatNome;
    private Conversazione conversazioneAttuale;
    private Utente utenteLoggato;
    private List<Messaggio> messaggiAttuali = new ArrayList<>();
    private chat.client.Client clientChat;

    public void initialize() {
        logger.info("Inizializzazione Interfaccia Grafica");

        this.clientChat = GestisciClient.getInstance().getClientChat();

        // Nascondi il pannello all'inizio
        if (PannelloPrincipale != null) {
            PannelloPrincipale.setVisible(false);
            PannelloPrincipale.managedProperty().bind(PannelloPrincipale.visibleProperty());
            logger.info("Pannello chat inizialmente nascosto");
        }

        // Configurazione del bottone di invio
        if (btnInvio != null) {
            btnInvio.setOnAction(event -> inviaMessaggio());
        }

        // Permette di inviare il messaggio premendo Enter
        if (CellaMessaggio != null) {
            CellaMessaggio.setOnAction(event -> inviaMessaggio());
        }

        if (ListaMessaggi != null) {
            configuraCellFactory();
        }
    }

    // configuro lo stile dei messaggi
    private void configuraCellFactory() {
        ListaMessaggi.setCellFactory(param -> new ListCell<Messaggio>() {
            @Override
            protected void updateItem(Messaggio messaggio, boolean empty) {
                super.updateItem(messaggio, empty);

                if (empty || messaggio == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                // Crea il layout per il messaggio
                VBox messaggioBox = new VBox(5);
                messaggioBox.setPadding(new Insets(5, 10, 5, 10));

                // Determina se il messaggio è stato inviato dall'utente corrente
                boolean messaggioMio = utenteLoggato != null && messaggio.getId_mittente() == utenteLoggato.getId();

                HBox intestazione = new HBox(10);
                //String nomeMittente = clientChat.getNomeMittente(messaggio, clientChat.getUtentiConosciuti(), messaggio.getId_mittente());
                //String nomeMittente = clientChat.getNomeMittente(messaggio, clientChat.getUtentiConosciuti(), utenteLoggato.getId());
                String nomeMittente = messaggioMio ? "Tu" : "Utente " + messaggio.getId_mittente();
                System.out.println("Nome mittente: " + nomeMittente);
                Label mittente = new Label(nomeMittente);
                mittente.setFont(Font.font("System", FontWeight.BOLD, 12));

                String orario = LocalDateTime.now().format(TIME_FORMATTER);
                Label timestamp = new Label(orario);
                timestamp.setFont(Font.font("System", FontWeight.NORMAL, 10));
                timestamp.setTextFill(Color.GRAY);

                intestazione.getChildren().addAll(mittente, timestamp);

                // Contenuto del messaggio
                Text contenuto = new Text(messaggio.getTesto());
                contenuto.setFont(Font.font("System", FontWeight.NORMAL, 14));
                contenuto.setWrappingWidth(200);  // Imposta la larghezza massima del testo

                // Aggiunge i componenti al container del messaggio
                messaggioBox.getChildren().addAll(intestazione, contenuto);

                // Allinea i messaggi a destra o sinistra in base al mittente
                HBox container = new HBox();
                if (messaggioMio) {
                    container.setAlignment(Pos.CENTER_RIGHT);
                    messaggioBox.setStyle("-fx-background-color: #DCF8C6; -fx-background-radius: 10;");
                } else {
                    container.setAlignment(Pos.CENTER_LEFT);
                    messaggioBox.setStyle("-fx-background-color: #ECECEC; -fx-background-radius: 10;");
                }
                container.getChildren().add(messaggioBox);
                setGraphic(container);
            }
        });
    }

    // meotodo chiamato quando l'utente seleziona una chat
    public void mostraConversazione(Conversazione conversazione, Utente utente) {
        this.conversazioneAttuale = conversazione;
        this.utenteLoggato = utente;

        Platform.runLater(() -> {
            Messaggio messaggioVuoto = new Messaggio(utenteLoggato.getId(), conversazione.getIdChat());
            RichiestaMessaggio richiestaMessaggioVuota = new RichiestaMessaggio(messaggioVuoto);
            try {
                clientChat.inviaRichiestaAlServer(richiestaMessaggioVuota);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            // Aggiorna l'interfaccia con i dati della conversazione
            if (chatNome != null) {
                chatNome.setText(conversazione.getNomeVisualizzato());
                logger.info("Impostato titolo chat: {}", conversazione.getNomeVisualizzato());
            }

            // Carica l'avatar dell'altro utente se disponibile
            if (chatAvatar != null && conversazione.getAltroUtente() != null &&
                    conversazione.getAltroUtente().getAvatar() != null) {

                File fileAvatar = new File(conversazione.getAltroUtente().getAvatar());
                if (fileAvatar.exists()) {
                    Image avatar = new Image(fileAvatar.toURI().toString());
                    chatAvatar.setImage(avatar);
                }
            }

            // Pulisce i messaggi vecchi
            if (ListaMessaggi != null) {
                ListaMessaggi.getItems().clear();
                logger.debug("Lista messaggi pulita");
            }

            if (PannelloPrincipale != null) {
                PannelloPrincipale.setVisible(true); // Mostra il pannello chat
            }
            logger.info("Mostrata conversazione: {}", conversazione.getNomeVisualizzato());
        });
    }

    private void inviaMessaggio() {

        String testoMessaggio = CellaMessaggio.getText().trim();

        if (!testoMessaggio.isEmpty() && conversazioneAttuale != null) {
            try {
                // Crea un nuovo messaggio
                Messaggio messaggio = new Messaggio(testoMessaggio, utenteLoggato.getId(), conversazioneAttuale.getIdChat());

                // Aggiungi il messaggio alla lista locale per visualizzazione immediata
                messaggiAttuali.add(messaggio);
                aggiornaMessaggi(messaggiAttuali);

                // Invia il messaggio tramite il client di rete
                /*
                if (clientChat != null) {
                    clientChat.inviaMessaggio(messaggio);

                    logger.info("Messaggio inviato: {}", messaggio.getTesto());
                } else {
                    logger.error("Cliente di rete non inizializzato");
                }
                */

                // Invece del messaggio, invio una RichiestaMessaggio
                if (clientChat != null) {
                    RichiestaMessaggio richiesta = new RichiestaMessaggio(messaggio);
                    clientChat.inviaRichiestaAlServer(richiesta);
                    logger.info("Messaggio inviato: {}", messaggio.getTesto());
                } else {
                    logger.error("Cliente di rete non inizializzato");
                }


                CellaMessaggio.clear(); // Pulisce il campo di input
            } catch (Exception e) {
                logger.error("Errore nell'invio del messaggio: {}", e.getMessage(), e);
            }
        }
    }

    // creaChat()
    // -L'utente crea una chat, se sceglie di farla con una sola persona allora è privata con quell'utente. Altrimenti è di gruppo
    // -

    // aggiungiPersonaAChat()


    public void aggiornaMessaggi(List<Messaggio> messaggi) {
        if (ListaMessaggi == null) {
            logger.error("Lista messaggi non disponibile");
            return;
        }
        // utilizzo runLater per mettere l'attività in coda ed eseguirla appena possibile
        Platform.runLater(() -> {
            try {
                ListaMessaggi.getItems().clear();
                messaggiAttuali = new ArrayList<>(messaggi);
                ListaMessaggi.getItems().addAll(messaggi);
                logger.info("Aggiornati {} messaggi nella lista", messaggi.size());

                // Scorre automaticamente alla fine per mostrare l'ultimo messaggio
                if (!messaggi.isEmpty()) {
                    ListaMessaggi.scrollTo(messaggi.size() - 1);
                }
            } catch (Exception e) {
                logger.error("Errore durante l'aggiornamento dei messaggi: {}", e.getMessage(), e);
            }
        });
    }

    // Per usare questa funzione dobbiamo prima fare la query e collegare tutto ciò che parte
    // da: riga 388 del Clienthanlder. Ci sono diversi commenti di cose che vanno implementate, tra cui
    // la query.
    // Inoltre forse va anche messo qualcosa nel Client per farlo funzionare.
    public void caricaChat(List<Chat> chats) {
        messaggiAttuali.clear(); // pulisci i messaggi attuali prima di caricare nuovi

        for (Chat chat : chats) {
            for (Messaggio messaggio : chat.getMessaggi().values()) {
                messaggiAttuali.add(messaggio);
            }
        }

        aggiornaMessaggi(messaggiAttuali);
    }


    public void setClientChat(chat.client.Client clientChat) {
        this.clientChat = clientChat;
    }

    // ritorno l'ID della conversazione attuale
    public int getIdConversazioneAttuale() {
        return conversazioneAttuale != null ? conversazioneAttuale.getIdChat() : -1;
    }
}