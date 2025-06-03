package chat.utils;

import chat.common.HashMapUtenti;
import chat.db.MySQLManager;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;


import java.awt.*;
import java.sql.Connection;

public class Controller {

    private HashMapUtenti utenti = new HashMapUtenti();

    @FXML
    private Label labelTitolo;

    @FXML
    private ListView<String> listUtenti; // Forzo la list view a essere <String>

    @FXML
    public void initialize() {
        labelTitolo.setText("Lista Utenti nel DB");
    }


    @FXML
    public void stampaUtenti() {
        try {
            XMLConfigLoaderDB.DBConfig config = XMLConfigLoaderDB.caricaConfigurazione("server.config.xml");
            MySQLManager db = new MySQLManager(config.ip, config.porta, config.nomeDB, config.username, config.password);
            db.connettiti();

            if (db.isConnected()) {
                Connection conn = db.getConnection();
                db.creaHashMapUtenti(conn, utenti);

                listUtenti.getItems().clear(); // svuota la lista

                utenti.getUtenti().values().forEach(u ->
                        listUtenti.getItems().add(u.toString())
                );

            } else {
                listUtenti.getItems().setAll("Errore di connessione al DB.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            listUtenti.getItems().setAll("Errore: " + e.getMessage());
        }
    }


}