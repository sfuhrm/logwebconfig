package de.sfuhrm.logwebconfig;

import java.util.Optional;

/** A configuration plug in for a logging system. */
abstract class LogConfigurator {

    /** A reference to a logger with CRUD style methods for the level. */
    interface Resource {
        /** Read the current level of the logger.
         * @return the level name of the logger or {@code null} if not known.
         *  */
        String read();

        /** Update the current level of the logger.
         * @param newLevel the new level to set.
         * @throws IllegalArgumentException if the level is not acceptable.
         *  */
        void update(String newLevel);
    }

    /** Finds a log configuration resource with its resource name.
     * @param resource the logger name. Usually this is the empty String for the
     *                 root logger and everything else being interpreted as a
     *                 Java fully qualified class name.
     * @return the found logger or {@code {@link Optional#EMPTY} if not found.
     * */
    public abstract Optional<Resource> findResource(String resource);
}
