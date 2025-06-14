package chat.db;

import java.sql.SQLException;

// la utilizzo per definire i metodi per la gestione del db
// lo faccio per implementare il Data Access Object, ovvero per separare la logica di accesso ai dati dall'implementazione effettiva
// implemento anche AutoCloseable per poter chiudere la connessione con il db
public interface IDatabase  extends AutoCloseable{

    boolean connetti() throws SQLException;
    void disconnetti() throws SQLException;
    boolean isConnesso();

    @Override
    default void close() throws SQLException {
        disconnetti();
    }
}
