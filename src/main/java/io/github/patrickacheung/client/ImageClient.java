package io.github.patrickacheung.client;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.patrickacheung.GreeterServiceGrpc;
import io.github.patrickacheung.HelloReply;
import io.github.patrickacheung.HelloRequest;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

/**
 * Grpc client for image service.
 * Based on grpc-java HelloWorldClient.java
 */
public class ImageClient {
    private static final Logger log = LogManager.getLogger(ImageClient.class.getName());

    private final GreeterServiceGrpc.GreeterServiceBlockingStub blockingStub;

    /** Construct client for accessing ImageServer server using the existing channel. */
    public ImageClient(Channel channel) {
    // 'channel' here is a Channel, not a ManagedChannel, so it is not this code's responsibility to
    // shut it down.

    // Passing Channels to code makes code easier to test and makes it easier to reuse Channels.
        this.blockingStub = GreeterServiceGrpc.newBlockingStub(channel);
    }

    private void greet(String user) {
        log.info("Will try to greet " + user + " ...");
        HelloRequest req = HelloRequest.newBuilder().setName(user).build();
        HelloReply response;
        try {
            response = blockingStub.sayHello(req);
        } catch (StatusRuntimeException e) {
            log.warn("RPC failed: {0}", e.getStatus());
            return;
        }

        log.info("Greeting: " + response.getMessage());
    }

    public static void main(String[] args) throws InterruptedException {
        String user = "user";
        String target = "localhost:8080";

        Options options = new Options();
        Option imageFile = new Option("i", "image", true, "image file path");
        Option rotate = new Option("r", "rotation", true, "0, 90, 180, 270");
        options.addOption(imageFile);
        options.addOption(rotate);

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);
            if (!line.hasOption("i") && !line.hasOption("r")) {
                throw new MissingOptionException(Arrays.asList(imageFile, rotate));
            }

            // Create a communication channel to the server, known as a Channel. Channels are thread-safe
            // and reusable. It is common to create channels at the beginning of your application and reuse
            // them until the application shuts down.
            ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
            // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
            // needing certificates.
            .usePlaintext()
            .build();
            
            try {
                ImageClient imageClient = new ImageClient(channel);
                imageClient.greet(user);    
            } finally {
                // ManagedChannels use resources like threads and TCP connections. To prevent leaking these
                // resources the channel should be shut down when it will no longer be used. If it may be used
                // again leave it running.
                channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
            }
        } catch (ParseException e) {
            log.error("Failed to parse command line args. Reason: " + e.getMessage());
        }
    }
}
