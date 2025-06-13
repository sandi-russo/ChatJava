package chat.client.controller;

import chat.client.GestoreFeedbackUI;
import chat.common.Conversazione;
import chat.common.Utente;
import chat.db.GestioneChat;
import chat.db.MySQLManager;
import chat.utils.XMLConfigLoaderDB;
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

public class GeneralUI {
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

    private Utente utenteLoggato;
    private GestioneChat gestioneChat;
    private ObservableList<Conversazione> conversazioni = FXCollections.observableArrayList();


    // metodo che richiamo in Login per passare i dati dell'utente
    public void initData(Utente utente) {

        try {
            this.utenteLoggato = utente;

            XMLConfigLoaderDB.DBConfig config = XMLConfigLoaderDB.caricaConfigurazione("server.config.xml");
            MySQLManager dbManager = new MySQLManager(config.ip, config.porta, config.nomeDB, config.username, config.password);

            this.gestioneChat = new GestioneChat(dbManager);

            impostaInfoUtente();
            caricaConversazioni();
            impostaFiltroRicerca();
        } catch (Exception e) {     // Il primo parametro è un messaggio descrittivo, il secondo è l'eccezione stessa.
            logger.error("Errore critico durante l'inizializzazione del controller di registrazione", e);
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

    private void caricaConversazioni() {
        try {
            conversazioni.setAll(gestioneChat.getConversazioniPerUtente(utenteLoggato.getId()));
            System.out.println("Sono dopo il setAll");

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
                return conversazione.getAltroUtente().getUsername().toLowerCase().contains(testoMinuscolo);// Non trovato
            });
        });

        // Collega la lista filtrata alla ListView
        listaChat.setItems(conversazioniFiltrate);
    }
}

