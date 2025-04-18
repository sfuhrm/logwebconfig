package de.sfuhrm.logwebconfig;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Optional;

/** Configuration class for log4j1.
 * */
class Log4j1Configurator extends LogConfigurator {
    /**
     * Converts a level to a Log4j level object.
     * @param levelString the level String to recognize.
     * @return the resulting level object.
     * @throws IllegalArgumentException if the level was not recognized.
     */
    private static Level parseLevel(final String levelString) {
        Level level = Level.toLevel(levelString.toUpperCase(), null);
        if (level == null) {
            throw new IllegalArgumentException("Level not recognized: "
                    + levelString);
        }
        return level;
    }

    @Override
    public Optional<Resource> findResource(final String resource) {
        if (resource == null || resource.isEmpty() || resource.equals("/")) {
            return Optional.of(new RootLoggerResource());
        }
        return Optional.of(new LoggerResource(resource));
    }

    /** Dynamic method for {@link LogManager#getRootLogger()}
     * to support mocking.
     * @return the root logger.
     * */
    Logger getRootLogger() {
        return LogManager.getRootLogger();
    }

    /** Resource representing the root logger.
     * */
    private class RootLoggerResource implements Resource {
        @Override
        public String read() {
            return getRootLogger().getLevel().toString();
        }

        @Override
        public void update(final String newLevel) {
            getRootLogger().setLevel(parseLevel(newLevel));
        }
    }

    /** Dynamic method for {@link LogManager#getLogger(String)}
     * to support mocking.
     * @param logger the name of the logger to get.
     * @return the requested logger.
     * */
    Logger getLogger(final String logger) {
        return LogManager.getLogger(logger);
    }


    /** Resource representing a named logger.
     * */
    private class LoggerResource implements Resource {
        /** The name of the logger to configure. */
        private String logger;

        /** Constructor for the resource.
         * @param inLogger the name of the logger to configure.
         *  */
        LoggerResource(final String inLogger) {
            this.logger = inLogger;
        }

        @Override
        public String read() {
            return getLogger(logger).getLevel().toString();
        }

        @Override
        public void update(final String newLevel) {
            getLogger(logger).setLevel(parseLevel(newLevel));
        }
    }
}
