package chat.client.controller;

import chat.client.Main;
import chat.common.ColorLogger;
import chat.db.GestioneUtente;
import chat.richieste.RichiestaRegistrazioneUtente;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import chat.client.GestoreFeedbackUI;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;


import java.io.File;
import java.io.IOException;

public class Registrazione {
    ColorLogger colorLogger = new ColorLogger();
    @FXML
    private TextField usernameField;
    @FXML
    private TextField nomeField;
    @FXML
    private TextField cognomeField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label feedbackLabel;
    @FXML
    private ImageView anteprimaAvatar;
    @FXML
    private Label nomeFileAvatarLabel;

    private GestioneUtente gestioneUtente;
    private File fileAvatarSelezionato; // lo utilizzo per salvare il file scelto dall'utente

    private chat.client.Client clientChat;

    @FXML
    public void initialize() {
        try {
            // Stessa cosa del login, il client fa una richiesta "Registazione *username password etc* al server
            // il server fa la query al db e gli ritorna al client un oggetto di tipo GestioneUtente così se la vede il client con queste funzioni
            //XMLConfigLoaderDB.DBConfig config = XMLConfigLoaderDB.caricaConfigurazione("server.config.xml");
            //MySQLManager dbManager = new MySQLManager(config.ip, config.porta, config.nomeDB, config.username, config.password);
            // usare RichiestaGestioneUtente (esiste già in login se l'hai già implementata.
            // diventa: this.gestioneUtente = RichiestaGestioneUtente();
            this.clientChat = GestisciClient.getInstance().getClientChat();
            clientChat.setControlloreRegistrazione(this);
            // AGGIUSTARE = feedbackLabel.setText("");
        } catch (Exception e) {     // Il primo parametro è un messaggio descrittivo, il secondo è l'eccezione stessa.
            colorLogger.logError("Errore critico durante l'inizializzazione del controller di registrazione" + e);
            GestoreFeedbackUI.mostraErrore(feedbackLabel, "Errore critico di configurazione.");
        }
    }

    @FXML
    private void gestisciSceltaAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Scegli un'immagine per l'avatar");
        // Filtriamo per vedere solo i file immagine
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Immagini", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")); // seleziono solo estensioni di tipo immagine

        // Mostriamo la finestra di dialogo. Il filescelto è l'immagine
        File fileScelto = fileChooser.showOpenDialog(anteprimaAvatar.getScene().getWindow());

        if (fileScelto != null) {
            this.fileAvatarSelezionato = fileScelto; //fileAvatarSelezionato è L'IMMAGINE
            nomeFileAvatarLabel.setText(fileScelto.getName());
            // Mostriamo l'anteprima
            Image immagine = new Image(fileScelto.toURI().toString());
            anteprimaAvatar.setImage(immagine);
        }
    }

    @FXML
    private void gestisciRegistrazione() throws IOException, ClassNotFoundException {
        feedbackLabel.setText("");
        String username = usernameField.getText();
        String nome = nomeField.getText();
        String cognome = cognomeField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            GestoreFeedbackUI.mostraErrore(feedbackLabel, "Username, Email e Password sono campi obbligatori!");
            return;
        }

        // Prendere l'avatar selezionato dall'utente.
        RichiestaRegistrazioneUtente richiesta = new RichiestaRegistrazioneUtente(username, nome, cognome, email, password, fileAvatarSelezionato);
        System.out.println("Sono prima che la richiesta sia stata inviata al server");
        clientChat.inviaRichiestaAlServer(richiesta);
        System.out.println("Sono dopo che la richiesta è stata inviata al server");
//        // DA QUI DEVE FARLO IL SERVER. COPIA TUTTO QUESTO SU UNA FUNZIONE DI CLIENTHANLDER CHE DOVRà ESSERE IN ELABORADATI.
//        String percorsoAvatarDaSalvare = null; // null se l'utente non sceglie un avatar
//
//        //  l'utente ha scelto un file, allora lo copiamo e salviamo il percorso
//        if (fileAvatarSelezionato != null) {
//            try {
//                Path cartellaDestinazione = Paths.get("dati_server/avatar");
//                Files.createDirectories(cartellaDestinazione); // creo la cartella, se non esiste
//
//                // creo il file con il timestamp per avere un nome univoco
//                String nomeFileOriginale = fileAvatarSelezionato.getName();
//                String estensione = nomeFileOriginale.substring(nomeFileOriginale.lastIndexOf("."));
//                String nomeFileUnivoco = username + "_" + System.currentTimeMillis() + estensione;
//
//                // copio il file o lo sostituisco se già esiste
//                Path percorsoDestinazioneCompleto = cartellaDestinazione.resolve(nomeFileUnivoco);
//                Files.copy(fileAvatarSelezionato.toPath(), percorsoDestinazioneCompleto, StandardCopyOption.REPLACE_EXISTING);
//
//                //percorso che scriverò nel db
//                percorsoAvatarDaSalvare = percorsoDestinazioneCompleto.toString();
//            } catch (IOException e) {
//                logger.error("Errore durante il salvataggio dell'avatar", e);
//                GestoreFeedbackUI.mostraErrore(feedbackLabel, "Impossibile salvare l'immagine dell'avatar");
//                return;
//        try {
//            // Questo deve farlo il server, client in questo try deve solo fare una RICHIESTA al server.
//            // richiestaRegistraUtente(gestioneUtente, username, nome, cognome, email, password, percorsoAvatarDaSalvare)
//            // gestioneUtente.registraUtente(username, nome, cognome, email, password, percorsoAvatarDaSalvare);
//            // A QUI è TUTTO LATO SERVER
//            GestoreFeedbackUI.mostraSuccesso(feedbackLabel, "Registrazione completata! Ora puoi accedere.");
//        } catch (
//                GestioneUtente.UserRegistrationException e) { // SE FALLISCE LA RICHIESTA, MOSTRA QUESTO. CAMBIA IL CATCH
//            GestoreFeedbackUI.mostraErrore(feedbackLabel, e.getMessage());
//        } catch (Exception e) {
//            logger.error("Errore imprevisto durante la registrazione per l'utente '{}'", username, e);
//            GestoreFeedbackUI.mostraErrore(feedbackLabel, "Si è verificato un errore di sistema. Riprova.");
//        }

    }

    @FXML
    private void vaiAlLogin () throws IOException {
        Main.getInstance().navigateTo("Login.fxml");
    }

    public void gestisciRegistrazioneConSuccesso() {
        Platform.runLater(() -> {
            try {
                GestoreFeedbackUI.mostraSuccesso(feedbackLabel, "Registrazione riuscita!");

                // se sono qui, la registrazione è riuscita e posso caricare la schermata di login
                vaiAlLogin();
            } catch (IOException e) {
                colorLogger.logError("Impossibile caricare la schermata principale");
            }
        });
    }

    // qui creo la versione per un login fallito, essendo fallito, passerò l'oggetto String con un messaggio e non l'oggetto Utente
    public void gestisciRegistrazioneFallita(String messsaggioErrore) {
        Platform.runLater(() -> {
            GestoreFeedbackUI.mostraErrore(feedbackLabel, messsaggioErrore);
        });
    }


}