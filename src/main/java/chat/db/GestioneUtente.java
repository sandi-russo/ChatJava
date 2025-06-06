package chat.db;

import chat.common.Utente;
import com.password4j.Password;
import com.password4j.BcryptFunction;
import com.password4j.types.Bcrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GestioneUtente {

    private final MySQLManager dbManager;
    private static final BcryptFunction BCRYPT_FUNCTION = BcryptFunction.getInstance(Bcrypt.B, 10);

    public GestioneUtente(MySQLManager dbManager) {
        this.dbManager = dbManager;
    }

    public void registraUtente(String username, String nome, String cognome, String email, String password, String avatar) throws UserRegistrationException, SQLException {
        Connection conn = dbManager.getConnection();

        if (userExists(conn, "username", username)) {
            throw new UserRegistrationException("Username già in uso. Scegline un altro.");
        }
        if (userExists(conn, "email", email)) {
            throw new UserRegistrationException("Email già registrata. Usa un'altra email.");
        }

        String passwordHash = Password.hash(password).with(BCRYPT_FUNCTION).getResult();

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
    }

    public Utente login(String username, String passwordInChiaro) throws SQLException, LoginException {
        Connection con = dbManager.getConnection();
        String sql = "SELECT * FROM utenti WHERE username = ?";

        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                // ERRORE LOGICO CORRETTO: se rs.next() è VERO, l'utente esiste e procediamo.
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
                                // NOME COLONNA CORRETTO: 'avatar_path'
                                resultSet.getString("avatar"),
                                resultSet.getTimestamp("created_at").toLocalDateTime()
                        );
                    }
                }

                // se arriviamo qui, o l'utente non esiste o la password è errata.

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
}
