package de.sfuhrm.logwebconfig;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for the {@link Log4j2Configurator} class.
 * */
public class Log4j2ConfiguratorTest {

    private MockedStatic<LogManager> mockedLogManager;

    private MockedStatic<Configurator> mockedConfigurator;

    private Log4j2Configurator instance;

    @BeforeEach
    public void init() {
        instance = new Log4j2Configurator();
    }

    @BeforeEach
    void setUpStaticMocks() {
        mockedLogManager = Mockito.mockStatic(LogManager.class);
        mockedConfigurator = Mockito.mockStatic(Configurator.class);
    }

    @AfterEach
    void tearDownStaticMocks() {
        mockedConfigurator.closeOnDemand();
        mockedLogManager.closeOnDemand();
    }

    @Test
    public void readLogger() {
        Logger logger = Mockito.mock(Logger.class);
        Mockito.when(logger.getLevel()).thenReturn(Level.ALL);
        mockedLogManager.when(() -> LogManager.getLogger("foo.bar.Baz")).thenReturn(logger);

        String level = instance.findResource("foo.bar.Baz").get().read();
        assertEquals("ALL", level);
    }

    @Test
    public void updateLogger() {
        instance.findResource("foo.bar.Baz").get().update("DEBUG");
        mockedConfigurator.verify(() -> Configurator.setLevel("foo.bar.Baz", Level.DEBUG));
    }

    @Test
    public void readRootLogger() {
        Logger rootLogger = Mockito.mock(Logger.class);
        Mockito.when(rootLogger.getLevel()).thenReturn(Level.ALL);
        mockedLogManager.when(LogManager::getRootLogger).thenReturn(rootLogger);

        String level = instance.findResource("").get().read();
        assertEquals("ALL", level);
    }

    @Test
    public void updateRootLoggerWithWrongLevel() {
        assertThrows(IllegalArgumentException.class, () -> {
            Logger rootLogger = Mockito.mock(Logger.class);
            Mockito.when(rootLogger.getLevel()).thenReturn(Level.ALL);
            mockedLogManager.when(LogManager::getRootLogger).thenReturn(rootLogger);

            instance.findResource("").get().update("FOOBAR");
        });
    }

    @Test
    public void updateRootLogger() {
        instance.findResource("").get().update("DEBUG");
        mockedConfigurator.verify(() -> Configurator.setRootLevel(Level.DEBUG));
    }
}
