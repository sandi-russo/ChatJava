package chat.client.controller;

import chat.client.GestoreFeedbackUI;
import chat.client.Main;
import chat.common.Utente;
import chat.db.GestioneUtente;
import chat.db.MySQLManager;
import chat.utils.XMLConfigLoaderDB;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class Login {
    private static final Logger logger = LoggerFactory.getLogger(Login.class);
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label feedbackLabel;

    private GestioneUtente gestioneUtente;

    @FXML
    public void initialize() {
        try {
            XMLConfigLoaderDB.DBConfig config = XMLConfigLoaderDB.caricaConfigurazione("server.config.xml");
            MySQLManager dbManager = new MySQLManager(config.ip, config.porta, config.nomeDB, config.username, config.password);
            this.gestioneUtente = new GestioneUtente(dbManager);
            feedbackLabel.setText("");
        } catch (Exception e) {
            logger.error("Errore nell'inizializzazione", e);
            feedbackLabel.setText("Errore critico di configurazione.");
        }
    }

    @FXML
    private void gestisciLogin() {
        feedbackLabel.setText("");
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (username.isBlank() || password.isBlank()) {
            feedbackLabel.setText("Inserisci username e password.");
            return;
        }
        try {
            Utente utenteLoggato = gestioneUtente.login(username, password);
            GestoreFeedbackUI.mostraSuccesso(feedbackLabel, "Login riuscito! Benvenuto " + utenteLoggato.getUsername());
        } catch (GestioneUtente.LoginException e) {
            feedbackLabel.setText(e.getMessage());
        } catch (Exception e) {
            logger.error("Errore nella connessione al DB", e);
            feedbackLabel.setText("Errore di connessione al database.");
        }
    }

    @FXML
    private void vaiARegistrazione() throws IOException {
        Main.getInstance().navigateTo("Registrazione.fxml");
    }
}
