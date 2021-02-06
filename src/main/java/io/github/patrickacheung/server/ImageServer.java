package io.github.patrickacheung.server;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.patrickacheung.service.GreeterServiceImpl;
import io.github.patrickacheung.service.ImageServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

/**
 * Grpc server for image service.
 * Based on grpc-java HelloWorldServer.java
 */
public class ImageServer {
    private static final Logger log = LogManager.getLogger(ImageServer.class.getName());

    private Server server;

    private void start() throws IOException {
        int port = 8080;
        server = ServerBuilder.forPort(port).addService(new GreeterServiceImpl()).addService(new ImageServiceImpl()).build().start(); // TODO investigate .intercept() - can intercept calls (perhaps logging?) //ServerTransportFilter vs ServerInterceptor
        log.info ("Server started, listening on " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Server shutting down...");
            try {
                shutdown();
            } catch (InterruptedException e) {
                log.error("Failed to shutdown server...");
            }
            LogManager.shutdown();
        }));
    }

    private void shutdown() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final ImageServer server = new ImageServer();
        server.start();
        server.blockUntilShutdown();
    }
}
