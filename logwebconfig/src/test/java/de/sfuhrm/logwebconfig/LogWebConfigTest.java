package de.sfuhrm.logwebconfig;

import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.*;

/**
 * Test for the {@link LogWebConfig} class.
 * */
public class LogWebConfigTest {

    private Server singleton() {
        return Whitebox.getInternalState(LogWebConfig.class, "singleton");
    }

    @Before
    public void shutdown() {
        LogWebConfig.stop();
    }

    @Test
    public void startOnce() {
        assertNull(singleton());
        LogWebConfig.start();
        assertNotNull(singleton());
    }

    @Test
    public void startTwice() {
        assertNull(singleton());
        LogWebConfig.start();
        Object first = singleton();

        LogWebConfig.start();
        Object second = singleton();

        assertSame(first, second);
    }
}
