package chat.db;

import chat.common.Utente;
import com.password4j.Password;
import com.password4j.BcryptFunction;
import com.password4j.types.Bcrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GestioneUtente {

    private final MySQLManager dbManager;
    private static final BcryptFunction BCRYPT_FUNCTION = BcryptFunction.getInstance(Bcrypt.B, 10);
    private static final Logger logger = LoggerFactory.getLogger(GestioneUtente.class);


    public GestioneUtente(MySQLManager dbManager) {
        this.dbManager = dbManager;
    }

    public Utente registraUtente(String username, String nome, String cognome, String email, String password, String avatar) throws UserRegistrationException, SQLException {
        Connection conn = dbManager.getConnection();

        if (userExists(conn, "username", username)) {
            throw new UserRegistrationException("Username già in uso. Scegline un altro.");
        }
        if (userExists(conn, "email", email)) {
            throw new UserRegistrationException("Email già registrata. Usa un'altra email.");
        }

        String passwordHash = Password.hash(password).with(BCRYPT_FUNCTION).getResult();

        // creo la query per inserire l'utente nella tabella utenti
        String sql = "INSERT INTO utenti (username, nome, cognome, email, password, avatar) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, nome);
            statement.setString(3, cognome);
            statement.setString(4, email);
            statement.setString(5, passwordHash);
            statement.setString(6, avatar);
            statement.executeUpdate();
        }
        return null;
    }

    // controllo se l'utente esiste e poi controllo la password
    public Utente login(String username, String passwordInChiaro) throws SQLException, LoginException {
        Connection con = dbManager.getConnection();
        String sql = "SELECT * FROM utenti WHERE username = ?";

        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {

                    String hashSalvato = resultSet.getString("password");

                    boolean passwordCorrisponde = Password.check(passwordInChiaro, hashSalvato).with(BCRYPT_FUNCTION);

                    if (passwordCorrisponde) {
                        return new Utente(
                                resultSet.getInt("id"),
                                resultSet.getString("username"),
                                resultSet.getString("nome"),
                                resultSet.getString("cognome"),
                                resultSet.getString("email"),
                                resultSet.getString("avatar"),
                                resultSet.getTimestamp("created_at").toLocalDateTime()
                        );
                    }
                }
                // se sono qui l'utente non esiste o la password è errata.
                throw new LoginException("Username o password non corretti.");
            }
        }
    }

    private boolean userExists(Connection conn, String fieldName, String value) throws SQLException {
        String sql = "SELECT COUNT(*) FROM utenti WHERE " + fieldName + " = ?";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, value);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public static class UserRegistrationException extends Exception {
        public UserRegistrationException(String message) {
            super(message);
        }
    }

    public static class LoginException extends Exception {
        public LoginException(String message) {
            super(message);
        }
    }

    public List<Utente> getListaUtenti(String filtro) throws SQLException {
        List<Utente> utenti = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT * FROM utenti");
        if (filtro != null && !filtro.isEmpty()) {
            sql.append(" WHERE username LIKE ? OR nome LIKE ? OR cognome LIKE ?");
        }
        sql.append(" ORDER BY username");

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            if (filtro != null && !filtro.isEmpty()) {
                String param = "%" + filtro + "%";
                stmt.setString(1, param);
                stmt.setString(2, param);
                stmt.setString(3, param);
            }

            logger.info("Esecuzione query: {}", sql);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Utente utente = new Utente(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("nome"),
                            rs.getString("cognome"),
                            rs.getString("email"),
                            rs.getString("avatar")
                    );
                    utenti.add(utente);
                }
            }
        }

        logger.info("Recuperati {} utenti dal database", utenti.size());
        return utenti;
    }
}
