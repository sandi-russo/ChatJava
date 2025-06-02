package chat.utils;

import chat.common.HashMapUtenti;
import chat.db.MySQLManager;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;


import java.awt.*;
import java.sql.Connection;

public class Controller {

    private HashMapUtenti utenti = new HashMapUtenti();

    @FXML
    private TextArea stampaArea;

    @FXML
    public void stampaButton() {
        try {
            XMLConfigLoaderDB.DBConfig config = XMLConfigLoaderDB.caricaConfigurazione("server.config.xml");
            MySQLManager db = new MySQLManager(config.ip, config.porta, config.nomeDB, config.username, config.password);
            db.connettiti();

            if (db.isConnected()) {
                Connection conn = db.getConnection();
                db.creaHashMapUtenti(conn, utenti);

                // Costruisci l'output
                StringBuilder output = new StringBuilder("Utenti nel DB:\n");
                utenti.getUtenti().values().forEach(u -> output.append(u.toString()).append("\n"));

                stampaArea.setText(output.toString());
                // db.chiudi(); // facoltativo: commentalo se vuoi tenerla aperta
            } else {
                stampaArea.setText("Errore di connessione al DB.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            stampaArea.setText("Errore: " + e.getMessage());
        }
    }
}