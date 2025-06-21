package chat.db;

import chat.common.ColorLogger;
import chat.common.Conversazione;
import chat.common.Messaggio;
import chat.common.Utente;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// utilizzata principalmente solo per il recupero delle conversazioni di un utente
public class GestioneChat {
    ColorLogger colorLogger = new ColorLogger();
    private final MySQLManager dbManager;

    public GestioneChat(MySQLManager dbManager) {
        this.dbManager = dbManager;
    }

    // Recupera tutte le conversazioni di un utente
    public ObservableList<Conversazione> getConversazioniPerUtente(int idUtenteLoggato) throws SQLException {
        //ObservableList<Conversazione> conversazioni = new ObservableList<Conversazione>();
        ObservableList<Conversazione> conversazioni = FXCollections.observableArrayList();

        // query per trovare tutte le chat dell'utente e per ognuna, i dati dell'altro partecipante
        String sql = """
                SELECT
                    c.id AS id_chat,
                    CASE
                        WHEN c.is_group THEN 0
                        ELSE MIN(u.id)
                    END AS id_altro_utente,
                    CASE
                        WHEN c.is_group THEN c.nome_chat
                        ELSE MIN(u.username)
                    END AS username,
                    CASE WHEN c.is_group THEN NULL ELSE MIN(u.nome)     END AS nome,
                    CASE WHEN c.is_group THEN NULL ELSE MIN(u.cognome)  END AS cognome,
                    CASE WHEN c.is_group THEN NULL ELSE MIN(u.avatar)   END AS avatar,
                    c.is_group AS is_group
                FROM chat_membri cm1
                JOIN chat c ON c.id = cm1.chat_id
                LEFT JOIN chat_membri cm2
                       ON cm2.chat_id = c.id
                      AND cm2.utente_id <> cm1.utente_id
                LEFT JOIN utenti u ON u.id = cm2.utente_id
                WHERE cm1.utente_id = ?
                GROUP BY c.id
                ORDER BY c.id DESC;                
                """;

        try (Connection conn = dbManager.getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, idUtenteLoggato);
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                // per ogni riga ci andiamo a creare l'oggetto Conversazione
                int idChat = result.getInt("id_chat");
                Utente altroUtente = new Utente(
                        result.getInt("id_altro_utente"),
                        result.getString("username"),
                        result.getString("nome"),
                        result.getString("cognome"),
                        null, // questo lo inserisco ma sarà null perché non lo uso nella query
                        result.getString("avatar"),
                        null // createdAt non ci interessa
                );

                boolean isGroup = result.getBoolean("is_group");
                String titolo = isGroup ? result.getString("username") : altroUtente.getUsername();
                conversazioni.add(new Conversazione(idChat, titolo, altroUtente));
            }
        } catch (SQLException e) {
            colorLogger.logError("Errore durante il recupero delle conversazioni per l'utente " + idUtenteLoggato);
        }
        return conversazioni;
    }


    // Cambiare in getMessaggiPerChat(int idChat)
    public List<Messaggio> getMessaggiPerChat(int idChat) throws SQLException {
        List<Messaggio> messaggi = new ArrayList<>();

        String sql = """
                SELECT 
                    m.id AS id_messaggio,
                    m.contenuto AS testo_messaggio,
                    m.sent_at AS data_invio,
                    m.mittente_id AS id_mittente,
                    m.chat_id AS id_chat,
                    m.tipo_messaggio
                FROM 
                    messaggi m
                WHERE 
                    m.chat_id = ?
                ORDER BY 
                    m.sent_at ASC
                """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, idChat);
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                Messaggio messaggio = new Messaggio(
                        result.getString("testo_messaggio"),
                        result.getInt("id_mittente"),
                        result.getInt("id_chat"),
                        result.getTimestamp("data_invio").toInstant()
                );

                messaggio.setId(result.getInt("id_messaggio"));
                messaggi.add(messaggio);
            }
        } catch (SQLException e) {
            colorLogger.logError("Errore durante il recupero dei messaggi per la chat " + idChat);
            throw e;
        }

        colorLogger.logInfo("Recuperati " + messaggi.size() + " messaggi per la chat " + idChat);
        return messaggi;
    }

    public List<Utente> getUtentiPerChat(int idChat) throws SQLException {
        List<Utente> utenti = new ArrayList<>();
        String query = "SELECT u.* FROM utenti u " +
                "JOIN chat_membri cm ON u.id = cm.utente_id " +
                "WHERE cm.chat_id = ?";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(query)) {
            stmt.setInt(1, idChat);
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
        return utenti;
    }

    public int creaNuovaChat(boolean isGruppo, String nomeGruppo) throws SQLException {
        String sql = "INSERT INTO chat (nome_chat, is_group) VALUES (?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Se è un gruppo, usa il nome specificato, altrimenti NULL
            if (isGruppo && nomeGruppo != null && !nomeGruppo.isEmpty()) {
                stmt.setString(1, nomeGruppo);
            } else {
                stmt.setNull(1, java.sql.Types.VARCHAR);
            }

            stmt.setBoolean(2, isGruppo);

            int righeInserite = stmt.executeUpdate();

            if (righeInserite > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int idChat = generatedKeys.getInt(1);
                        return idChat;
                    } else {
                        throw new SQLException("Creazione chat fallita, nessun ID ottenuto.");
                    }
                }
            } else {
                throw new SQLException("Creazione chat fallita, nessuna riga inserita.");
            }
        } catch (SQLException e) {
            colorLogger.logError("Errore durante la creazione della chat: " + e.getMessage());
            throw e;
        }
    }

    public void aggiungiUtenteAChat(int idChat, int idUtente) throws SQLException {
        String sql = "INSERT INTO chat_membri (chat_id, utente_id) VALUES (?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idChat);
            stmt.setInt(2, idUtente);

            stmt.executeUpdate();
        }
    }

    public Conversazione getConversazionePerId(int idChat, int idUtenteLoggato) throws SQLException {
        String sql = """
            SELECT
                c.id AS id_chat,
                c.nome_chat,
                CASE
                    WHEN c.is_group THEN 0
                    ELSE MIN(u.id)
                END AS id_altro_utente,
                CASE
                    WHEN c.is_group THEN c.nome_chat
                    ELSE MIN(u.username)
                END AS username,
                CASE WHEN c.is_group THEN NULL ELSE MIN(u.nome) END AS nome,
                CASE WHEN c.is_group THEN NULL ELSE MIN(u.cognome) END AS cognome,
                CASE WHEN c.is_group THEN NULL ELSE MIN(u.avatar) END AS avatar,
                c.is_group AS is_group
            FROM chat c
            LEFT JOIN chat_membri cm ON c.id = cm.chat_id AND cm.utente_id <> ?
            LEFT JOIN utenti u ON u.id = cm.utente_id
            WHERE c.id = ?
            GROUP BY c.id, c.nome_chat
            """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUtenteLoggato);
            stmt.setInt(2, idChat);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Utente altroUtente = new Utente(
                            rs.getInt("id_altro_utente"),
                            rs.getString("username"),
                            rs.getString("nome"),
                            rs.getString("cognome"),
                            null,
                            rs.getString("avatar")
                    );

                    boolean isGroup = rs.getBoolean("is_group");
                    String titolo;

                    if (isGroup) {
                        String nomeChat = rs.getString("nome_chat");
                        titolo = nomeChat != null && !nomeChat.isEmpty() ? nomeChat : "Gruppo";
                    } else {
                        // Per chat private
                        titolo = altroUtente.getUsername();
                    }

                    return new Conversazione(idChat, titolo, altroUtente);
                }
            }
        }

        return null;
    }

    public Integer trovaChatPrivata(int utente1, int utente2) throws SQLException {
        String query = """
        SELECT chat_id
        FROM chat_membri
        GROUP BY chat_id
        HAVING COUNT(*) = 2
        AND SUM(utente_id = ?) = 1
        AND SUM(utente_id = ?) = 1
        ORDER BY chat_id DESC
        """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, utente1);
            stmt.setInt(2, utente2);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("chat_id");
                } else {
                    return null; // Nessuna chat trovata
                }
            }
        }
    }
}
