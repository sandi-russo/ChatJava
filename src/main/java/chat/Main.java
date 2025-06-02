package chat;

import chat.common.HashMapUtenti;
import chat.db.MySQLManager;
import chat.utils.XMLConfigLoaderDB;
import java.sql.*;

import java.io.IOException;


public class Main {

    public static void main(String[] args) throws SQLException {
        //launch();

        // La Map è un dizionario chiave valore, in questo caso ID (intero) - Utente.
        HashMapUtenti utenti = new HashMapUtenti();

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