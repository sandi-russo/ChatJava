package chat.client.controller;

import chat.client.GestoreFeedbackUI;
import chat.client.Main;
import chat.common.ColorLogger;
import chat.common.Utente;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;

import chat.richieste.RichiestaLogin;

public class Login {
    ColorLogger colorLogger = new ColorLogger();

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

    @FXML
    public void initialize() {
        try {
            feedbackLabel.setText("");
            // imposto il pulsante come predefinito per abilitare l'invio da tastiera
            btnLogin.setDefaultButton(true);

            GestisciClient infoClient = new GestisciClient();
            this.clientChat = infoClient.getClientChat();
            this.clientChat.setControlloreLogin(this); // registro il controller per il client

        } catch (Exception e) {
            colorLogger.logError("Errore nell'inizializzazione");
            feedbackLabel.setText("Errore critico di configurazione.");
        }
    }

    @FXML
    private void gestisciLogin() {
        feedbackLabel.setText("");
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (username.isBlank() || password.isBlank()) {
            return;
        }

        try {

            RichiestaLogin richiesta = new RichiestaLogin(username, password);
            clientChat.inviaRichiestaAlServer(richiesta);
            // login crea il primo GestisciController, poi lo invia a GeneralUI che lo invia ad altre funzioni se gli serve usarlo
           // chatController.initData(utenteLoggato, clientChat);

        } catch (Exception e) {
            colorLogger.logError("Non riesco ad inviare la richiesta al server " + e);
            feedbackLabel.setText("Non riesco ad inviare la richiesta al server");
        }
    }

    @FXML
    private void vaiARegistrazione() throws IOException {
        Main.getInstance().navigateTo("Registrazione.fxml");
    }

    // HO CREATO QUESTO METODO CHE VIENE CHIAMATA *DOPO* LA RISPOSTA POSITIVA DI LOGIN
    public void gestisciLoginConSuccesso (Utente utenteLoggato){

        Platform.runLater(()-> {
            try {
                GestoreFeedbackUI.mostraSuccesso(feedbackLabel, "Login riuscito!");

                // se sono qui, il login è riuscito e posso caricare la schermata principale
                GeneralUI chatController = (GeneralUI) Main.getInstance().navigateTo("GeneralUI.fxml");
                chatController.initData(utenteLoggato, clientChat); // passo le informazioni dell'utente appena loggato all'interno della schermata principale
            } catch (IOException e) {
                colorLogger.logError("Impossibile caricare la schermata principale");
            }
        });
    }

    // qui creo la versione per un login fallito, essendo fallito, passerò l'oggetto String con un messaggio e non l'oggetto Utente
    public void gestisciLoginFallito (String messsaggioErrore){
        Platform.runLater(()-> {
            GestoreFeedbackUI.mostraErrore(feedbackLabel, messsaggioErrore);
        });
    }

    public void setClientChat(chat.client.Client clientChat) {
        this.clientChat = clientChat;
    }
}
