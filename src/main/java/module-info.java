module com.example.chat {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires password4j;
    requires org.slf4j;

    exports chat.utils;
    exports chat.client;
    opens chat.utils to javafx.fxml;
    exports chat.client.controller;
    opens chat.client.controller to javafx.fxml;
}