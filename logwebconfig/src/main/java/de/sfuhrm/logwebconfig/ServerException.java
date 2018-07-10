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
    private NanoHTTPD.Response.Status status;

    /** The status message to return.*/
    private String message;

    /** Modifies the response. */
    private Optional<Consumer<NanoHTTPD.Response>> responseConsumer;

    /** Constructs a new exception.
     * @param inStatus the HTTP status associated.
     * @param inMessage the HTTP status message associated.
     * */
    ServerException(
            final NanoHTTPD.Response.Status inStatus,
            final String inMessage) {
        this(inStatus, inMessage, Optional.empty());
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
            final Optional<Consumer<NanoHTTPD.Response>> inConsumer) {
        super(inMessage);
        this.status = Objects.requireNonNull(inStatus);
        Objects.requireNonNull(inMessage);
        this.responseConsumer = Objects.requireNonNull(inConsumer);
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

        responseConsumer.ifPresent(t -> t.accept(response));

        return response;
    }
}
