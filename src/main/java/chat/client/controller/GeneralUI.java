package chat.client.controller;

import chat.client.Client;
import chat.client.GestoreFeedbackUI;
import chat.common.Conversazione;
import chat.common.Utente;
import chat.db.GestioneChat;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;

public class GeneralUI extends GestisciClient {
    private static final Logger logger = LoggerFactory.getLogger(GeneralUI.class);

    @FXML
    private TextField campoRicercaChat;
    @FXML
    private ListView<Conversazione> listaChat;
    @FXML
    private ImageView avatarUtenteLoggato;
    @FXML
    private Label labelNomeUtente;
    @FXML
    private Label labelUsername;
    @FXML
    private Label feedbackLabel;
    @FXML
    private ChatUI chatUIController;
    // Etichetta da nascondere quando si seleziona una chat
    @FXML
    private Label etichettaSelezionaChatPerIniziare;
    private Utente utenteLoggato;
    private GestioneChat gestioneChat;
    private ObservableList<Conversazione> conversazioni = FXCollections.observableArrayList();
    private Client clientChat;


public GeneralUI() {
    super();
}

    @FXML
    public void initialize() {
        try {
            // Configura la selezione della lista chat.
            // Aggiunge un listener su ListaChat, quando premi una chat essa viene selezionata e aperta con selezionaChat.
            listaChat.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    selezionaChat(newValue);    // newValue è la Conversazione che hai selezionato nella grafica
                }
            });

        } catch (Exception e) {
            logger.error("Errore nell'inizializzazione di GeneralUI: {}", e.getMessage(), e);
        }
    }


    // metodo chiamato da Login per passare i dati dell'utente
    public void initData (Utente utenteLoggato, Client clientChat) {
        try {
            // L'utente viene dalla chiamata di Login, dopo che Login avrà già effettuato una richiesta per l'utente.
            this.utenteLoggato = utenteLoggato;
            // Non dovrebbe connettersi direttamente al db ma al server, è già connesso dal clienthandler
            // GestioneChat non lo deve usare il client ma il server, quindi il client manda una richiesta in base a ciò
            // che deve fare e il server usa queste funzioni esatte per usare da se GestioneChat connettendosi al db (per
            // poi mandare la risposta al client)
            // DA QUI
         //   XMLConfigLoaderDB.DBConfig config = XMLConfigLoaderDB.caricaConfigurazione("server.config.xml");
           // MySQLManager dbManager = new MySQLManager(config.ip, config.porta, config.nomeDB, config.username, config.password);

            // this.gestioneChat = new GestioneChat(dbManager);
            // A QUI, se ne deve occupare il server.
            // Si usa una richiesta che chiede al server di dargli gestioneChat tramite GestioneChat(dbmanager) eseguito dal server
            // this.gestioneChat = RichiestaGestioneChat(); // Al posto di quello attuale

            // Crea e inizializza il client di rete
            this.clientChat = GestisciClient.getInstance().getClientChat();
            // AGGIUNGERE:
            // setControlloreGeneralUI();

            // Verifica che chatUIController sia stato iniettato correttamente
            if (chatUIController != null) {
                chatUIController.setClientChat(clientChat);
                clientChat.setChatUI(chatUIController);
                logger.info("Controller ChatUI inizializzato con successo");
            } else {
                logger.error("Controller ChatUI non disponibile - verifica l'inclusione FXML");
            }
            impostaInfoUtente();
            caricaConversazioni();
            impostaFiltroRicerca();
        } catch (Exception e) {
            logger.error("Errore critico durante l'inizializzazione del controller: {}", e.getMessage(), e);
            GestoreFeedbackUI.mostraErrore(feedbackLabel, "Errore critico di configurazione.");
        }
    }

    private void impostaInfoUtente() {
        labelNomeUtente.setText(utenteLoggato.getNome() + " " + utenteLoggato.getCognome());
        labelUsername.setText("@" + utenteLoggato.getUsername());

        if (utenteLoggato.getAvatar() != null && !utenteLoggato.getAvatar().isBlank()) {
            File fileAvatar = new File(utenteLoggato.getAvatar());
            if (fileAvatar.exists()) {
                Image avatar = new Image(fileAvatar.toURI().toString());
                avatarUtenteLoggato.setImage(avatar);
            }
        }
    }

    // QUESTA VA CAMBIATA, BISOGNA CREARE UNA RICHIESTA AL SERVER CHE RITORNI AL CLIENT LE INFO
    private void caricaConversazioni() {
        try {
            // Invia richiesta tramite stringa "Carica Conversazioni *idUtente*"
            // Il server legge la stringa, la divide e fa esattamente quello che fa questa funzione adesso MA NEL SERVER.
            // DEVE DIVENTARE:
            // conversazioni.setAll(richiestaGetConversazioniPerUtente(gestioneChat, utenteLoggato.getId());
            conversazioni.setAll(gestioneChat.getConversazioniPerUtente(utenteLoggato.getId())); // tutta la parte dentro la deve fare il server una volta che il client gli invia la richiesta (nello switch case)

            listaChat.setItems(conversazioni);
            logger.info("Caricate {} conversazioni per l'utente {}", conversazioni.size(), utenteLoggato.getUsername());

        } catch (SQLException e) {
            logger.error("Impossibile caricare le conversazioni per l'utente {}", utenteLoggato.getUsername(), e);
        }
    }

    private void impostaFiltroRicerca() {
        // Uso una FilteredList per la ricerca in tempo reale
        FilteredList<Conversazione> conversazioniFiltrate = new FilteredList<>(conversazioni, p -> true);

        campoRicercaChat.textProperty().addListener((observable, oldValue, newValue) -> {
            conversazioniFiltrate.setPredicate(conversazione -> {
                // Se il campo di ricerca è vuoto, mostra tutto
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String testoMinuscolo = newValue.toLowerCase();

                // Controlla se il nome utente dell'altro partecipante contiene il testo
                return conversazione.getAltroUtente().getUsername().toLowerCase().contains(testoMinuscolo);
            });
        });

        // Collega la lista filtrata alla ListView
        listaChat.setItems(conversazioniFiltrate);
    }


    // gestisco cosa fare con il "pannello" quando seleziono una chat
    private void selezionaChat(Conversazione conversazione) {
        if (conversazione != null && chatUIController != null) {
            // Nascondi l'etichetta "Seleziona una chat per iniziare"
            if (etichettaSelezionaChatPerIniziare != null) {
                etichettaSelezionaChatPerIniziare.setVisible(false);
            }

            // Mostra la conversazione nella UI
            chatUIController.mostraConversazione(conversazione, utenteLoggato);

            // Attiva la chat sul client di rete
            if (clientChat != null) {
                clientChat.attivaChat(conversazione.getIdChat());
            }

            logger.info("Selezionata chat: {} (ID: {})",
                    conversazione.getNomeVisualizzato(), conversazione.getIdChat());
        }
    }

    // la uso quando chiudo l'app
    // non dovrebbe esserci perché il client non si connette direttamente con il database
    // PROBABILMENTE DEPRECATA
    public void onClose() {
        if (clientChat != null) {
            clientChat.disconnetti();
        }
    }
}