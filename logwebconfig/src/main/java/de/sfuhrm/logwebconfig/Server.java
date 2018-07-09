package de.sfuhrm.logwebconfig;

import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            LogConfigurator.Resource resource = getResource(session);
            switch (method) {
                case GET:
                    String level = resource.read();
                    return newFixedLengthResponse(
                            Response.Status.OK,
                            MIME_PLAINTEXT,
                            level);
                case PUT:
                    configure(session, resource);
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
        Pattern pattern = Pattern.compile("/*(log4j2)/([^/]*)/level");
        Matcher matcher = pattern.matcher(session.getUri());
        if (!matcher.matches()) {
            throw new ServerException(
                    Response.Status.BAD_REQUEST,
                    "URI illegal: " + session.getUri());
        }

        String logFramework = matcher.group(1);
        String logger = matcher.group(2);

        LogConfigurator logConfigurator;
        switch (logFramework) {
            case "log4j2":
                logConfigurator = new Log4j2Configurator();
                break;
            default:
                throw new ServerException(
                        Response.Status.BAD_REQUEST,
                        "Unknown framework " + logFramework);
        }
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
     * @param resource the log resource to manipulate.
     * @throws ServerException if the request is malformed.
     * */
    private void configure(final IHTTPSession session,
                           final LogConfigurator.Resource resource)
            throws ServerException {
        Map<String, String> params = session.getParms();
        String levelString = params.get(PARAM_LEVEL);
        if (levelString == null) {
            throw new ServerException(Response.Status.BAD_REQUEST,
                    "Parameter '" + PARAM_LEVEL + "' is missing");
        }

        try {
                resource.update(levelString);
        } catch (IllegalArgumentException e) {
            throw new ServerException(Response.Status.BAD_REQUEST,
                    e.getMessage());
        }
    }
}
