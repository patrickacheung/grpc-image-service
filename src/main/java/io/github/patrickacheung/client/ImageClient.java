package io.github.patrickacheung.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Grpc client for image service.
 */
public class ImageClient {
    private static final Logger log = LogManager.getLogger(ImageClient.class);
    
    public static void main( String[] args ) {
        ImageClient imageClient = new ImageClient();
    }
}
