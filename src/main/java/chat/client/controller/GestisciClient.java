package chat.client.controller;

import chat.client.Client;

import chat.common.Utente;

public class GestisciClient {
   public static GestisciClient instance;

   private Client clientChat;
   private Utente utenteLoggato;

   public GestisciClient() {
      // inizializzo clientChat, ma fino a che non ho utenteLoggato potresti rimandare la creazione
      this.clientChat = new Client(this);
   }

   public static synchronized GestisciClient getInstance() {
      if (instance == null) {
         instance = new GestisciClient();
      }
      return instance;
   }

   public Client getClientChat() {
      return clientChat;
   }

   public void setClientChat(Client clientChat) {
      this.clientChat = clientChat;
   }

   public Utente getUtenteLoggato() {
      return utenteLoggato;
   }

   public void setUtenteLoggato(Utente utenteLoggato) {
      this.utenteLoggato = utenteLoggato;
   }
}

/*
package chat.client.controller;

import chat.client.Client;
import chat.common.Utente;

// Questa classe ha le informazione sul client: l'utenteLoggato e il client stesso
public class GestisciClient {

   private Client clientChat;
   private Utente utenteLoggato;

   public GestisciClient(){
      this.clientChat = new Client(this);
   }

   public Client getClientChat() {
      return clientChat;
   }

   public Utente getUtenteLoggato() {
      return utenteLoggato;
   }

   public void setClientChat(Client clientChat) {
      this.clientChat = clientChat;
   }

   public void setUtenteLoggato(Utente utenteLoggato) {
      this.utenteLoggato = utenteLoggato;
   }
}
*/