package chat;

import chat.common.HashMapUtenti;
import chat.db.MySQLManager;
import chat.utils.XMLConfigLoaderDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    // Questo main è utile per testare la logica del database dalla console.
    public static void main(String[] args) {

        // La Map è un dizionario chiave valore, in questo caso ID (intero) - Utente.
        HashMapUtenti utenti = new HashMapUtenti();

        XMLConfigLoaderDB.DBConfig config;
        MySQLManager dbManager;

        System.out.println("1-Caricamento server.config.xml...");
        try {
            config = XMLConfigLoaderDB.caricaConfigurazione("server.config.xml");
            System.out.println("Config OK: DB su " + config.ip + ":" + config.porta + ", DB: " + config.nomeDB);

            System.out.println("2-Creazione MySQLManager...");
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

                System.out.println("4-Popolamento della mappa utenti...");
                dbManager.popolaHashMapUtenti(utenti);
                System.out.println("Mappa popolata con " + utenti.getUtenti().size() + " utenti.");


                System.out.println("\n--- STAMPA DEGLI UTENTI CARICATI ---");
                System.out.println(utenti);


                System.out.println("5-Chiusura della connessione...");
                dbManager.chiudi();
                System.out.println("Connessione chiusa.");

            } else {
                System.err.println("Connessione fallita");
            }
        } catch (SQLException e) {
            System.err.println("ERRORE SQL: " + e.getMessage());
            logger.error("ERRORE SQL: {}", e.getMessage());
        } catch (Exception e) {
            // Cattura altre eccezioni, come quelle dal caricamento del file XML
            System.err.println("ERRORE GENERICO: " + e.getMessage());
            logger.error("ERRORE GENERICO: {}", e.getMessage());
        }
    }
}