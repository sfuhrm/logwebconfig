package de.sfuhrm.logwebconfig;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;

/**
 * Test for the {@link Log4j2Configurator} class.
 * */
@RunWith(PowerMockRunner.class)
@PrepareForTest( { LogManager.class, Logger.class } )
public class Log4j1ConfiguratorTest {

    private Log4j1Configurator instance;

    @Before
    public void init() {
        instance = new Log4j1Configurator();
    }

    @Test
    public void readLogger() {
        Logger logger = PowerMockito.mock(Logger.class);
        PowerMockito.when(logger.getLevel()).thenReturn(Level.ALL);
        PowerMockito.mockStatic(LogManager.class);
        PowerMockito.when(LogManager.getLogger("foo.bar.Baz")).thenReturn(logger);

        String level = instance.findResource("foo.bar.Baz").get().read();
        assertEquals("ALL", level);
    }

    @Test
    public void updateLogger() {
        Logger logger = Mockito.mock(Logger.class);
        PowerMockito.mockStatic(LogManager.class);
        PowerMockito.when(LogManager.getLogger("foo.bar.Baz")).thenReturn(logger);

        instance.findResource("foo.bar.Baz").get().update("DEBUG");

        Mockito.verify(logger).setLevel(Level.DEBUG);
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
        PowerMockito.mockStatic(LogManager.class);
        Logger logger = PowerMockito.mock(Logger.class);
        PowerMockito.when(logger.getLevel()).thenReturn(Level.ALL);
        PowerMockito.when(LogManager.getRootLogger()).thenReturn(logger);

        instance.findResource("").get().update("DEBUG");

        Mockito.verify(logger).setLevel(Level.DEBUG);
    }
}
