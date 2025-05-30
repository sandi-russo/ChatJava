package chat.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLManager extends GestoreDB {

    private Connection conn;

    public MySQLManager(String ip, int porta, String nomeDB, String username, String password) {
        super(ip, porta, nomeDB, username, password);
    }

    @Override
    public void connettiti() throws SQLException {
        String url = "jdbc:mysql://" + this.ip + ":" + this.porta + "/" + this.nomeDB +
                "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        this.conn = DriverManager.getConnection(url, this.username, this.password);
        System.out.println("Connesso");
    }

    @Override
    public void chiudi() throws SQLException {
        if (this.conn != null && !this.conn.isClosed()) {
            this.conn.close();
        }
    }

// Restituisce la connessione attiva
    public Connection getConnection() throws SQLException {
        if (this.conn == null || this.conn.isClosed()) {
            throw new SQLException("Connessione al database non disponibile o chiusa.");
        }
        return this.conn;
    }
}