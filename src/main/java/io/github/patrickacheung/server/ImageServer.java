package io.github.patrickacheung.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Grpc server for image service.
 */
public class ImageServer {
    private static final Logger log = LogManager.getLogger(ImageServer.class);

    public static void main( String[] args ) {
        ImageServer server = new ImageServer();
    }
}
