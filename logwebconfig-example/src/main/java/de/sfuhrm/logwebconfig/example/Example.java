package de.sfuhrm.logwebconfig.example;

import de.sfuhrm.logwebconfig.LogWebConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Example class showing the usage of the logging web config.
 */
public final class Example {

    /** Millis to sleep between logs. */
    private static final int THREAD_SLEEP = 1000;

    /** No instance allowed. */
    private Example() {

    }

    /** Main method of the example.
     * @param args no arguments are expected.
     * */
    public static void main(final String[] args) {
        LogWebConfig.start();
        Logger logger = LogManager.getLogger(Example.class);

        while (true) {
            logger.debug("Debug");
            logger.info("Info");
            logger.warn("Warn");
            try {
                Thread.sleep(THREAD_SLEEP);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
