package de.sfuhrm.logwebconfig;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.*;
import org.mockito.Mock;
import org.mockito.Mockito;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for the {@link Server} class.
 * */
@ExtendWith(MockitoExtension.class)
public class ServerTest {

    private Server server;
    private Client jerseyClient;
    private WebTarget serviceTarget;
    private int port = 9999;

    @Spy
    private Log4J2Bridge configuratorMock;

    @Mock
    private Logger loggerMock;

    @Mock
    private Logger rootLoggerMock;

    @AfterEach
    public void shutdown() {
        server.stop();
        server = null;
        if (jerseyClient != null) {
            jerseyClient.close();
        }
    }

    @BeforeEach
    public void startup() throws IOException, ServerException {
        Server realServer = new Server(null, port, false);
        server = Mockito.spy(realServer);
        server.start(1000, true);
        jerseyClient = ClientBuilder.newClient();
        serviceTarget = jerseyClient.target("http://localhost:" + port);
    }

    private void installMocks() throws ServerException {
        doReturn(configuratorMock).when(server).getLogFrameworkBridge(Mockito.anyString());
    }

    @Test
    public void post() throws ServerException {
        installMocks();
        Response r = serviceTarget
                .path("/log4j2//level")
                .request()
                .post(Entity.entity("", MediaType.TEXT_PLAIN_TYPE));
        assertEquals(Response.Status.METHOD_NOT_ALLOWED.getStatusCode(), r.getStatus());
    }

    @Test
    public void getWithLog4j2() throws ServerException {
        installMocks();
        Mockito.when(configuratorMock.getRootLogger()).thenReturn(rootLoggerMock);
        Mockito.when(rootLoggerMock.getLevel()).thenReturn(Level.ALL);

        Response r = serviceTarget.path("/log4j2//level").request().get();
        String level = r.readEntity(String.class);

        verify(rootLoggerMock, times(1)).getLevel();
        assertEquals("ALL", level);
    }

    @Test
    public void getWithAuthenticationMissing() {
        server.setAuthentication("user", "password");
        Logger rootLogger = Mockito.mock(Logger.class);

        Response r = serviceTarget.path("/log4j2//level").request().get();
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), r.getStatus());
        assertEquals("Basic realm=\"LogWebConfig\"", r.getHeaderString("WWW-Authenticate"));
    }

    @Test
    public void getWithAuthenticationFailing() {
        server.setAuthentication("user", "password");

        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic("X", "Y");

        jerseyClient.register(feature);
        serviceTarget = jerseyClient.target("http://localhost:" + port);

        Response r = serviceTarget.path("/log4j2//level").request().get();
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), r.getStatus());
        assertEquals("Basic realm=\"LogWebConfig\"", r.getHeaderString("WWW-Authenticate"));
    }

    @Test
    public void getWithAuthenticationWrongMode() {
        server.setAuthentication("user", "password");

        HttpAuthenticationFeature feature = HttpAuthenticationFeature.digest("X", "Y");

        jerseyClient.register(feature);
        serviceTarget = jerseyClient.target("http://localhost:" + port);

        Logger rootLogger = Mockito.mock(Logger.class);

        Response r = serviceTarget.path("/log4j2//level").request().get();
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), r.getStatus());
        assertEquals("Basic realm=\"LogWebConfig\"", r.getHeaderString("WWW-Authenticate"));
    }

    @Test
    public void getWithAuthenticationOK() throws ServerException {
        installMocks();
        server.setAuthentication("user", "password");

        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic("user", "password");

        jerseyClient.register(feature);
        serviceTarget = jerseyClient.target("http://localhost:" + port);

        doReturn(rootLoggerMock).when(configuratorMock).getRootLogger();
        Mockito.when(rootLoggerMock.getLevel()).thenReturn(Level.ALL);

        Response r = serviceTarget.path("/log4j2//level").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        String level = r.readEntity(String.class);
        assertEquals("ALL", level);
    }

    @Test
    public void getWithMalformedUri() {
        Response r = serviceTarget.path("/foofoobar//level").request().get();
        assertEquals(Response.Status.BAD_REQUEST, r.getStatusInfo().toEnum());
    }

    @Test
    void getLogFrameworkBridgeWithLog4j1() throws IOException, ServerException {
        Server server = new Server(null, 1234, false);
        LogFrameworkBridge bridge = server.getLogFrameworkBridge("log4j1");
        assertNotNull(bridge);
        assertEquals(Log4J1Bridge.class, bridge.getClass());
    }

    @Test
    void getLogFrameworkBridgeWithLog4j2() throws IOException, ServerException {
        Server server = new Server(null, 1234, false);
        LogFrameworkBridge bridge = server.getLogFrameworkBridge("log4j2");
        assertNotNull(bridge);
        assertEquals(Log4J2Bridge.class, bridge.getClass());
    }

    @Test
    void getLogFrameworkBridgeWithUnknown() throws IOException {
        Server server = new Server(null, 1234);
        assertThrows(ServerException.class, () ->
            server.getLogFrameworkBridge("frameworkunknown"));
    }

    @Test
    public void putWithoutLevel() {
        Response r = serviceTarget
                .path("/log4j2//level")
                .request()
                .put(Entity.entity("", MediaType.TEXT_PLAIN_TYPE));
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), r.getStatus());
    }

    @Test
    public void putWithIllegalLevel() {
        Response r = serviceTarget.path("/log4j2//level")
                .request()
                .put(Entity.entity("Schnitlauch", MediaType.TEXT_PLAIN_TYPE));
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), r.getStatus());
    }

    @Test
    public void putWithLevelDebugAndLoggerRoot() throws Exception {
        installMocks();
        Response r = serviceTarget
                .path("/log4j2//level")
                .request()
                .put(Entity.entity("DEBUG", MediaType.TEXT_PLAIN_TYPE));
        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());

        Mockito.verify(configuratorMock).setRootLevel(Level.DEBUG);
    }

    @Test
    public void putWithLevelErrorAndLogger() throws Exception {
        String logger = "de.sfuhrm.logwebconfig.LogWebConfig";

        installMocks();
        Mockito.doNothing().when(configuratorMock).setLevel(Mockito.anyString(), Mockito.any());
        Mockito.when(configuratorMock.findLoggerResource(Mockito.anyString())).thenCallRealMethod();

        Response r = serviceTarget
                .path("log4j2").path(logger).path("level")
                .request()
                .put(Entity.entity("ERROR", MediaType.TEXT_PLAIN_TYPE));
        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());

        Mockito.verify(configuratorMock).setLevel(logger, Level.ERROR);
    }
}
