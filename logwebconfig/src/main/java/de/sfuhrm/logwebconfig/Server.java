package de.sfuhrm.logwebconfig;

import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/** HTTP server for handling RESTy requests and passing them on to log4j2.
 * */
final class Server extends NanoHTTPD {

    /** The request parameter name for the log level to configure. */
    static final String PARAM_LEVEL = "level";

    /** Constructs a new server and starts it.
     * @param hostname the host name to listen to, or {@code null}
     *                 for listen to all addresses.
     * @param port the TCP/IP port to listen on.
     * @throws IOException when not able to binding to the port.
     * */
    Server(final String hostname, final int port) throws IOException {
        super(hostname, port);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, true);
    }

    @Override
    public Response serve(final IHTTPSession session) {
        try {
            Method method = session.getMethod();
            switch (method) {
                case PUT:
                    configure(session);
                    break;
                default:
                    throw new ServerException(
                            Response.Status.METHOD_NOT_ALLOWED,
                            method.toString());
            }
        } catch (ServerException e) {
            return e.toResponse();
        }
        return newFixedLengthResponse(
                Response.Status.OK,
                MIME_PLAINTEXT,
                "");
    }

    /** Get the logger resource associated with the request.
     * @param session the session to get the resource from.
     * @return the logger resource associated with the request.
     * @throws ServerException if the logger was not found.
     *  */
    private LogConfigurator.Resource getResource(
            final IHTTPSession session) throws ServerException {
        String logger = session.getUri();
        if (logger.startsWith("/")) {
            logger = logger.substring(1);
        }
        LogConfigurator logConfigurator = new Log4j2Configurator();
        Optional<LogConfigurator.Resource> resource =
                logConfigurator.findResource(logger);
        if (!resource.isPresent()) {
            throw new ServerException(Response.Status.NOT_FOUND,
                    "Logger not found: " + logger + "'");
        }
        return resource.get();
    }

    /** Configures a logger based on the data in the request.
     * @param session the session to take the parameters from.
     * @throws ServerException if the request is malformed.
     * */
    private void configure(final IHTTPSession session)
            throws ServerException {
        String logger = session.getUri();
        Map<String, String> params = session.getParms();
        String levelString = params.get(PARAM_LEVEL);
        if (levelString == null) {
            throw new ServerException(Response.Status.BAD_REQUEST,
                    "Parameter '" + PARAM_LEVEL + "' is missing");
        }

        LogConfigurator.Resource resource =
                getResource(session);

        try {
                resource.update(levelString);
        } catch (IllegalArgumentException e) {
            throw new ServerException(Response.Status.BAD_REQUEST,
                    e.getMessage());
        }
    }
}
