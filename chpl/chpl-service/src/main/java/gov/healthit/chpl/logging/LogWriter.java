package gov.healthit.chpl.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class LogWriter {

    private LogWriter() {}

    static void write(Class clazz, String logLevel, String message) {
        final Logger LOGGER = LogManager.getLogger("loggableLogger");

        LOGGER.log(Level.INFO, message);
    }

}