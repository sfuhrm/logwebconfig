package de.sfuhrm.logwebconfig;

import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** HTTP server for handling RESTy requests and passing them on to
 * log4j1 or log4j2.
 * */
final class Server extends NanoHTTPD {

    /** Optional username to authenticate with.  */
    private String username;

    /** Optional password to authenticate with.  */
    private String password;

    /** Constructs a new server and starts it.
     * @param hostname the host name to listen to, or {@code null}
     *                 for listen to all addresses.
     * @param port the TCP/IP port to listen on.
     * @throws IOException when not able to binding to the port.
     * */
    Server(final String hostname,
           final int port) throws IOException {
        this(hostname, port, true);
    }

    /** Constructs a new server and starts it.
     * @param hostname the host name to listen to, or {@code null}
     *                 for listen to all addresses.
     * @param port the TCP/IP port to listen on.
     * @param start whether to start the server thread.
     * @throws IOException when not able to binding to the port.
     * */
    Server(final String hostname,
           final int port,
           final boolean start) throws IOException {
        super(hostname, port);
        if (start) {
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, true);
        }
    }

    /** Configures the authentication information to authenticate the
     * clients with.
     * @param inUsername the name of the user expected.
     * @param inPassword the password expected.
     * */
    void setAuthentication(final String inUsername,
                           final String inPassword) {
        this.username = inUsername;
        this.password = inPassword;
    }

    /** Checks the authentication information.
     * If no authentication is configured, this call just returns.
     * Otherwise, it checks authentication ant throws a ServerException if
     * authentication fails.
     * @param session the session the check authentication for.
     * @throws ServerException if authentication fails.
     *  */
    private void checkAuthentication(final IHTTPSession session)
            throws ServerException {
        if (username != null
                && password != null) {
            String auth = session.getHeaders().get("authorization");
            if (auth == null) {
                throw unauth();
            }

            int spaceIndex = auth.indexOf(' ');
            if (spaceIndex == -1) {
                throw unauth();
            }

            String first = auth.substring(0, spaceIndex);
            if (!"Basic".equals(first)) {
                throw unauth();
            }

            String base64 = auth.substring(spaceIndex + 1);

            byte[] clientAuthBytes = Base64.getDecoder().decode(base64);
            String clientAuth = new String(
                    clientAuthBytes,
                    Charset.forName("ISO-8859-1"));
            String serverAuth = username + ":" + password;
            if (!serverAuth.equals(clientAuth)) {
                throw unauth();
            }
        }
    }

    /** Creates a new unauthorized server exception.
     * This is a convenience method for
     * {@link #checkAuthentication(IHTTPSession)}.
     * @return the generated exception that also sets the
     * {@code WWW-Authenticate} HTTP header.
     * */
    private ServerException unauth() {
      return new ServerException(Response.Status.UNAUTHORIZED,
              "",
              r -> r.addHeader(
                      "WWW-Authenticate",
                      "Basic realm=\"LogWebConfig\""));
      };

    @Override
    public Response serve(final IHTTPSession session) {
        try {
            checkAuthentication(session);
            Method method = session.getMethod();
            LogFrameworkBridge.LoggerResource resource = getResource(session);
            switch (method) {
                case GET:
                    String level = resource.get();
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
    private LogFrameworkBridge.LoggerResource getResource(
            final IHTTPSession session) throws ServerException {
        Pattern pattern = Pattern.compile("/*(log4j1|log4j2)/([^/]*)/level");
        Matcher matcher = pattern.matcher(session.getUri());
        if (!matcher.matches()) {
            throw new ServerException(
                    Response.Status.BAD_REQUEST,
                    "URI illegal: " + session.getUri());
        }

        String logFramework = matcher.group(1);
        String logger = matcher.group(2);

        LogFrameworkBridge logFrameworkHandler =
                getLogConfigurator(logFramework);
        Optional<LogFrameworkBridge.LoggerResource> resource =
                logFrameworkHandler.findLoggerResource(logger);
        if (!resource.isPresent()) {
            throw new ServerException(Response.Status.NOT_FOUND,
                    "Logger not found: " + logger + "'");
        }
        return resource.get();
    }

    /**
     * Get the log configurator for the named logging framework.
     * @param logFramework the log framework name to get.
     *                     Possible frameworks are at the
     *                     moment: {@code "log4j1"}, {@code "log4j2"}.
     * @return the logging framework specific configurator instance.
     * @throws ServerException if the logging framework was not found.
     * */
    LogFrameworkBridge getLogConfigurator(
            final String logFramework) throws ServerException {
        LogFrameworkBridge logFrameworkHandler;
        switch (logFramework) {
            case "log4j1":
                logFrameworkHandler = new Log4J1Bridge();
                break;
            case "log4j2":
                logFrameworkHandler = new Log4J2Bridge();
                break;
            default:
                throw new ServerException(
                        Response.Status.BAD_REQUEST,
                        "Unknown framework " + logFramework);
        }
        return logFrameworkHandler;
    }

    /** Configures a logger based on the data in the request.
     * @param session the session to take the parameters from.
     * @param resource the log resource to manipulate.
     * @throws ServerException if the request is malformed.
     * */
    private void configure(final IHTTPSession session,
                           final LogFrameworkBridge.LoggerResource resource)
            throws ServerException {
        String lengthString = session.getHeaders().get("content-length");
        if (lengthString == null) {
            throw new ServerException(Response.Status.BAD_REQUEST,
                    "Content-Length header is missing");
        }
        int length = Integer.parseInt(lengthString);

        byte[] data = new byte[length];
        try {
            int readLength = session.getInputStream().read(data);
            if (readLength != length) {
                throw new ServerException(
                        Response.Status.INTERNAL_ERROR,
                        "Short read");
            }
        } catch (IOException e) {
            throw new ServerException(
                    Response.Status.INTERNAL_ERROR,
                    e.getMessage());
        }

        String levelString = new String(
                data,
                Charset.forName("ASCII"));
        try {
                resource.set(levelString);
        } catch (IllegalArgumentException e) {
            throw new ServerException(Response.Status.BAD_REQUEST,
                    e.getMessage());
        }
    }
}
