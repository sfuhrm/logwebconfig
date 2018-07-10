package de.sfuhrm.logwebconfig;

import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

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
    public void startWithPortBlocked() throws IOException {
        assertNull(singleton());
        System.setProperty("LOGWEBCONFIG_PORT", "9999");
        ServerSocket socket = new ServerSocket(9999);
        LogWebConfig.start();
        assertNull(singleton());
        socket.close();
    }

    @Test
    public void startWithDisabled() throws IOException {
        assertNull(singleton());
        System.setProperty("LOGWEBCONFIG_ENABLE", "false");
        LogWebConfig.start();
        assertNull(singleton());
    }

    @Test
    public void startWithUserPassword() throws IOException {
        assertNull(singleton());
        System.setProperty("LOGWEBCONFIG_USER", "u");
        System.setProperty("LOGWEBCONFIG_PASSWORD", "p");
        LogWebConfig.start();
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
