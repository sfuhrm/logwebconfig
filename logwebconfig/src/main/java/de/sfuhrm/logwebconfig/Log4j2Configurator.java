package de.sfuhrm.logwebconfig;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.Optional;

/** Configuration class for log4j2.
 * */
class Log4j2Configurator extends LogConfigurator {
    /**
     * Converts a level to a Log4j2 level object.
     * @param levelString the level String to recognize.
     * @return the resulting level object.
     * @throws IllegalArgumentException if the level was not recognized.
     */
    private static Level parseLevel(final String levelString) {
        Level level = Level.getLevel(levelString.toUpperCase());
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

    /** Resource representing the root logger.
     * */
    private static class RootLoggerResource implements Resource {
        @Override
        public String read() {
            return null;
        }

        @Override
        public void update(final String newLevel) {
            Configurator.setRootLevel(parseLevel(newLevel));
        }
    }

    /** Resource representing a named logger.
     * */
    private static class LoggerResource implements Resource {
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
            return null;
        }

        @Override
        public void update(final String newLevel) {
            Configurator.setLevel(logger, parseLevel(newLevel));
        }
    }
}
