package chat.client.controller;

import chat.client.GestoreFeedbackUI;
import chat.common.Utente;
import chat.db.MySQLManager;
import chat.utils.XMLConfigLoaderDB;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class StampaLista {

    private static final Logger logger = LoggerFactory.getLogger(StampaLista.class);

    @FXML
    private Label labelTitolo;

    // la listview la dichiaro come <String> e non come generic <?>
    @FXML
    private ListView<String> listUtenti;

    @FXML
    private Label feedbackLabel;

    // Creiamo un'istanza del manager a livello di classe
    private MySQLManager dbManager;

    @FXML
    public void initialize() {
        labelTitolo.setText("Lista Utenti nel DB");
        feedbackLabel.setText("");
        try {
            // Carichiamo la configurazione e inizializziamo il DB Manager una sola volta
            XMLConfigLoaderDB.DBConfig config = XMLConfigLoaderDB.caricaConfigurazione("server.config.xml");
            this.dbManager = new MySQLManager(config.ip, config.porta, config.nomeDB, config.username, config.password);

            Label placeholderLabel = new Label("Nessun utente trovato nel database.");
            placeholderLabel.setStyle("-fx-font-style: italic;");
            listUtenti.setPlaceholder(placeholderLabel);

            // Popoliamo la lista all'avvio della schermata
            stampaUtenti();

        } catch (Exception e) {
            logger.error("Errore nella lista de utenti", e);
            GestoreFeedbackUI.mostraErrore(feedbackLabel, "Errore nella configurazione del DB");
        }
    }

    @FXML
    public void stampaUtenti() {
        feedbackLabel.setText("");
        if (dbManager == null) {
            listUtenti.getItems().setAll("Errore: DBManager non inizializzato.");
            System.err.println("DBManager non inizializzato.");
            return;
        }

        try {
            // Usa il nuovo metodo getAllUsers() per ottenere una lista di oggetti Utente
            List<Utente> utentiDalDb = dbManager.getAllUsers();

            // Svuota la lista prima di riempirla di nuovo
            listUtenti.getItems().clear();

            // Itera sulla lista di oggetti Utente e aggiungi la loro rappresentazione
            for (Utente utente : utentiDalDb) {
                listUtenti.getItems().add(utente.toString());
            }

        } catch (Exception e) {
            logger.error("Errore durante il caricamento degli utenti", e);
            // Mostra un errore direttamente nella lista in caso di problemi
            listUtenti.getItems().setAll("Errore durante il caricamento degli utenti: " + e.getMessage());
        }
    }
}