package chat.db;

import chat.common.Utente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MySQLManager extends ADatabase {
    private static final Logger logger = LoggerFactory.getLogger(MySQLManager.class);

    // variabile d’istanza per la connessione
    private Connection connection;

    public MySQLManager(String ip, int porta, String nomeDB, String username, String password) {
        super(
                "jdbc:mysql://" + ip + ":" + porta + "/" + nomeDB +
                        "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                username,
                password
        );
    }

    // implemento l'interfaccia di IDatabase
    @Override
    public boolean connetti() throws SQLException {
        if (isConnesso()) {
            return true; // siamo già connessi
        }
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            logger.info("Connessione al database effettuata con successo.");
            return true;
        } catch (SQLException e) {
            logger.error("Errore durante la connessione al database: {}", e.getMessage());
            throw e;
        }
    }

    // ottengo le informazioni generali sulla connessione
    public Connection getConnection() throws SQLException {
        if (!isConnesso()) {
            connetti();
        }
        return connection;
    }

    // lo utilizzo per restituire una lista di tutti gli utenti all'interno del DB
    public List<Utente> getAllUsers() throws SQLException {
        List<Utente> listaUtenti = new ArrayList<>();
        String selectQuery = "SELECT id, username, nome, cognome, email, avatar, created_at FROM utenti";

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