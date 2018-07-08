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
                    return configure(session);
                default:
                    return methodNotAllowed(method);
            }
        } catch (IOException | ResponseException e) {
            return newFixedLengthResponse(
                    Response.Status.INTERNAL_ERROR,
                    NanoHTTPD.MIME_PLAINTEXT,
                    e.getMessage());
        }
    }

    /** Configures a logger based on the data in the request.
     * @param session the session to take the parameters from.
     * @return the response to return to the HTTP client.
     * @throws IOException if reading the files fails.
     * @throws ResponseException if reading the files fails.
     * */
    private Response configure(final IHTTPSession session)
            throws IOException, ResponseException {
        String logger = session.getUri();
        Map<String, String> params = session.getParms();
        String levelString = params.get(PARAM_LEVEL);
        if (levelString == null) {
            return badRequest("Parameter '" + PARAM_LEVEL + "' is missing\r\n");
        }

        if (logger.startsWith("/")) {
            logger = logger.substring(1);
        }

        LogConfigurator logConfigurator = new Log4j2Configurator();
        Optional<LogConfigurator.Resource> resource =
                logConfigurator.findResource(logger);
        if (!resource.isPresent()) {
            return newFixedLengthResponse(
                    Response.Status.NOT_FOUND,
                    MIME_PLAINTEXT,
                    "Not found: " + logger + "\r\n");
        }

        try {
            if (resource.isPresent()) {
                resource.get().update(levelString);
            }
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }
        return newFixedLengthResponse(
                Response.Status.OK,
                MIME_PLAINTEXT,
                "OK\r\n");
    }

    /** Returns a bad request HTTP response.
     * @param message the plain text message to pass along with the response.
     * @return the created response object for this error.
     * */
    private Response badRequest(final String message) {
        return newFixedLengthResponse(
                Response.Status.BAD_REQUEST,
                MIME_PLAINTEXT,
                message);
    }

    /** Returns a method not allowed HTTP response.
     * @param method the method that was used in the request.
     * @return the created response object for this error.
     * */
    private Response methodNotAllowed(final Method method) {
        return newFixedLengthResponse(
                Response.Status.METHOD_NOT_ALLOWED,
                MIME_PLAINTEXT,
                "Method " + method + " is not allowed\r\n");
    }
}
