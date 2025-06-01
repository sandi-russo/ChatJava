package chat;

import chat.common.TipoMessaggio;
import chat.common.Utente;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import chat.db.MySQLManager;
import chat.utils.XMLConfigLoaderDB;
import java.sql.*;

import java.io.IOException;


public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) throws SQLException {
        //launch();


        // Uso questa parte qui per fare dei test con la Map (chiave valore) che servirà al server per caricare gli utenti e alla chat per sapere quali utenti sono abilitati in quella chat
        System.out.println("Ciao!");

        // La Map è un dizionario chiave valore, in questo caso ID - Utente.
        Map<Integer, Utente> utenti = new HashMap<>();
        // A inizio server (o anche durante ogni volta che si aggiorna) carica tutti gli utenti presenti nel db o in ogni chat
        utenti.put(1, new Utente(1, "Paola", "Cariddi", "Parola.Cariddi@gmail.com", "1234", "./Paola.png"));
        utenti.put(2, new Utente(2, "Paola", "Cariddi", "Parola.Cariddi@gmail.com", "1234", "./Paola.png"));

        // Dato l'ID dell'utente in una chat:

        Utente u = utenti.get(1); // Ottieni l'oggetto

        System.out.println(u.getNome());

        TipoMessaggio ciao;
        ciao = TipoMessaggio.DOCUMENTO;
        System.out.println("Ao sei un " + ciao.getTipo());


        XMLConfigLoaderDB.DBConfig config = null;
        MySQLManager dbManager = null;

        System.out.println("1-Caricamento server.config.xml...");
        config = XMLConfigLoaderDB.caricaConfigurazione("server.config.xml");
        System.out.println("    Config OK: DB su " + config.ip + ":" + config.porta + ", DB: " + config.nomeDB);

        System.out.println("2-Creazione MySQLManager...");
        try {
            dbManager = new MySQLManager(
                    config.ip,
                    config.porta,
                    config.nomeDB,
                    config.username,
                    config.password
            );
            System.out.println("MySQLManager OK.");

            System.out.println("3-Tentativo di connessione al database...");
            dbManager.connettiti(); // Uso diretto del metodo connettiti()

            if (dbManager.isConnected()) {
                System.out.println("Connessione OK");

                // Ottengo la connessione
                Connection conn = dbManager.getConnection();

                // Operazioni sul db
                dbManager.creaHashMapUtenti(conn, utenti);
                utenti.stampaHashMap();

                System.out.println("4-Chiusura della connessione...");
                dbManager.chiudi();
            } else {
                System.out.println("Connessione fallita");
            }
        } catch (SQLException e) {
            System.err.println("Errore: " + e.getMessage());
        }
    }
}