package chat.client.controller;

import chat.client.Client;
import chat.common.Utente;
import chat.richieste.RichiestaListaUtenti;
import chat.richieste.RichiestaNuovaChat;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NuovaChatDialog {
    private static final Logger logger = LoggerFactory.getLogger(NuovaChatDialog.class);

    @FXML
    private TextField campoRicercaUtenti;

    @FXML
    private CheckBox checkboxGruppo;

    @FXML
    private TextField campoNomeGruppo;

    @FXML
    private ListView<Utente> listaUtenti;

    @FXML
    private Label labelFeedback;

    @FXML
    private Button btnAnnulla;

    @FXML
    private Button btnCrea;

    private Client client;
    private Utente utenteLoggato;
    private Stage dialogStage;
    private ObservableList<Utente> utenti = FXCollections.observableArrayList();
    private HashMap<Utente, Boolean> utentiSelezionati = new HashMap<>();

    @FXML
    public void initialize() {
        // Configura la lista degli utenti con checkbox
        listaUtenti.setCellFactory(CheckBoxListCell.forListView(utente -> {
            // Crea una proprietà booleana osservabile per ogni utente
            javafx.beans.property.SimpleBooleanProperty property = new javafx.beans.property.SimpleBooleanProperty(utentiSelezionati.getOrDefault(utente, false));

            // Aggiungi un listener per aggiornare la mappa quando la proprietà cambia
            property.addListener((observable, oldValue, newValue) -> {
                boolean isGruppo = checkboxGruppo.isSelected();

                // Se non è un gruppo e c'è già un utente selezionato, deseleziona gli altri
                if (!isGruppo && newValue && getNumeroUtentiSelezionati() > 0) {
                    // Deseleziona tutti gli altri utenti
                    for (Map.Entry<Utente, Boolean> entry : utentiSelezionati.entrySet()) {
                        if (!entry.getKey().equals(utente) && entry.getValue()) {
                            entry.setValue(false);
                        }
                    }
                    // Aggiorna solo questo utente
                    utentiSelezionati.put(utente, true);

                    // Aggiorna la vista
                    listaUtenti.refresh();
                } else {
                    // Aggiorna normalmente
                    utentiSelezionati.put(utente, newValue);
                }
            });

            return property;
        }));

        // Listener per mostrare/nascondere il campo nome gruppo
        checkboxGruppo.selectedProperty().addListener((observable, oldValue, newValue) -> {
            campoNomeGruppo.setVisible(newValue);
            campoNomeGruppo.setManaged(newValue);

            // Se deseleziono "gruppo", mantengo solo il primo utente selezionato
            if (!newValue && getNumeroUtentiSelezionati() > 1) {
                Utente primoUtente = null;

                // Trova il primo utente selezionato
                for (Map.Entry<Utente, Boolean> entry : utentiSelezionati.entrySet()) {
                    if (entry.getValue()) {
                        primoUtente = entry.getKey();
                        break;
                    }
                }

                // Deseleziona tutti
                for (Utente utente : utentiSelezionati.keySet()) {
                    utentiSelezionati.put(utente, false);
                }

                // Riseleziona solo il primo
                if (primoUtente != null) {
                    utentiSelezionati.put(primoUtente, true);
                }

                // Aggiorna la vista
                listaUtenti.refresh();

                // Feedback all'utente
                labelFeedback.setText("Mantenuto solo il primo utente selezionato");
                labelFeedback.setStyle("-fx-text-fill: blue;");
            } else {
                labelFeedback.setText("");
            }
        });

        // Configura il filtro per la ricerca utenti
        campoRicercaUtenti.textProperty().addListener((observable, oldValue, newValue) -> {
            filtroRicercaUtenti(newValue);
        });

        // Configura il click sulla lista per selezionare/deselezionare utenti
        listaUtenti.setOnMouseClicked(event -> {
            Utente utenteSel = listaUtenti.getSelectionModel().getSelectedItem();
            if (utenteSel != null) {
                boolean stato = utentiSelezionati.getOrDefault(utenteSel, false);
                boolean isGruppo = checkboxGruppo.isSelected();

                // Se non è un gruppo e c'è già un utente selezionato, deseleziona gli altri
                if (!isGruppo && !stato && getNumeroUtentiSelezionati() > 0) {
                    // Deseleziona tutti gli altri utenti
                    for (Map.Entry<Utente, Boolean> entry : utentiSelezionati.entrySet()) {
                        utentiSelezionati.put(entry.getKey(), false);
                    }
                }

                // Seleziona/deseleziona l'utente cliccato
                utentiSelezionati.put(utenteSel, !stato);

                // Aggiorna la vista
                listaUtenti.refresh();
            }
        });
    }

    private int getNumeroUtentiSelezionati() {
        int count = 0;
        for (Boolean selected : utentiSelezionati.values()) {
            if (selected) count++;
        }
        return count;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void setUtenteLoggato(Utente utenteLoggato) {
        this.utenteLoggato = utenteLoggato;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void caricaUtenti() {
        try {
            // Invia richiesta per ottenere la lista degli utenti
            RichiestaListaUtenti richiesta = new RichiestaListaUtenti("");
            client.inviaRichiestaAlServer(richiesta);
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Errore nella richiesta della lista utenti: {}", e.getMessage());
            labelFeedback.setText("Errore nel caricamento degli utenti");
            labelFeedback.setStyle("-fx-text-fill: red;");
        }
    }

    public void aggiornaListaUtenti(List<Utente> listaUtenti) {
        Platform.runLater(() -> {
            utenti.clear();
            for (Utente utente : listaUtenti) {
                // Non mostrare l'utente loggato nella lista
                if (utente.getId() != utenteLoggato.getId()) {
                    utenti.add(utente);
                    utentiSelezionati.put(utente, false);
                }
            }
            this.listaUtenti.setItems(utenti);
        });
    }

    private void filtroRicercaUtenti(String testoRicerca) {
        if (testoRicerca == null || testoRicerca.isEmpty()) {
            listaUtenti.setItems(utenti);
            return;
        }

        FilteredList<Utente> utentiFiltrati = new FilteredList<>(utenti, p -> true);
        utentiFiltrati.setPredicate(utente -> {
            String testoMinuscolo = testoRicerca.toLowerCase();

            if (utente.getUsername().toLowerCase().contains(testoMinuscolo))
                return true;
            if (utente.getNome() != null && utente.getNome().toLowerCase().contains(testoMinuscolo))
                return true;
            if (utente.getCognome() != null && utente.getCognome().toLowerCase().contains(testoMinuscolo))
                return true;

            return false;
        });

        listaUtenti.setItems(utentiFiltrati);
    }

    @FXML
    private void annulla() {
        dialogStage.close();
    }

    @FXML
    private void creaNuovaChat() {
        // Ottieni gli utenti selezionati
        List<Utente> selezionati = utentiSelezionati.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Verifica se ci sono utenti selezionati
        if (selezionati.isEmpty()) {
            labelFeedback.setText("Seleziona almeno un utente");
            labelFeedback.setStyle("-fx-text-fill: red;");
            return;
        }

        boolean isGruppo = checkboxGruppo.isSelected();

        // Se è un gruppo, verifica se ci sono almeno 2 utenti selezionati
        if (isGruppo && selezionati.size() < 2) {
            labelFeedback.setText("Per un gruppo seleziona almeno 2 utenti");
            labelFeedback.setStyle("-fx-text-fill: red;");
            return;
        }

        // Se è un gruppo, verifica che sia stato specificato un nome
        String nomeGruppo = null;
        if (isGruppo) {
            nomeGruppo = campoNomeGruppo.getText().trim();
            if (nomeGruppo.isEmpty()) {
                labelFeedback.setText("Inserisci un nome per il gruppo");
                labelFeedback.setStyle("-fx-text-fill: red;");
                return;
            }
        }

        try {
            // Crea la lista degli ID degli utenti selezionati
            List<Integer> idUtenti = new ArrayList<>();
            for (Utente utente : selezionati) {
                idUtenti.add(utente.getId());
            }

            // Crea la richiesta
            RichiestaNuovaChat richiesta = new RichiestaNuovaChat(idUtenti, utenteLoggato.getId(), isGruppo, nomeGruppo);

            // Invia la richiesta al server
            client.inviaRichiestaAlServer(richiesta);

            // Chiudi la finestra di dialogo
            dialogStage.close();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Errore nella creazione della nuova chat: {}", e.getMessage());
            labelFeedback.setText("Errore nella creazione della chat");
            labelFeedback.setStyle("-fx-text-fill: red;");
        }
    }
}