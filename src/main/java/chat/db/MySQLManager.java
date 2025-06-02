package chat.db;

import chat.common.HashMapUtenti;
import chat.common.Utente;

import java.sql.*;
import java.util.Map;

// Gestisco le connessioni al DB e le operazioni
public class MySQLManager extends GestoreDB {
    private boolean isConnected;
    private Connection connection;

    public MySQLManager(String ip, int porta, String nomeDB, String username, String password) throws SQLException {
        super(ip, porta, nomeDB, username, password);
        this.isConnected = false;
    }

    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void connettiti() throws SQLException {
        if (!isConnected) {
            String url = "jdbc:mysql://" + this.ip + ":" + this.porta + "/" + this.nomeDB +
                    "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            this.connection = DriverManager.getConnection(url, this.username, this.password);
            isConnected = true;
            System.out.println("Connesso al database");
        }
    }

    @Override
    public void chiudi() throws SQLException {
        if (isConnected && connection != null) {
            connection.close();
            isConnected = false;
            System.out.println("Connessione al database chiusa");
        }
    }

    // Restituisce la connessione attiva
    @Override
    public Connection getConnection() throws SQLException {
        if (!isConnected) {
            connettiti(); // Riutilizziamo il metodo connettiti()
        }
        return connection;
    }

    public void creaHashMapUtenti(Connection conn, HashMapUtenti utenti){
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

                // *1 - Prendo tutti i dati degli utenti dal db e li metto ad uno ad uno nella HashMap creata inizialmente
                utenti.aggiungiUtente(id, nome, cognome, email, password, avatar);
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