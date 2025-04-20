package de.sfuhrm.logwebconfig;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.Optional;

/** Configuration class for log4j2.
 * */
class Log4J2Bridge extends LogFrameworkBridge {
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
    public Optional<LogFrameworkBridge.LoggerResource> findLoggerResource(
            final String loggerName) {
        if (loggerName == null
                || loggerName.isEmpty()
                || "/".equals(loggerName)) {
            return Optional.of(new RootLoggerResource());
        }
        return Optional.of(new LoggerResource(loggerName));
    }

    /** Dynamic method for {@link LogManager#getRootLogger()}
     * to support mocking.
     * @return the root logger.
     * */
    Logger getRootLogger() {
        return LogManager.getRootLogger();
    }

    /** Dynamic method for {@link Configurator#setRootLevel(Level)}
     * to support mocking.
     * @param level the level to configure.
     * */
    void setRootLevel(final Level level) {
        Configurator.setRootLevel(level);
    }

    /** Resource representing the root logger.
     * */
    private class RootLoggerResource
            implements LogFrameworkBridge.LoggerResource {
        @Override
        public String get() {
            return getRootLogger().getLevel().toString();
        }

        @Override
        public void set(final String newLevel) {
            setRootLevel(parseLevel(newLevel));
        }
    }

    /** Dynamic method for {@link org.apache.log4j.LogManager#getLogger(String)}
     * to support mocking.
     * @param logger the name of the logger to get.
     * @return the requested logger instance.
     * */
    Logger getLogger(final String logger) {
        return LogManager.getLogger(logger);
    }

    /** Dynamic method for {@link Configurator#setLevel(String, Level)}
     * to support mocking.
     * @param logger the name of the logger to configure the level for.
     * @param level the level to set.
     * */
    void setLevel(final String logger, final Level level) {
        Configurator.setLevel(logger, level);
    }


    /** Resource representing a named logger.
     * */
    private class LoggerResource implements LogFrameworkBridge.LoggerResource {
        /** The name of the logger to configure. */
        private String logger;

        /** Constructor for the resource.
         * @param inLogger the name of the logger to configure.
         *  */
        LoggerResource(final String inLogger) {
            this.logger = inLogger;
        }

        @Override
        public String get() {
            return getLogger(logger).getLevel().toString();
        }

        @Override
        public void set(final String newLevel) {
            setLevel(logger, parseLevel(newLevel));
        }
    }
}
