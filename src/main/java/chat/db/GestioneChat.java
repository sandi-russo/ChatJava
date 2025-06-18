package chat.db;

import chat.common.Conversazione;
import chat.common.Utente;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
                    END                   AS id_altro_utente,
                
                                        
                    CASE
                        WHEN c.is_group THEN
                             CONCAT('gruppo con ',
                                    GROUP_CONCAT(DISTINCT u.username
                                                 ORDER BY u.username SEPARATOR ', '))
                        ELSE MIN(u.username)
                    END                   AS username,
     
                    CASE WHEN c.is_group THEN NULL ELSE MIN(u.nome)     END AS nome,
                    CASE WHEN c.is_group THEN NULL ELSE MIN(u.cognome)  END AS cognome,
                    CASE WHEN c.is_group THEN NULL ELSE MIN(u.avatar)   END AS avatar,
                
                    c.is_group AS is_group
                FROM chat_membri cm1   
                JOIN chat         c  ON c.id = cm1.chat_id
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
                String titolo = isGroup ? result.getString("username") : "Chat con " + altroUtente.getUsername();
                conversazioni.add(new Conversazione(idChat, titolo, altroUtente));
            }
        } catch (SQLException e) {
            logger.error("Errore durante il recupero delle conversazioni per l'utente {}", idUtenteLoggato, e);
        }
        return conversazioni;
    }

}
