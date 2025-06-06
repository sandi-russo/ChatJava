package chat.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

public class XMLConfigLoaderDB {

    public static class DBConfig {
        public String ip;
        public String nomeDB;
        public String username;
        public String password;
        public int porta;
    }

    public static DBConfig caricaConfigurazione(String nomeFileXML) {
        DBConfig config = new DBConfig();

        try (InputStream inputStream = XMLConfigLoaderDB.class.getClassLoader().getResourceAsStream(nomeFileXML)) {

            if (inputStream == null) {
                throw new RuntimeException("File di configurazione non trovato: " + nomeFileXML);
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputStream);
            doc.getDocumentElement().normalize();

            Element rootElement = doc.getDocumentElement();
            if (!"database".equalsIgnoreCase(rootElement.getNodeName())) {
                throw new RuntimeException("Elemento root XML atteso: '<database>', trovato: '<" + rootElement.getNodeName() + ">'");
            }

            config.ip = getTagValue(rootElement, "ip");
            String portaStr = getTagValue(rootElement, "porta");
            try {
                config.porta = Integer.parseInt(portaStr);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Porta non valida ('" + portaStr + "'): non è un numero.", e);
            }
            config.nomeDB = getTagValue(rootElement, "nomeDB");
            config.username = getTagValue(rootElement, "username");
            config.password = getTagValue(rootElement, "password");

            return config;

        } catch (Exception e) {
            // Se è già una RuntimeException (dalle nostre chiamate a throw o da parseInt), rilanciala.
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("Errore caricamento/parsing file XML: " + e.getMessage(), e);
        }
    }

    private static String getTagValue(Element parentElement, String tagName) {
        NodeList nodeList = parentElement.getElementsByTagName(tagName);
        if (nodeList.getLength() == 0) {
            throw new RuntimeException("Tag XML mancante: <" + tagName + ">");
        }
        String value = nodeList.item(0).getTextContent();
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException("Valore vuoto per tag XML: <" + tagName + ">");
        }
        return value.trim();
    }
}