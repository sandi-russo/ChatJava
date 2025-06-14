package chat.db;

import chat.common.Conversazione;
import chat.common.Utente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// utilizzata principalmente solo per il recupero delle conversazioni di un utente
public class GestioneChat {
    private static final Logger logger = LoggerFactory.getLogger(GestioneChat.class);
    private final MySQLManager dbManager;

    public GestioneChat(MySQLManager dbManager) {
        this.dbManager = dbManager;
    }

    // Recupera tutte le conversazioni di un utente
    public List<Conversazione> getConversazioniPerUtente(int idUtenteLoggato) throws SQLException {
        List<Conversazione> conversazioni = new ArrayList<>();

        // query per trovare tutte le chat dell'utente e per ognuna, i dati dell'altro partecipante
        String sql = """
                SELECT c.id AS id_chat, u.id AS id_altro_utente, u.username, u.nome, u.cognome, u.avatar 
                FROM chat_membri cm1 JOIN chat_membri cm2 ON cm1.chat_id = cm2.chat_id AND cm1.utente_id <> cm2.utente_id JOIN chat c ON cm1.chat_id = c.id JOIN utenti u ON cm2.utente_id = u.id 
                WHERE cm1.utente_id = ? AND c.is_group = FALSE ORDER BY c.id DESC;
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

                conversazioni.add(new Conversazione(idChat, "Chat con " + altroUtente.getUsername(), altroUtente));
            }
        } catch (SQLException e) {
            logger.error("Errore durante il recupero delle conversazioni per l'utente {}", idUtenteLoggato, e);
        }
        return conversazioni;
    }

}
