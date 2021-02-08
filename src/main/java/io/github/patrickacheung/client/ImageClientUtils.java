package io.github.patrickacheung.client;

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.ByteString;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

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

    public static NLImageRotateRequest generateRequestProto(BufferedImage image, 
            NLImageRotateRequest.Rotation rotation) throws IOException {

        NLImage.Builder imageBuilder = NLImage.newBuilder();
        imageBuilder.setColor(image.getColorModel().getColorSpace().getType() == ColorSpace.TYPE_GRAY);
        imageBuilder.setWidth(image.getWidth());
        imageBuilder.setHeight(image.getHeight());
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", bos); // TODO: wrapper class to store image type
            imageBuilder.setData(ByteString.copyFrom(bos.toByteArray()));
        }
        NLImage imageProto = imageBuilder.build();

        NLImageRotateRequest.Builder requestBuilder = NLImageRotateRequest.newBuilder();
        requestBuilder.setRotation(rotation);
        requestBuilder.setImage(imageProto);
        
        return requestBuilder.build();
    }

    private ImageClientUtils() {
        // private constructor
    }
}
