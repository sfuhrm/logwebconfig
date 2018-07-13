package de.sfuhrm.logwebconfig;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Test for the {@link Server} class.
 * */
@RunWith(PowerMockRunner.class)
@PrepareForTest( { Configurator.class, LogManager.class } )
public class ServerTest {

    private Server server;
    private Client jerseyClient;
    private WebTarget serviceTarget;
    private int port = 9999;

    @After
    public void shutdown() {
        server.stop();
        server = null;
        jerseyClient.close();
    }

    @Before
    public void startup() throws IOException {
        server = new Server(null, port);
        jerseyClient = ClientBuilder.newClient();
        serviceTarget = jerseyClient.target("http://localhost:" + port);
    }

    @Test
    public void post() {
        Response r = serviceTarget
                .path("/log4j2//level")
                .request()
                .post(Entity.entity("", MediaType.TEXT_PLAIN_TYPE));
        assertEquals(Response.Status.METHOD_NOT_ALLOWED.getStatusCode(), r.getStatus());
    }

    @Test
    public void getWithLog4j2() {
        PowerMockito.mockStatic(LogManager.class);
        Logger rootLogger = PowerMockito.mock(Logger.class);
        PowerMockito.when(rootLogger.getLevel()).thenReturn(Level.ALL);
        PowerMockito.when(LogManager.getRootLogger()).thenReturn(rootLogger);

        Response r = serviceTarget.path("/log4j2//level").request().get();
        String level = r.readEntity(String.class);
        assertEquals("ALL", level);
    }

    @Test
    public void getWithAuthenticationMissing() {
        server.setAuthentication("user", "password");
        PowerMockito.mockStatic(LogManager.class);
        Logger rootLogger = PowerMockito.mock(Logger.class);
        PowerMockito.when(rootLogger.getLevel()).thenReturn(Level.ALL);
        PowerMockito.when(LogManager.getRootLogger()).thenReturn(rootLogger);

        Response r = serviceTarget.path("/log4j2//level").request().get();
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), r.getStatus());
        assertEquals("Basic realm=\"LogWebConfig\"", r.getHeaderString("WWW-Authenticate"));
    }

    @Test
    public void getWithAuthenticationFailing() {
        server.setAuthentication("user", "password");
        PowerMockito.mockStatic(LogManager.class);

        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic("X", "Y");

        jerseyClient.register(feature);
        serviceTarget = jerseyClient.target("http://localhost:" + port);

        Logger rootLogger = PowerMockito.mock(Logger.class);
        PowerMockito.when(rootLogger.getLevel()).thenReturn(Level.ALL);
        PowerMockito.when(LogManager.getRootLogger()).thenReturn(rootLogger);

        Response r = serviceTarget.path("/log4j2//level").request().get();
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), r.getStatus());
        assertEquals("Basic realm=\"LogWebConfig\"", r.getHeaderString("WWW-Authenticate"));
    }

    @Test
    public void getWithAuthenticationWrongMode() {
        server.setAuthentication("user", "password");
        PowerMockito.mockStatic(LogManager.class);

        HttpAuthenticationFeature feature = HttpAuthenticationFeature.digest("X", "Y");

        jerseyClient.register(feature);
        serviceTarget = jerseyClient.target("http://localhost:" + port);

        Logger rootLogger = PowerMockito.mock(Logger.class);
        PowerMockito.when(rootLogger.getLevel()).thenReturn(Level.ALL);
        PowerMockito.when(LogManager.getRootLogger()).thenReturn(rootLogger);

        Response r = serviceTarget.path("/log4j2//level").request().get();
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), r.getStatus());
        assertEquals("Basic realm=\"LogWebConfig\"", r.getHeaderString("WWW-Authenticate"));
    }

    @Test
    public void getWithAuthenticationOK() {
        server.setAuthentication("user", "password");
        PowerMockito.mockStatic(LogManager.class);

        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic("user", "password");

        jerseyClient.register(feature);
        serviceTarget = jerseyClient.target("http://localhost:" + port);

        Logger rootLogger = PowerMockito.mock(Logger.class);
        PowerMockito.when(rootLogger.getLevel()).thenReturn(Level.ALL);
        PowerMockito.when(LogManager.getRootLogger()).thenReturn(rootLogger);

        Response r = serviceTarget.path("/log4j2//level").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        String level = r.readEntity(String.class);
        assertEquals("ALL", level);
    }

    @Test
    public void getWithMalformedUri() {
        PowerMockito.mockStatic(LogManager.class);
        Logger rootLogger = PowerMockito.mock(Logger.class);
        PowerMockito.when(rootLogger.getLevel()).thenReturn(Level.ALL);
        PowerMockito.when(LogManager.getRootLogger()).thenReturn(rootLogger);

        Response r = serviceTarget.path("/foofoobar//level").request().get();
        assertEquals(Response.Status.BAD_REQUEST, r.getStatusInfo().toEnum());
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
        PowerMockito.mockStatic(Configurator.class);

        PowerMockito.doNothing().when(Configurator.class, "setRootLevel", Level.DEBUG);

        Response r = serviceTarget
                .path("/log4j2//level")
                .request()
                .put(Entity.entity("DEBUG", MediaType.TEXT_PLAIN_TYPE));
        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());

        PowerMockito.verifyStatic(Configurator.class);
        Configurator.setRootLevel(Level.DEBUG);
    }

    @Test
    public void putWithLevelErrorAndLogger() throws Exception {
        String logger = "de.sfuhrm.logwebconfig.LogWebConfig";
        PowerMockito.mockStatic(Configurator.class);

        PowerMockito.doNothing().when(Configurator.class, "setLevel", "", Level.ERROR);

        Response r = serviceTarget
                .path("log4j2").path(logger).path("level")
                .request()
                .put(Entity.entity("ERROR", MediaType.TEXT_PLAIN_TYPE));
        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());

        PowerMockito.verifyStatic(Configurator.class);
        Configurator.setLevel(logger, Level.ERROR);
    }
}
