package chat.client;

import chat.common.Chat;
import chat.common.Messaggio;
import chat.common.Utente;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    // L'id scelto ora si può togliere, mi serve solo per testare client diversi senza cambiare tutto manualmente ogni volta
    Utente utenteClient;
    Chat chatAttuale;

    public Client() throws IOException {
        Scanner scannerIniziale = new Scanner(System.in);

        try {
            System.out.println("Scegli un account tra 1, 2 e 3 (WIP: Questo esiste solo per testing)");
            int idSceltoOra = scannerIniziale.nextInt();
            System.out.println("Scegli l'id della chat in cui vuoi scrivere, se la chat non esiste ne verrà creata una nuova dal server (WIP: Questo esiste solo per testing)");
            int chatSceltaOra = scannerIniziale.nextInt();

            if(idSceltoOra == 1){
                utenteClient = new Utente(1, "PAODOS", "ASDSAD", "persi", "123", "./antnrn.png", null);
            } else if(idSceltoOra == 2){
                utenteClient = new Utente(2, "Snndi", "Rususo", "fessi", "nonloso", "./sandnr.png", null);
            } else {
                throw new IllegalArgumentException("Hai inserito un numero diverso da 1 o 2 o 3");
            }

            chatAttuale = new Chat(chatSceltaOra);
        } catch (Exception e) {
            System.out.println("ERRORE: Non hai inserito un numero intero valido.");
            System.exit(1);  // Termina il programma con codice di uscita 1
        }

        Socket socket = new Socket("localhost", 5558);

        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

        // Lettore dei messaggi in arrivo (su un altro thread)
        new Thread(() -> {
            Chat nuovaChat;
            while(true) {
                try {
                    // L'object letto va per forza castato a chat, altimenti non sembra andare
                    while ((nuovaChat = (Chat) in.readObject()) != null) {
                        System.out.println("Messaggi ricevuti: " + nuovaChat.getMessaggi().size());
                        chatAttuale = nuovaChat;
                        chatAttuale.stampaMessaggi();
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // Scrivi verso il server
        String userInput;
        // Manda come primo messaggio l'utente che si sta connettendo al server
        out.writeObject(utenteClient);

        out.writeObject(chatAttuale); // MANDO LA CHAT DA VISUALIZZARE
        out.flush();
        while (true) {
            String str = null;
            try {
                Scanner scanner = new Scanner(System.in); // crea lo scanner

                System.out.print("Inserisci una parola: ");
                str = scanner.nextLine(); // legge una singola frase

                if (str != null) {
                    // Crea il messaggio:
                    /*
                    Il testo è str
                    L'utente che lo sta mandando lo prende direttamente dal client con utenteClient.getId()
                    La chat su cui vuole mandare il messaggio è chatAttuale.getId()
                     */
                    Messaggio msg = new Messaggio(str, utenteClient.getId(), chatAttuale.getId());
                    if (msg.getTesto().equalsIgnoreCase("basta")) {
                        break;
                    }

                    // Mando al server il messaggio che ho appena creato
                    out.writeObject(msg);
                    out.flush();
                }
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }
        }

        socket.close();
    }
}
