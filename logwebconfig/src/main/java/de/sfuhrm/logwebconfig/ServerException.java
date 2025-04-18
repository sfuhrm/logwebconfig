package de.sfuhrm.logwebconfig;

import fi.iki.elonen.NanoHTTPD;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/** An exception that can be converted to a server
 * response.
 * */
class ServerException extends Exception {
    /** The HTTP status to return. */
    private final NanoHTTPD.Response.Status status;

    /** The status message to return.*/
    private final String message;

    /** Modifies the response, nullable. */
    private Consumer<NanoHTTPD.Response> responseConsumer;

    /** Constructs a new exception.
     * @param inStatus the HTTP status associated.
     * @param inMessage the HTTP status message associated.
     * */
    ServerException(
            final NanoHTTPD.Response.Status inStatus,
            final String inMessage) {
        this(inStatus, inMessage, null);
    }

    /** Constructs a new exception.
     * @param inStatus the HTTP status associated.
     * @param inMessage the HTTP status message associated.
     * @param inConsumer the consumer for customizing the
     *                   response before returning.
     * */
    ServerException(
            final NanoHTTPD.Response.Status inStatus,
            final String inMessage,
            final Consumer<NanoHTTPD.Response> inConsumer) {
        super(inMessage);
        this.status = Objects.requireNonNull(inStatus);
        message = Objects.requireNonNull(inMessage);
        this.responseConsumer = inConsumer;
    }

    /** Converts the exception to a server response.
     * @return a HTTP response to return for the server stack.
     * */
    public NanoHTTPD.Response toResponse() {
        NanoHTTPD.Response response =
                NanoHTTPD.newFixedLengthResponse(
                        status,
                        NanoHTTPD.MIME_PLAINTEXT,
                        message + "\r\n");

        Optional.ofNullable(responseConsumer)
                .ifPresent(t -> t.accept(response));
        return response;
    }
}
