package chat.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class ColorLogger implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ColorLogger.class);

    public void logDebug(String logging) {
        LOGGER.debug("\u001B[34m{}\u001B[0m", logging); // blu
    }
    public void logInfo(String logging) {
        LOGGER.info("\u001B[32m{}\u001B[0m", logging); //verde
    }

    public void logError(String logging) {
        LOGGER.error("\u001B[31m{}\u001B[0m", logging); // rosso
    }
}
