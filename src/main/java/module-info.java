module com.example.chat {
    requires javafx.controls;
    requires javafx.fxml;


    opens chat to javafx.fxml;
    exports chat;
}