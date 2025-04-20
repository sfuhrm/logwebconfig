package de.sfuhrm.logwebconfig;

import java.util.Optional;

/** A configuration plug in for a logging system. */
abstract class LogFrameworkBridge {

    /** A reference to a logger with CRUD style methods for the level. */
    interface LoggerResource {
        /** Read the current level of the logger.
         * @return the level name of the logger or {@code null} if not known.
         *  */
        String get();

        /** Update the current level of the logger.
         * @param newLevel the new level to set.
         * @throws IllegalArgumentException if the level is not acceptable.
         *  */
        void set(String newLevel);
    }

    /** Finds a log configuration resource with its resource name.
     * @param loggerName the logger name. Usually this is the
     *                 empty String for the
     *                 root logger and everything else being interpreted as a
     *                 Java fully qualified class name.
     * @return the found logger or {@code {@link Optional#empty()} if not found.
     * */
    public abstract Optional<LoggerResource> findLoggerResource(
            String loggerName);
}
