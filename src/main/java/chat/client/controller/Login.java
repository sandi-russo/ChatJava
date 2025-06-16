package chat.client.controller;

import chat.client.GestoreFeedbackUI;
import chat.client.Main;
import chat.common.Utente;
import chat.db.GestioneUtente;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

import chat.richieste.RichiestaLogin;

public class Login {
    private static final Logger logger = LoggerFactory.getLogger(Login.class);
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label feedbackLabel;
    @FXML
    private Button btnLogin;

    // è il client
    private chat.client.Client clientChat;

    public void initialize() {
        try {
            // deve connettersi al server e mandare la richiesta per le info dell'utente, non dal db.
            // Qui viene usato GestioneUtente che è del db. Invece di questo deve venire creata un oggetto Richiesta,
            // questa Richiesta fa in modo che il client chieda al server di connettersi e il server manda al client
            // le informazioni riguardanti l'utente.
            // Quindi tutto questo sotto la fa il server e non il client.

            // GestioneUtente LO DEVE USARE IL CLIENT, ma lo deve richiedere al server. Quindi queste funzioni devono solo mandare richieste e poi il server
            // si deve occupare di elaborarle (per come sono già qui, vanno benissimo perché usano GestioneUtente) e poi mandare indietro una risposta
            // DA QUI
            //XMLConfigLoaderDB.DBConfig config = XMLConfigLoaderDB.caricaConfigurazione("server.config.xml");
            //MySQLManager dbManager = new MySQLManager(config.ip, config.porta, config.nomeDB, config.username, config.password);
            // A QUI va fatto nel DB

            // La prossima linea, invece del new GestioneUtente(dbManager) deve avere:
            // this.gestioneUtente = richiestaGestioneUtente();
           // this.gestioneUtente = new GestioneUtente(dbManager);

            feedbackLabel.setText("");

            // imposto il pulsante come predefinito per abilitare l'invio da tastiera
            btnLogin.setDefaultButton(true);

            GestisciClient infoClient = new GestisciClient();
            this.clientChat = infoClient.getClientChat();
            this.clientChat.setControlloreLogin(this); // registro il controller per il client

        } catch (Exception e) {
            logger.error("Errore nell'inizializzazione", e);
            feedbackLabel.setText("Errore critico di configurazione.");
        }
    }

    @FXML
    private void gestisciLogin() throws SQLException {
        feedbackLabel.setText("");
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (username.isBlank() || password.isBlank()) {
            return;
        }

        try {
            // gestioneUtente.login(username,password); LO DEVE FARE IL SERVER NELLO SWITCH CASE
            // quindi va fatta una richiesta:

            RichiestaLogin richiesta = new RichiestaLogin(username, password);
            clientChat.inviaRichiestaAlServer(richiesta);
            // richiestaLogin(gestioneUtente, username, password);
            // Utente utenteLoggato = gestioneUtente.login(username, password);
            //Utente utenteLoggato = clientChat.getUtenteDaGestisciClient();
            //GestoreFeedbackUI.mostraSuccesso(feedbackLabel, "Login riuscito! Benvenuto " + utenteLoggato.getUsername());
            //GeneralUI chatController = (GeneralUI) Main.getInstance().navigateTo("GeneralUI.fxml");
            //infoClient.setUtenteLoggato(utenteLoggato);
            // login deve modificare GestisciController mettendogli l'utente loggato così che venga avviato il client e possano
            // usarlo tutti quanti.
            // login crea il primo GestisciController, poi lo invia a GeneralUI che lo invia ad altre funzioni se gli serve usarlo
           // chatController.initData(utenteLoggato, clientChat);

        } catch (Exception e) {
            logger.error("Non riesco ad inviare la richiesta al server", e);
            feedbackLabel.setText("Non riesco ad inviare la richiesta al server");
        }
    }

    // HO CREATO QUESTO METODO CHE VIENE CHIAMATA *DOPO* LA RISPOSTA POSITIVA DI LOGIN
    public void gestisciLoginConSuccesso (Utente utenteLoggato){
        // metto il codice che c'era sopra, non ho modificato praticamente nulla

        Platform.runLater(()-> {
            try {
                GestoreFeedbackUI.mostraSuccesso(feedbackLabel, "Login riuscito!");

                // se sono qui, il login è riuscito e posso caricare la schermata principale
                GeneralUI chatController = (GeneralUI) Main.getInstance().navigateTo("GeneralUI.fxml");
                chatController.initData(utenteLoggato, clientChat); // gli passo le informazioni dell'utente appena loggato all'interno della schermata principale
            } catch (IOException e) {
                logger.error("Impossibile caricare la schermata principale");
            }
        });
    }

    // qui creo la versione per un login fallito, essendo fallito, passerò l'oggetto String con un messaggio e non l'oggetto Utente
    public void gestisciLoginFallito (String messsaggioErrore){
        Platform.runLater(()-> {
            GestoreFeedbackUI.mostraErrore(feedbackLabel, messsaggioErrore);
        });
    }

    @FXML
    private void vaiARegistrazione() throws IOException {
        Main.getInstance().navigateTo("Registrazione.fxml");
    }

    public void setClientChat(chat.client.Client clientChat) {
        this.clientChat = clientChat;
    }
}
