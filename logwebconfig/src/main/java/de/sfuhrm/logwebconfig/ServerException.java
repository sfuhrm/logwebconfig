package de.sfuhrm.logwebconfig;

import fi.iki.elonen.NanoHTTPD;

import java.util.Objects;

/** An exception that can be converted to a server
 * response.
 * */
class ServerException extends Exception {
    /** The HTTP status to return. */
    private NanoHTTPD.Response.Status status;

    /** The status message to return.*/
    private String message;

    /** Constructs a new exception.
     * @param inStatus the HTTP status associated.
     * @param inMmessage the HTTP status message associated.
     * */
    ServerException(
            final NanoHTTPD.Response.Status inStatus,
            final String inMmessage) {
        super(inMmessage);
        this.status = Objects.requireNonNull(inStatus);
        Objects.requireNonNull(inMmessage);
    }

    /** Converts the exception to a server response.
     * @return a HTTP response to return for the server stack.
     * */
    public NanoHTTPD.Response toResponse() {
        return
                NanoHTTPD.newFixedLengthResponse(
                        status,
                        NanoHTTPD.MIME_PLAINTEXT,
                        message + "\r\n");
    }
}
