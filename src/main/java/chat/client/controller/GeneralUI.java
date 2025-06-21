package chat.client.controller;

import chat.client.Client;
import chat.client.GestoreFeedbackUI;
import chat.common.ColorLogger;
import chat.common.Conversazione;
import chat.common.Utente;
import chat.richieste.RichiestaConversazioni;
import chat.richieste.RichiestaListaUtenti;
import chat.richieste.RichiestaNuovaChat;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.net.URL;
import java.util.List;

import java.io.File;
import java.io.IOException;

public class GeneralUI extends GestisciClient {
    ColorLogger colorLogger = new ColorLogger();

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
    private Button btnNuovaChat;
    @FXML
    private ChatUI chatUIController;
    // Etichetta da nascondere quando si seleziona una chat
    @FXML
    private Label etichettaSelezionaChatPerIniziare;
    private Utente utenteLoggato;
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
            colorLogger.logError("Errore nell'inizializzazione di GeneralUI: " + e.getMessage());

        }
    }

    @FXML
    private void mostraDialogNuovaChat() {
        try {
            // Carica il file FXML
            URL location = getClass().getResource("/fxml/NuovaChatDialog.fxml");

            if (location == null) {
                GestoreFeedbackUI.mostraErrore(feedbackLabel, "Errore nell'apertura del dialog");
                return;
            }

            // Carica il file FXML
            FXMLLoader loader = new FXMLLoader(location);
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // Configura la finestra di dialogo
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Nuova Chat");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(btnNuovaChat.getScene().getWindow());
            dialogStage.setScene(scene);

            // Ottieni il controller e imposta i dati
            NuovaChatDialog controller = loader.getController();

            // Salva il controller come userData della scena per recuperarlo più tardi
            scene.setUserData(controller);

            controller.setDialogStage(dialogStage);
            controller.setClient(clientChat);
            controller.setUtenteLoggato(utenteLoggato);

            // Carica la lista degli utenti
            controller.caricaUtenti();

            // Mostra la finestra di dialogo
            dialogStage.showAndWait();
        } catch (IOException e) {
            colorLogger.logError("Errore nell'apertura del dialog per nuova chat: " + e.getMessage());
            GestoreFeedbackUI.mostraErrore(feedbackLabel, "Errore nell'apertura del dialog");
        }
    }

    // metodo chiamato da Login per passare i dati dell'utente
    public void initData(Utente utenteLoggato, Client clientChat) {
        try {
            // L'utente viene dalla chiamata di Login, dopo che Login avrà già effettuato una richiesta per l'utente.
            this.utenteLoggato = utenteLoggato;
            this.clientChat = clientChat;
            this.clientChat.setControlloreGeneralUI(this);
            if (chatUIController != null) {
                chatUIController.setClientChat(clientChat);
                clientChat.setChatUI(chatUIController);
            } else {
                colorLogger.logError("Controller ChatUI non disponibile");
            }
            impostaInfoUtente();
            caricaConversazioni();
            impostaFiltroRicerca();
        } catch (Exception e) {
            colorLogger.logError(e.getMessage());
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
            RichiestaConversazioni richiesta = new RichiestaConversazioni(utenteLoggato.getId());
            clientChat.inviaRichiestaAlServer(richiesta);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void gestisciConversazioneConSuccesso(RichiestaConversazioni risposta) {
        // metto il codice che c'era sopra, non ho modificato praticamente nulla

        Platform.runLater(() -> {

            List<Conversazione> listaRicevuta = risposta.getConversazioni();

            if (listaRicevuta == null || listaRicevuta.isEmpty()) {
                colorLogger.logError("Ricevuta una lista di conversazioni vuota o nulla dal server");
                return;
            }

            if (clientChat != null) {
                clientChat.popolaUtentiDaConversazioni(listaRicevuta);
            }
            conversazioni.setAll(listaRicevuta);
            impostaFiltroRicerca();

            colorLogger.logInfo("Caricate " + conversazioni.size() + " conversazioni per l'utente " + utenteLoggato.getUsername());
        });
    }


    // qui creo la versione per un login fallito, essendo fallito, passerò l'oggetto String con un messaggio e non l'oggetto Utente
    public void gestisciConversazioneFallito(String messsaggioErrore) {
        Platform.runLater(() -> {
            GestoreFeedbackUI.mostraErrore(feedbackLabel, messsaggioErrore);
        });
    }


    private void impostaFiltroRicerca() {

        FilteredList<Conversazione> conversazioniFiltrate = new FilteredList<>(conversazioni, p -> true);

        // Imposta la FilteredList come modello per la ListView
        listaChat.setItems(conversazioniFiltrate);

        // Aggiungi un listener al campo di ricerca per aggiornare il predicato
        campoRicercaChat.textProperty().addListener((observable, oldValue, newValue) -> {

            conversazioniFiltrate.setPredicate(conversazione -> {
                // Se il campo di ricerca è vuoto, mostra tutto
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String testoMinuscolo = newValue.toLowerCase();

                // Verifica che altroUtente non sia null prima di usarlo
                if (conversazione.getAltroUtente() == null) {
                    return false;
                }

                // Controlla sia username che nome/cognome se disponibili
                boolean matchUsername = conversazione.getAltroUtente().getUsername().toLowerCase().contains(testoMinuscolo);
                boolean matchNome = false;

                if (conversazione.getAltroUtente().getNome() != null) {
                    matchNome = conversazione.getAltroUtente().getNome().toLowerCase().contains(testoMinuscolo);
                }

                boolean matchCognome = false;
                if (conversazione.getAltroUtente().getCognome() != null) {
                    matchCognome = conversazione.getAltroUtente().getCognome().toLowerCase().contains(testoMinuscolo);
                }

                return matchUsername || matchNome || matchCognome;
            });
        });
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

            colorLogger.logInfo("Selezionata chat: " + conversazione.getNomeVisualizzato() + ", ID: " + conversazione.getIdChat());
        }
    }


    public void gestisciListaUtentiConSuccesso(RichiestaListaUtenti risposta) {
        Platform.runLater(() -> {
            // Trova il controller NuovaChatDialog attivo
            for (Window window : Window.getWindows()) {
                if (window instanceof Stage stage && stage.isShowing() && stage.getTitle().equals("Nuova Chat")) {
                    Scene scene = stage.getScene();
                    if (scene != null) {
                        // Cerca il controller nella scena
                        for (Node node : scene.getRoot().lookupAll("*")) {
                            if (node.getUserData() instanceof NuovaChatDialog) {
                                NuovaChatDialog controller = (NuovaChatDialog) node.getUserData();
                                controller.aggiornaListaUtenti(risposta.getUtenti());
                                return;
                            }
                        }

                        // Prova ad ottenere il controller usando FXMLLoader
                        Object controller = scene.getUserData();
                        if (controller instanceof NuovaChatDialog) {
                            ((NuovaChatDialog) controller).aggiornaListaUtenti(risposta.getUtenti());
                            return;
                        }
                    }
                }
            }

            colorLogger.logError("Non è stato possibile trovare la finestra di dialog attiva o il suo controller");
        });
    }


    public void gestisciNuovaChatConSuccesso(RichiestaNuovaChat risposta) {
        Platform.runLater(() -> {
            if (risposta.getNuovaConversazione() != null) {
                // Aggiungi la nuova conversazione alla lista
                conversazioni.add(0, risposta.getNuovaConversazione());

                // Aggiorna gli utenti conosciuti
                Utente altroUtente = risposta.getNuovaConversazione().getAltroUtente();
                if (altroUtente != null && altroUtente.getId() != 0) {
                    clientChat.getUtentiConosciuti().put(altroUtente.getId(), altroUtente);
                }

                // Seleziona la nuova chat
                listaChat.getSelectionModel().select(0);
                selezionaChat(risposta.getNuovaConversazione());
            }
        });
    }
}