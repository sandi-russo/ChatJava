package chat.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    private static Main instance;
    private Stage primaryStage;

    // quando l'app parte, salva una sua istanza per essere richiamata da altre classi
    public Main() {
        instance = this;
    }

    // uso questo per ottenere l'istanza corrente dell'applicazione
    public static Main getInstance() {
        return instance;
    }

    @Override
    public void start(Stage stage) throws IOException {
        this.primaryStage = stage;
        // all'avvio, carichiamo la schermata di login
        navigateTo("login.fxml");
        stage.setTitle("Chat");
        stage.show();
    }

    public Object navigateTo(String fxmlFile) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/" + fxmlFile));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setScene(scene);
        primaryStage.sizeToScene(); // faccio in modo che la finestra si adatti ai contenuti
        primaryStage.setTitle("Chat - " + fxmlFile.replace(".fxml", ""));
        return fxmlLoader.getController(); // Ritorno il controller per poterci interagire
    }

    public static void main(String[] args) {
//        try {
//            Client client = new Client();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        // chiamo il metodo sul controller per passargli i dati dell'utente
        // adesso, qui viene creato il CLIENT, parte da qui.
        launch();
    }
}