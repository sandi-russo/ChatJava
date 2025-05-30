module com.example.chat {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens chat to javafx.fxml;
    exports chat;
}