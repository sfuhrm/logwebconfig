package de.sfuhrm.logwebconfig;

import java.io.IOException;

/** The setup class for the log web configuration service.
 * */
public final class LogWebConfig {
    /** The default port to listen to. */
    private static final String DEFAULT_HOST = "127.0.0.1";
    /** The default port to listen to. */
    private static final String DEFAULT_PORT = "19293";
    /** System property to configure the TCP/IP server port to listen to. */
    private static final String PROPERTY_PORT = "LOGWEBCONFIG_PORT";
    /** System property to configure the host address to listen to. */
    private static final String PROPERTY_HOST = "LOGWEBCONFIG_HOST";
    /** System property to configure whether the server is started or not. */
    private static final String PROPERTY_ENABLE = "LOGWEBCONFIG_ENABLE";

    /** The singleton server instance. */
    private static Server singleton;

    /** No instance allowed. */
    private LogWebConfig() {

    }

    /** Stop the web config server. Do nothing if the server is
     * already stopped.
     * */
    public static synchronized void stop() {
        if (singleton == null) {
            return;
        }
        singleton.stop();
        singleton = null;
    }

    /** Start the web config server.
     * @throws RuntimeException if the server can not be started due to an
     * IOException.
     * */
    public static synchronized void start() {
        if (singleton != null) {
            return;
        }

        String enabled = System.getProperty(PROPERTY_ENABLE);
        if (enabled != null && !Boolean.valueOf(enabled)) {
            return;
        }

        String host = System.getProperty(PROPERTY_HOST, DEFAULT_HOST);
        int port = Integer.parseInt(
                System.getProperty(PROPERTY_PORT, DEFAULT_PORT));
        try {
            singleton = new Server(host, port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
