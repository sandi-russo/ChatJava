package chat.client;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

public class GestoreFeedbackUI {

    public static void mostraErrore(Label etichetta, String messaggio) {
        if (etichetta != null) {
            etichetta.setText(messaggio);
            etichetta.setTextFill(Color.RED);
        }
    }

    public static void mostraSuccesso(Label etichetta, String messaggio) {
        if (etichetta != null) {
            etichetta.setText(messaggio);
            etichetta.setTextFill(Color.GREEN);
        }
    }
}
