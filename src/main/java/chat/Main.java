package chat;

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

        /*
        // Uso questa parte qui per fare dei test con la Map (chiave valore) che servirà al server per caricare gli utenti e alla chat per sapere quali utenti sono abilitati in quella chat
        System.out.println("Ciao!");

        // La Map è un dizionario chiave valore, in questo caso ID - Utente.
        Map<Integer, Utente> utenti = new HashMap<>();
        // A inizio server (o anche durante ogni volta che si aggiorna) carica tutti gli utenti presenti nel db o in ogni chat
        utenti.put(1, new Utente(1, "Paola", "Cariddi", "Parola.Cariddi@gmail.com", "1234", "./Paola.png"));
        utenti.put(2, new Utente(2, "Paola", "Cariddi", "Parola.Cariddi@gmail.com", "1234", "./Paola.png"));
        */
        // Dato l'ID dell'utente in una chat:
        /*
        Utente u = utenti.get(1); // Ottieni l'oggetto

        System.out.println(u.getNome());

        TipoMessaggio ciao;
        ciao = TipoMessaggio.DOCUMENTO;
        System.out.println("Ao sei un " + ciao);
        */



        XMLConfigLoaderDB.DBConfig config = null;
        MySQLManager dbManager = null;

        System.out.println("[1] Caricamento server.config.xml...");
        config = XMLConfigLoaderDB.caricaConfigurazione("server.config.xml");
        System.out.println("    Config OK: DB su " + config.ip + ":" + config.porta + ", DB: " + config.nomeDB);

        System.out.println("[2] Creazione MySQLManager...");
        dbManager = new MySQLManager(
                config.ip,
                config.porta,
                config.nomeDB,
                config.username,
                config.password
        );
        System.out.println("    MySQLManager OK.");

        System.out.println("[3] Tentativo di chiamata a connettiti()...");
        dbManager.connettiti();
        Connection conn = dbManager.getConnection(); // ottengo la connessione

        // Query che ci interessa
        String selectTuttiGliUtenti = "SELECT id, nome, cognome, email, password, avatar FROM Utenti";

        try(Statement query = conn.createStatement()){
            ResultSet risultato = query.executeQuery(selectTuttiGliUtenti);
            boolean trovati = false;    // Se non trova nulla
            while (risultato.next()) {
                trovati = true; // Finché nel record set (risultato) che abbiamo preso tramite la query ci sono altre righe, fai il while
                int id = risultato.getInt("id");
                String nome = risultato.getString("nome");
                String cognome = risultato.getString("cognome");
                String email = risultato.getString("email");
                String password = risultato.getString("password"); // ATTENZIONE: Evita di stampare password in produzione!
                String avatar = risultato.getString("avatar");

                System.out.printf("ID: %-3d | Nome: %-15s | Cognome: %-15s | Email: %-25s | Password: %-10s | Img: %s\n",
                        id, nome, cognome, email, password, avatar);
            }
            if (!trovati) {
                System.out.println("Nessun utente trovato nella tabella 'utenti' o la tabella è vuota.");
            }
        } catch (SQLException eQuery) {
            System.err.println("Errore nella query");
            System.err.println("    Query: " + selectTuttiGliUtenti);
            System.err.println("    Messaggio: " + eQuery.getMessage());
        }

    }
}