package chat.client.controller;

import chat.client.Main;
import chat.db.GestioneUtente;
import chat.db.MySQLManager;
import chat.utils.XMLConfigLoaderDB;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import chat.client.GestoreFeedbackUI;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.slf4j.Logger; // Lo utilizzo per avere un controllo sugli errori più approfondito rispetto a printStackTrace che è più generico
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


public class Registrazione {
    private static final Logger logger = LoggerFactory.getLogger(Registrazione.class); // Logger per gli errori
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


    @FXML
    public void initialize() {
        try {
            XMLConfigLoaderDB.DBConfig config = XMLConfigLoaderDB.caricaConfigurazione("server.config.xml");
            MySQLManager dbManager = new MySQLManager(config.ip, config.porta, config.nomeDB, config.username, config.password);
            this.gestioneUtente = new GestioneUtente(dbManager);
            feedbackLabel.setText("");
        } catch (Exception e) {     // Il primo parametro è un messaggio descrittivo, il secondo è l'eccezione stessa.
            logger.error("Errore critico durante l'inizializzazione del controller di registrazione", e);
            GestoreFeedbackUI.mostraErrore(feedbackLabel, "Errore critico di configurazione.");
        }
    }

    @FXML
    private void gestisciSceltaAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Scegli un'immagine per l'avatar");
        // Filtriamo per vedere solo i file immagine
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Immagini", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")); // seleziono solo estensioni di tipo immagine

        // Mostriamo la finestra di dialogo
        File fileScelto = fileChooser.showOpenDialog(anteprimaAvatar.getScene().getWindow());

        if (fileScelto != null) {
            this.fileAvatarSelezionato = fileScelto;
            nomeFileAvatarLabel.setText(fileScelto.getName());
            // Mostriamo l'anteprima
            Image immagine = new Image(fileScelto.toURI().toString());
            anteprimaAvatar.setImage(immagine);
        }
    }

    @FXML
    private void gestisciRegistrazione() {
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

        String percorsoAvatarDaSalvare = null; // null se l'utente non sceglie un avatar

        //  l'utente ha scelto un file, allora lo copiamo e salviamo il percorso
        if (fileAvatarSelezionato != null) {
            try {
                Path cartellaDestinazione = Paths.get("dati_server/avatar");
                Files.createDirectories(cartellaDestinazione); // creo la cartella, se non esiste

                // creo il file con il timestamp per avere un nome univoco
                String nomeFileOriginale = fileAvatarSelezionato.getName();
                String estensione = nomeFileOriginale.substring(nomeFileOriginale.lastIndexOf("."));
                String nomeFileUnivoco = username + "_" + System.currentTimeMillis() + estensione;

                // copio il file o lo sostituisco se già esiste
                Path percorsoDestinazioneCompleto = cartellaDestinazione.resolve(nomeFileUnivoco);
                Files.copy(fileAvatarSelezionato.toPath(), percorsoDestinazioneCompleto, StandardCopyOption.REPLACE_EXISTING);

                //percorso che scriverò nel db
                percorsoAvatarDaSalvare = percorsoDestinazioneCompleto.toString();
            } catch (IOException e) {
                logger.error("Errore durante il salvataggio dell'avatar", e);
                GestoreFeedbackUI.mostraErrore(feedbackLabel, "Impossibile salvare l'immagine dell'avatar");
                return;
            }
        }

        try {
            gestioneUtente.registraUtente(username, nome, cognome, email, password, percorsoAvatarDaSalvare);
            GestoreFeedbackUI.mostraSuccesso(feedbackLabel, "Registrazione completata! Ora puoi accedere.");
        } catch (GestioneUtente.UserRegistrationException e) {
            GestoreFeedbackUI.mostraErrore(feedbackLabel, e.getMessage());
        } catch (Exception e) {
            logger.error("Errore imprevisto durante la registrazione per l'utente '{}'", username, e);
            GestoreFeedbackUI.mostraErrore(feedbackLabel, "Si è verificato un errore di sistema. Riprova.");
        }
    }

    @FXML
    private void vaiAlLogin() throws IOException {
        Main.getInstance().navigateTo("Login.fxml");
    }
}