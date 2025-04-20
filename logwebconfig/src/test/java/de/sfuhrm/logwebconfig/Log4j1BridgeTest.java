package de.sfuhrm.logwebconfig;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for the {@link Log4J1Bridge} class.
 * */
public class Log4j1BridgeTest {

    private MockedStatic<LogManager> mockedLogManager;

    private Log4J1Bridge instance;

    @BeforeEach
    public void init() {
        instance = new Log4J1Bridge();
    }

    @BeforeEach
    void setUpStaticMocks() {
        mockedLogManager = Mockito.mockStatic(LogManager.class);
    }

    @AfterEach
    void tearDownStaticMocks() {
        mockedLogManager.closeOnDemand();
    }

    @Test
    public void readLogger() {
        Logger logger = Mockito.mock(Logger.class);
        Mockito.when(logger.getLevel()).thenReturn(Level.ALL);
        mockedLogManager.when(() -> LogManager.getLogger("foo.bar.Baz")).thenReturn(logger);

        String level = instance.findLoggerResource("foo.bar.Baz").get().get();
        assertEquals("ALL", level);
    }

    @Test
    public void updateLogger() {
        Logger logger = Mockito.mock(Logger.class);
        mockedLogManager.when(() -> LogManager.getLogger("foo.bar.Baz")).thenReturn(logger);

        instance.findLoggerResource("foo.bar.Baz").get().set("DEBUG");

        Mockito.verify(logger).setLevel(Level.DEBUG);
    }

    @Test
    public void readRootLogger() {
        Logger rootLogger = Mockito.mock(Logger.class);
        Mockito.when(rootLogger.getLevel()).thenReturn(Level.ALL);
        mockedLogManager.when(LogManager::getRootLogger).thenReturn(rootLogger);

        String level = instance.findLoggerResource("").get().get();
        assertEquals("ALL", level);
    }

    @Test
    public void updateRootLoggerWithWrongLevel() {
        assertThrows(IllegalArgumentException.class, () -> {
            Logger rootLogger = Mockito.mock(Logger.class);
            Mockito.when(rootLogger.getLevel()).thenReturn(Level.ALL);
            mockedLogManager.when(LogManager::getRootLogger).thenReturn(rootLogger);

            instance.findLoggerResource("").get().set("FOOBAR");
        });
    }

    @Test
    public void updateRootLogger() {
        Logger logger = Mockito.mock(Logger.class);
        Mockito.when(logger.getLevel()).thenReturn(Level.ALL);
        mockedLogManager.when(LogManager::getRootLogger).thenReturn(logger);

        instance.findLoggerResource("").get().set("DEBUG");

        Mockito.verify(logger).setLevel(Level.DEBUG);
    }
}
