package chat.client.controller;

import chat.common.*;
import chat.richieste.RichiestaMembriGruppo;
import chat.richieste.RichiestaMessaggio;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ChatUI {
    ColorLogger colorLogger = new ColorLogger();

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

    @FXML
    public void initialize() {
        colorLogger.logInfo("Inizializzazione Interfaccia Grafica");

        this.clientChat = GestisciClient.getInstance().getClientChat();

        // Nascondi il pannello all'inizio, ovvero, non mostrare nessuna chat
        if (PannelloPrincipale != null) {
            PannelloPrincipale.setVisible(false);
            PannelloPrincipale.managedProperty().bind(PannelloPrincipale.visibleProperty());
            colorLogger.logInfo("Pannello chat inizialmente nascosto");
        }

        // Configurazione del bottone di invio
        if (btnInvio != null) {
            btnInvio.setOnAction(event -> inviaMessaggio());
        }

        // mi permette di inviare il messaggio premendo invio quando sono all'interno della cella
        if (CellaMessaggio != null) {
            CellaMessaggio.setOnAction(event -> inviaMessaggio());
        }

        if (ListaMessaggi != null) {
            configuraCellFactory();
        }
    }

    @FXML
    public void ListaMembriGruppo() {
        if (conversazioneAttuale == null) {
            colorLogger.logError("Nessuna conversazione selezionata");
            return;
        }

        try {
            // Crea la richiesta per ottenere i membri della chat
            RichiestaMembriGruppo richiesta = new RichiestaMembriGruppo(conversazioneAttuale.getIdChat());

            // Invia la richiesta al server
            clientChat.inviaRichiestaAlServer(richiesta);
            colorLogger.logInfo("Inviata richiesta membri gruppo per chat ID: " + conversazioneAttuale.getIdChat());
        } catch (Exception e) {
            colorLogger.logError("Errore nell'invio della richiesta membri gruppo: " + e.getMessage());
            mostraErrore("Impossibile ottenere i membri del gruppo");
        }
    }

    // configuro lo stile dei messaggi
    private void configuraCellFactory() {
        ListaMessaggi.setCellFactory(param -> new ListCell<>() {
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

                // Determina se il messaggio è stato inviato dall'utente corrente, questo per applicare uno stile diverso in base a chi ha inviato il messaggio
                boolean messaggioMio = utenteLoggato != null && messaggio.getId_mittente() == utenteLoggato.getId();

                HBox intestazione = new HBox(10);
                String nomeMittente = clientChat.getNomeMittente(messaggio, clientChat.getUtentiConosciuti(), utenteLoggato.getId());

               //  System.out.println("Nome mittente: " + nomeMittente);
                Label mittente = new Label(nomeMittente);
                mittente.setFont(Font.font("System", FontWeight.BOLD, 10));

                String orario = messaggio.getOraFormattata();
                Label timestamp = new Label(orario);
                timestamp.setFont(Font.font("System", FontWeight.NORMAL, 8));
                timestamp.setTextFill(Color.GRAY);

                intestazione.getChildren().addAll(mittente, timestamp);

                // Contenuto del messaggio
                Text contenuto = new Text(messaggio.getTesto());
                contenuto.setFont(Font.font("System", FontWeight.NORMAL, 12));
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

    // metodo chiamato quando l'utente seleziona una chat
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
                colorLogger.logInfo("Impostato titolo chat: " + conversazione.getNomeVisualizzato());
            }

            // Gestisci l'avatar in base al tipo di chat
            if (chatAvatar != null) {
                // Determina se è una chat di gruppo basandosi sul nome o altre proprietà
                boolean isGruppo = isGruppoChat(conversazione);

                if (isGruppo) {
                    // Se è un gruppo, nascondi l'avatar
                    chatAvatar.setOpacity(0.0);
                } else {
                    // Se è una chat privata, mostra l'avatar dell'altro utente se disponibile
                    chatAvatar.setOpacity(1.0);

                    if (conversazione.getAltroUtente() != null && conversazione.getAltroUtente().getAvatar() != null) {
                        File fileAvatar = new File(conversazione.getAltroUtente().getAvatar());
                        if (fileAvatar.exists()) {
                            Image avatar = new Image(fileAvatar.toURI().toString());
                            chatAvatar.setImage(avatar);
                        } else {
                            // Se il file non esiste, puoi impostare un'immagine predefinita
                            colorLogger.logError("File avatar non trovato: " + conversazione.getAltroUtente().getAvatar());
                        }
                    }
                }
            }

            // Pulisce i messaggi vecchi
            if (ListaMessaggi != null) {
                ListaMessaggi.getItems().clear();
                colorLogger.logDebug("Lista messaggi pulita");
            }

            if (PannelloPrincipale != null) {
                PannelloPrincipale.setVisible(true); // Mostra il pannello chat
            }
            colorLogger.logInfo("Mostrata conversazione: " + conversazione.getNomeVisualizzato());
        });
    }

    // Metodo di supporto per determinare se una conversazione è di gruppo
    private boolean isGruppoChat(Conversazione conversazione) {
        // se l'ID dell'altro utente è zero, allora è un gruppo
        return conversazione.getAltroUtente() != null && conversazione.getAltroUtente().getId() == 0;
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

                if (clientChat != null) {
                    RichiestaMessaggio richiesta = new RichiestaMessaggio(messaggio);
                    clientChat.inviaRichiestaAlServer(richiesta);
                    colorLogger.logInfo("Messaggio inviato: " + messaggio.getTesto());
                } else {
                    colorLogger.logError("Cliente di rete non inizializzato");
                }

                CellaMessaggio.clear(); // Pulisce il campo di input
            } catch (Exception e) {
                colorLogger.logError("Errore nell'invio del messaggio: " + e.getMessage());
            }
        }
    }

    public void aggiornaMessaggi(List<Messaggio> messaggi) {
        if (ListaMessaggi == null) {
            colorLogger.logError("Lista messaggi non disponibile");
            return;
        }
        // utilizzo runLater per mettere l'attività in coda ed eseguirla appena possibile
        Platform.runLater(() -> {
            try {
                ListaMessaggi.getItems().clear();
                messaggiAttuali = new ArrayList<>(messaggi);
                ListaMessaggi.getItems().addAll(messaggi);
                colorLogger.logInfo("Aggiornati " + messaggi.size() + " messaggi nella lista");

                // Scorre automaticamente alla fine per mostrare l'ultimo messaggio
                if (!messaggi.isEmpty()) {
                    ListaMessaggi.scrollTo(messaggi.size() - 1);
                }
            } catch (Exception e) {
                colorLogger.logError("Errore durante l'aggiornamento dei messaggi: " + e.getMessage());
            }
        });
    }




    public void gestisciMembriGruppoRicevuti(List<Utente> membri) {
        Platform.runLater(() -> {
            try {
                // Crea e mostra un dialog con la lista
                Stage dialog = new Stage();
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.setTitle("Membri della chat: " + conversazioneAttuale.getNomeVisualizzato());

                // Crea una lista per mostrare i membri
                ListView<String> listaMembri = new ListView<>();

                // Popola la lista con i nomi degli utenti
                for (Utente utente : membri) {
                    String nome = utente.getUsername();
                    if (utente.getId() == utenteLoggato.getId()) {
                        nome += " (Tu)";
                    }
                    listaMembri.getItems().add(nome);
                }

                // Crea un pulsante per chiudere
                Button btnChiudi = new Button("Chiudi");
                btnChiudi.setOnAction(e -> dialog.close());

                // Layout base
                VBox layout = new VBox(15);
                layout.setPadding(new Insets(20));
                layout.getChildren().addAll(listaMembri, btnChiudi);

                // Mostra il dialog
                Scene scene = new Scene(layout, 400, 500);
                dialog.setScene(scene);
                dialog.showAndWait();

            } catch (Exception e) {
                colorLogger.logError("Errore nella visualizzazione dei membri: " + e.getMessage());
                mostraErrore("Errore nella visualizzazione dei membri");
            }
        });
    }

    private void mostraErrore(String messaggio) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setHeaderText(null);
            alert.setContentText(messaggio);
            alert.showAndWait();
        });
    }

    public void setClientChat(chat.client.Client clientChat) {
        this.clientChat = clientChat;
    }

    public int getIdConversazioneAttuale() {
        return conversazioneAttuale != null ? conversazioneAttuale.getIdChat() : -1;
    }

}