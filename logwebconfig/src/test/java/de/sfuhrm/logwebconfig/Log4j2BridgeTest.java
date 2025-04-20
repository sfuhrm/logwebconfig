package de.sfuhrm.logwebconfig;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for the {@link Log4J2Bridge} class.
 * */
public class Log4j2BridgeTest {

    private MockedStatic<LogManager> mockedLogManager;

    private MockedStatic<Configurator> mockedConfigurator;

    private Log4J2Bridge instance;

    @BeforeEach
    public void init() {
        instance = new Log4J2Bridge();
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
    public void testGetLevelWithNamedLogger() {
        Logger logger = Mockito.mock(Logger.class);
        Mockito.when(logger.getLevel()).thenReturn(Level.ALL);
        mockedLogManager.when(() -> LogManager.getLogger("foo.bar.Baz")).thenReturn(logger);

        String level = instance.findLoggerResource("foo.bar.Baz").get().getLevel();
        assertEquals("ALL", level);
    }

    @Test
    public void testSetLevelWithNamedLogger() {
        instance.findLoggerResource("foo.bar.Baz").get().setLevel("DEBUG");
        mockedConfigurator.verify(() -> Configurator.setLevel("foo.bar.Baz", Level.DEBUG));
    }

    @Test
    public void testGetLevenWithRootLogger() {
        Logger rootLogger = Mockito.mock(Logger.class);
        Mockito.when(rootLogger.getLevel()).thenReturn(Level.ALL);
        mockedLogManager.when(LogManager::getRootLogger).thenReturn(rootLogger);

        String level = instance.findLoggerResource("").get().getLevel();
        assertEquals("ALL", level);
    }

    @Test
    public void testSetLevelWithWrongLevelName() {
        assertThrows(IllegalArgumentException.class, () -> {
            Logger rootLogger = Mockito.mock(Logger.class);
            Mockito.when(rootLogger.getLevel()).thenReturn(Level.ALL);
            mockedLogManager.when(LogManager::getRootLogger).thenReturn(rootLogger);

            instance.findLoggerResource("").get().setLevel("FOOBAR");
        });
    }

    @Test
    public void setLevelWithRootLogger() {
        instance.findLoggerResource("").get().setLevel("DEBUG");
        mockedConfigurator.verify(() -> Configurator.setRootLevel(Level.DEBUG));
    }
}
