package io.github.patrickacheung.client;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.patrickacheung.NLImage;
import io.github.patrickacheung.NLImageRotateRequest;
import io.github.patrickacheung.NLImageServiceGrpc;
import io.github.patrickacheung.client.ImageClientUtils.Image;
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

    private final NLImageServiceGrpc.NLImageServiceBlockingStub blockingStub;

    /** Construct client for accessing ImageServer server using the existing channel. */
    public ImageClient(Channel channel) {
        // 'channel' here is a Channel, not a ManagedChannel, so it is not this code's responsibility to
        // shut it down.

        // Passing Channels to code makes code easier to test and makes it easier to reuse Channels.
        this.blockingStub = NLImageServiceGrpc.newBlockingStub(channel);
    }

    public NLImage rotateImage(NLImageRotateRequest request) {
        NLImage reply;
        try {
            reply = blockingStub.rotateImage(request);
        } catch (StatusRuntimeException e) {
            log.error("RPC failed: {0}", e.getStatus());
            reply = null;
        }
        return reply;
    }

    public static void main(String[] args) throws InterruptedException {
        String defaultHost = "localhost";
        int defaultPort = 8080;

        Options options = new Options();
        Option imageFile = new Option("i", "image", true, "image file path");
        Option rotate = new Option("r", "rotation", true, "0, 90, 180, 270");
        Option host = new Option("h", "host", true, "the host");
        Option port = new Option("p", "port", true, "the port");
        options.addOption(imageFile);
        options.addOption(rotate);
        options.addOption(host);
        options.addOption(port);

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);
            if (!line.hasOption("i") && !line.hasOption("r")) {
                throw new MissingOptionException(Arrays.asList(imageFile, rotate));
            } else if (!line.hasOption("i")) {
                throw new MissingOptionException(Arrays.asList(imageFile));
            } else if (!line.hasOption("r")) {
                throw new MissingOptionException(Arrays.asList(rotate));
            }

            if (line.hasOption("h")) {
                defaultHost = line.getOptionValue("h");
            }
            if (line.hasOption("p")) {
                defaultPort = Integer.parseInt(line.getOptionValue("p"));
            }

            Path imageFilePath = Paths.get(line.getOptionValue("i"));
            Image image = ImageClientUtils.parseImage(imageFilePath);
            NLImageRotateRequest.Rotation rotation = ImageClientUtils.getRotation(line.getOptionValue("r"));
            NLImageRotateRequest rotateRequestProto = ImageClientUtils.generateRequestProto(image, rotation);
            
            // Create a communication channel to the server, known as a Channel. Channels are thread-safe
            // and reusable. It is common to create channels at the beginning of your application and reuse
            // them until the application shuts down.
            ManagedChannel channel = ManagedChannelBuilder.forAddress(defaultHost, defaultPort)
            // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
            // needing certificates.
            .usePlaintext()
            .build();
            
            try {
                ImageClient imageClient = new ImageClient(channel); // TODO: make an IMAGECLIENTBUILDER
                NLImage replyProto = imageClient.rotateImage(rotateRequestProto);
            } finally {
                // ManagedChannels use resources like threads and TCP connections. To prevent leaking these
                // resources the channel should be shut down when it will no longer be used. If it may be used
                // again leave it running.
                channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
            }
        } catch (ParseException e) {
            log.error("Failed to parse command line args. Reason: " + e.getMessage());
        } catch (NumberFormatException e) {
            log.error(options.getOption("p").getLongOpt() + " arg provided is not an integer");
        } catch (IOException e) {
            log.error("Failed to read image file. Reason: " + e);
        } catch (IllegalArgumentException e) {
            log.error("Invalid argument. Reason: " + e.getMessage());
        }
    }
}
