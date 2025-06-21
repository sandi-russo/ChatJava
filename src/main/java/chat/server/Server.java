package chat.server;

import chat.common.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class Server {

    ColorLogger colorLogger = new ColorLogger();
    // Questa HashMap associa ad ogni username di un utente il suo clientHandler (ovvero il thread) all'interno del server
    // Questa hashmap sembra non servire a niente? forse si può togliere?
    //private static ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    // Elenco di tutte le  chat presenti, ATTENZIONE questo tipo di elenco lo dovrebbe avere anche il client ma solo
    // delle chat con cui può comunicare
    private static ConcurrentHashMap<Integer, Chat> chats = new ConcurrentHashMap<>();

    private static List<ClientHandler> clientHandlers = new ArrayList<>();

    public Server() {
        int port = 5558;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server in ascolto sulla porta " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuovo client connesso");
                // Creo un thread del server per questo specifico client che si sta connettendo così se lo gestisce lui.
                new Thread(new ClientHandler(clientSocket, chats, clientHandlers)).start();
            }
        } catch (IOException e) {
           colorLogger.logError(e.getMessage());
        }
    }
}

