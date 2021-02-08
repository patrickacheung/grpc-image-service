package io.github.patrickacheung.client;

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.ByteString;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import io.github.patrickacheung.NLImage;
import io.github.patrickacheung.NLImageRotateRequest;
import io.github.patrickacheung.NLImageRotateRequest.Rotation;

public class ImageClientUtils {
    private static final ImmutableMap<Integer, NLImageRotateRequest.Rotation> validRotationsToEnum = ImmutableMap.of(
        0, Rotation.NONE, 
        90, Rotation.NINETY_DEG, 
        180, Rotation.ONE_EIGHTY_DEG, 
        270, Rotation.TWO_SEVENTY_DEG);

    private static int validateCliRotationOptions(String rotation) throws IllegalArgumentException {
        try {
            int rotationNum = Integer.parseInt(rotation);
            if (!validRotationsToEnum.containsKey(rotationNum)) {
                throw new IllegalArgumentException("Invalid rotation request!");
            }
            return rotationNum;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid rotation request! " + e.getMessage());
        }
    }

    public static NLImageRotateRequest.Rotation getRotation(String rotation) throws IllegalArgumentException {
        int rotationNum = validateCliRotationOptions(rotation);
        return validRotationsToEnum.get(rotationNum);
    }

    public static NLImageRotateRequest generateRequestProto(Image image, 
            NLImageRotateRequest.Rotation rotation) throws IOException {

        NLImage.Builder imageBuilder = NLImage.newBuilder();
        imageBuilder.setColor(image.getBufferedImage().getColorModel().getColorSpace().getType() == ColorSpace.TYPE_GRAY);
        imageBuilder.setWidth(image.getBufferedImage().getWidth());
        imageBuilder.setHeight(image.getBufferedImage().getHeight());
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ImageIO.write(image.getBufferedImage(), image.getFormat(), bos);
            imageBuilder.setData(ByteString.copyFrom(bos.toByteArray()));
        }
        NLImage imageProto = imageBuilder.build();

        NLImageRotateRequest.Builder requestBuilder = NLImageRotateRequest.newBuilder();
        requestBuilder.setRotation(rotation);
        requestBuilder.setImage(imageProto);
        
        return requestBuilder.build();
    }

    public static Image parseImage(Path imageFilePath) throws IOException {
        String format = null;
        BufferedImage bufferedImage = null;

        try (InputStream is = Files.newInputStream(imageFilePath);
             ImageInputStream iis = ImageIO.createImageInputStream(is)) {

            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                format = reader.getFormatName();
                reader.setInput(iis);
                bufferedImage = reader.read(0);
            } else {
                throw new IOException("Unable to parse image! Invalid format!");
            }
        }

        return new Image(bufferedImage, format);
    }

    private ImageClientUtils() {
        // private constructor
    }

    /** Wrapper class to hold image format */
    static class Image {
        private String format;
        private BufferedImage bufferedImage;

        private Image(BufferedImage bufferedImage, String format) {
            this.bufferedImage = bufferedImage;
            this.format = format;
        }

        BufferedImage getBufferedImage() {
            return bufferedImage;
        }

        String getFormat() {
            return format;
        }
    }
}
