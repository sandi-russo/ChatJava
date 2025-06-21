package chat.db;

import chat.common.ColorLogger;
import java.sql.Connection;
import java.sql.SQLException;

// implemento le funzionalità di base del db
public abstract class ADatabase implements IDatabase {
    ColorLogger colorLogger = new ColorLogger();
    protected Connection connection;
    protected String dbUrl;
    protected String username;
    protected String password;

    public ADatabase(String dbUrl, String username, String password) {
        this.dbUrl = dbUrl;
        this.username = username;
        this.password = password;
        this.connection = null;
    }

    // controllo se la connessione esiste o è aperta
    @Override
    public boolean isConnesso() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            colorLogger.logError("Errore nel controllare lo stato del connessione: " + e.getMessage());
            return false;
        }
    }

    // chiudo la connessone se aperta
    @Override
    public void disconnetti() throws SQLException {
        if (isConnesso()) {
            try {
                connection.close();
                colorLogger.logInfo("Disconnessione dal database avvenuta con successo.");
            } finally {
                connection = null;
            }
        }
    }
}
