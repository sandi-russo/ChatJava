package chat.db;

import chat.common.HashMapUtenti;
import chat.common.Utente;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MySQLManager {
    // Campi per le credenziali del database
    private final String ip;
    private final int porta;
    private final String nomeDB;
    private final String username;
    private final String password;

    // Campi per la gestione dello stato della connessione
    private boolean isConnected;
    private Connection connection;

    public MySQLManager(String ip, int porta, String nomeDB, String username, String password) {
        this.ip = ip;
        this.porta = porta;
        this.nomeDB = nomeDB;
        this.username = username;
        this.password = password;
        this.isConnected = false; // Inizialmente non siamo connessi
        this.connection = null;
    }

    public boolean isConnected() {
        try {
            /* isConnected è vero solo se l'oggetto connection, non è nullo e la connessione è ancora valida. */
            return isConnected && connection != null && !connection.isClosed();
        } catch (SQLException e) {
            isConnected = false;
            return false;
        }
    }

    public void connettiti() throws SQLException {
        if (!isConnected()) { // Usiamo il metodo isConnected() per un controllo più sicuro
            String url = "jdbc:mysql://" + this.ip + ":" + this.porta + "/" + this.nomeDB +
                    "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            this.connection = DriverManager.getConnection(url, this.username, this.password);
            this.isConnected = true;
            System.out.println("Connesso al database.");
        }
    }

    public void chiudi() throws SQLException {
        if (isConnected()) {
            connection.close();
            this.isConnected = false;
            this.connection = null;
            System.out.println("Connessione al database chiusa.");
        }
    }

    public Connection getConnection() throws SQLException {
        // Se non siamo connessi, tentiamo di connetterci.
        if (!isConnected()) {
            connettiti();
        }
        return connection;
    }

    public void popolaHashMapUtenti(HashMapUtenti hashMapUtenti) throws SQLException {
        hashMapUtenti.svuota(); // Svuota la mappa prima di riempirla
        List<Utente> tuttiGliUtenti = getAllUsers();

        // Popola la mappa usando il suo metodo aggiungiUtente
        for (Utente utente : tuttiGliUtenti) {
            hashMapUtenti.aggiungiUtente(
                    utente.getId(),
                    utente.getUsername(),
                    utente.getNome(),
                    utente.getCognome(),
                    utente.getEmail(),
                    utente.getAvatar(),
                    utente.getCreatedAt()
            );
        }
    }

    public List<Utente> getAllUsers() throws SQLException {
        List<Utente> listaUtenti = new ArrayList<>();
        // Query aggiornata per non prelevare la password_hash
        String selectQuery = "SELECT id, username, nome, cognome, email, avatar, created_at FROM utenti";

        // Usiamo try-with-resources per garantire la chiusura automatica dello Statement e del ResultSet
        // La connessione la gestiamo manualmente tramite getConnection() e chiudi()
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(selectQuery)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String nome = rs.getString("nome");
                String cognome = rs.getString("cognome");
                String email = rs.getString("email");
                String avatar = rs.getString("avatar");
                LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();

                listaUtenti.add(new Utente(id, username, nome, cognome, email, avatar, createdAt));
            }
        }
        return listaUtenti;
    }
}