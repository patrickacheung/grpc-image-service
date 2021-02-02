package io.github.patrickacheung.server;

import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class ImageServerTest {
    private static final Logger log = LogManager.getLogger(ImageServerTest.class);

    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        log.info("hello logger!");
        assertTrue( true );
    }
}
