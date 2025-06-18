package chat.richieste;

import java.io.Serializable;

public interface IRichiesta extends Serializable {
    // Che cosa è una richiesta?
    // Il client invia un messaggio al server per richiedere una informazione che gli venga data dal database
    // Client invia una richiesta, queste richiesta

    TipoRichiesta getTipo();
}