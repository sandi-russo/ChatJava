package chat.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/welcome.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 500);
        stage.setTitle("Chat");
        stage.setScene(scene);
        stage.sizeToScene(); // faccio in modo che la finestra si adatti ai contenuti
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}