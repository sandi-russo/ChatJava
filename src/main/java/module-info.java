module com.example.chat {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;


    opens chat to javafx.fxml;
    exports chat;
    exports chat.utils;
    exports chat.client;
    opens chat.utils to javafx.fxml;
}