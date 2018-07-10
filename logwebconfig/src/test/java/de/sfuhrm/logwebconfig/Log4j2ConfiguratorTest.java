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
import org.mockito.Mockito;
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

import static org.junit.Assert.assertEquals;

/**
 * Test for the {@link Log4j2Configurator} class.
 * */
@RunWith(PowerMockRunner.class)
@PrepareForTest( { Configurator.class, LogManager.class } )
public class Log4j2ConfiguratorTest {

    private Log4j2Configurator instance;

    @Before
    public void init() {
        instance = new Log4j2Configurator();
    }

    @Test
    public void readLogger() {
        PowerMockito.mockStatic(LogManager.class);
        Logger logger = PowerMockito.mock(Logger.class);
        PowerMockito.when(logger.getLevel()).thenReturn(Level.ALL);
        PowerMockito.when(LogManager.getLogger("foo.bar.Baz")).thenReturn(logger);

        String level = instance.findResource("foo.bar.Baz").get().read();
        assertEquals("ALL", level);
    }

    @Test
    public void updateLogger() {
        PowerMockito.mockStatic(Configurator.class);

        instance.findResource("foo.bar.Baz").get().update("DEBUG");

        PowerMockito.verifyStatic(Configurator.class);
        Configurator.setLevel("foo.bar.Baz", Level.DEBUG);
    }

    @Test
    public void readRootLogger() {
        PowerMockito.mockStatic(LogManager.class);
        Logger rootLogger = PowerMockito.mock(Logger.class);
        PowerMockito.when(rootLogger.getLevel()).thenReturn(Level.ALL);
        PowerMockito.when(LogManager.getRootLogger()).thenReturn(rootLogger);

        String level = instance.findResource("").get().read();
        assertEquals("ALL", level);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateRootLoggerWithWrongLevel() {
        PowerMockito.mockStatic(LogManager.class);
        Logger rootLogger = PowerMockito.mock(Logger.class);
        PowerMockito.when(rootLogger.getLevel()).thenReturn(Level.ALL);
        PowerMockito.when(LogManager.getRootLogger()).thenReturn(rootLogger);

        instance.findResource("").get().update("FOOBAR");
    }

    @Test
    public void updateRootLogger() {
        PowerMockito.mockStatic(Configurator.class);

        instance.findResource("").get().update("DEBUG");

        PowerMockito.verifyStatic(Configurator.class);
        Configurator.setRootLevel(Level.DEBUG);
    }
}
