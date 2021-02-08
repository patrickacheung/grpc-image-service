package io.github.patrickacheung.client;

import com.google.common.collect.ImmutableMap;

import java.awt.image.BufferedImage;

import io.github.patrickacheung.NLImageRotateRequest;
import io.github.patrickacheung.NLImageRotateRequest.Rotation;

public class ImageClientUtils {
    private static final ImmutableMap<Integer, NLImageRotateRequest.Rotation> validRotationsToEnum = ImmutableMap.of(
        0, Rotation.NONE, 
        90, Rotation.NINETY_DEG, 
        180, Rotation.ONE_EIGHTY_DEG, 
        270, Rotation.TWO_SEVENTY_DEG);

    public static void validateCliRotationOptions(String rotation) throws IllegalArgumentException {
        try {
            int rotationNum = Integer.parseInt(rotation);
            if (!validRotationsToEnum.containsKey(rotationNum)) {
                throw new IllegalArgumentException("Invalid rotation request!");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid rotation request! " + e.getMessage());
        }
    }

    public static NLImageRotateRequest generateRequestProto(BufferedImage image, String rotation) {
        return null;
    }
}
