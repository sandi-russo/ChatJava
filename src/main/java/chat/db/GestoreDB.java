package chat.db;

import java.sql.SQLException;

public abstract class GestoreDB {

    protected String ip;
    protected int porta;
    protected String nomeDB;
    protected String username;
    protected String password;

    public GestoreDB(String ip, int porta, String nomeDB, String username, String password) {
        this.ip = ip;
        this.porta = porta;
        this.nomeDB = nomeDB;
        this.username = username;
        this.password = password;
    }

    public abstract void connettiti() throws SQLException;

    public abstract void chiudi() throws SQLException;
}