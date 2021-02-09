package io.github.patrickacheung.client;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.ByteString;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import io.github.patrickacheung.NLImage;
import io.github.patrickacheung.NLImageRotateRequest;
import io.github.patrickacheung.NLImageRotateRequest.Rotation;

public class ImageClientUtils {
    private static final ImmutableMap<Integer, NLImageRotateRequest.Rotation> validRotationsToEnum = ImmutableMap.of(
        0, Rotation.NONE, 
        90, Rotation.NINETY_DEG, 
        180, Rotation.ONE_EIGHTY_DEG, 
        270, Rotation.TWO_SEVENTY_DEG);
    
    private static final String SUFFIX = "_rotated";

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

        return new Image(bufferedImage, imageFilePath, format);
    }

    public static void writeImage(Image originalImage, NLImage imageToWriteProto) throws IOException {
        try (InputStream is = new ByteArrayInputStream(imageToWriteProto.getData().toByteArray())) {
            BufferedImage rotatedImage = ImageIO.read(is);
            File newFile = ImageClientUtils.newOutputName(originalImage.filePath, originalImage.format).toFile();
            ImageIO.write(rotatedImage, originalImage.getFormat(), newFile);
        }
    }

    @VisibleForTesting
    protected static Path newOutputName(Path originalFilePath, String format) {
        Path lastSegment = originalFilePath.getFileName();
        int dotIdx = lastSegment.toString().lastIndexOf(".");
        String extension = lastSegment.toString().substring(
            dotIdx == lastSegment.toString().length() -1 ? dotIdx : dotIdx + 1, 
            lastSegment.toString().length());
        
        if (dotIdx < 0 || !extension.toLowerCase().equals(format.toLowerCase())) {
            return originalFilePath.resolveSibling(Paths.get(originalFilePath.toString() + SUFFIX));
        }
        
        return originalFilePath.resolveSibling(Paths.get(lastSegment.toString().substring(0, dotIdx) + SUFFIX + "." + format));
    }

    private ImageClientUtils() {
        // private constructor
    }

    /** Wrapper class to hold image format */
    static class Image {
        private String format;
        private BufferedImage bufferedImage;
        private Path filePath;

        private Image(BufferedImage bufferedImage, Path originalPath, String format) {
            this.bufferedImage = bufferedImage;
            this.filePath = originalPath;
            this.format = format;
        }

        BufferedImage getBufferedImage() {
            return bufferedImage;
        }

        String getFormat() {
            return format;
        }

        Path getOriginalFilePath() {
            return filePath;
        }
    }
}
