package io.github.patrickacheung.client;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class ImageClientUtilsTest {
    @Test
    public void shouldGenerateGoodRotatedPathForNormalFilePath() {
        Path goodPath = Paths.get("/mnt", "c", "test", "image.jpg");
        Path newPath = ImageClientUtils.newOutputName(goodPath, "jpg");

        String expectedPath = "/mnt/c/test/image_rotated.jpg";
        String actualPath = newPath.toString();

        assertEquals(expectedPath, actualPath);
    }

    @Test
    public void shouldGenerateGoodRotatedPathEvenWithoutExtension() {
        Path goodPath = Paths.get("/mnt", "c", "test", "image");
        Path newPath = ImageClientUtils.newOutputName(goodPath, "jpg");

        String expectedPath = "/mnt/c/test/image_rotated";
        String actualPath = newPath.toString();

        assertEquals(expectedPath, actualPath);
    }

    @Test
    public void shouldGenerateGoodRotatedPathIfEndsWithDot() {
        Path goodPath = Paths.get("/mnt", "c", "test", "image.");
        Path newPath = ImageClientUtils.newOutputName(goodPath, "jpg");

        String expectedPath = "/mnt/c/test/image._rotated";
        String actualPath = newPath.toString();

        assertEquals(expectedPath, actualPath);
    }

    @Test
    public void shouldGenerateGoodRotatedPathIfEndsWithMultipleDots() {
        Path goodPath = Paths.get("/mnt", "c", "test", "image.....");
        Path newPath = ImageClientUtils.newOutputName(goodPath, "jpg");

        String expectedPath = "/mnt/c/test/image....._rotated";
        String actualPath = newPath.toString();

        assertEquals(expectedPath, actualPath);
    }

    @Test
    public void shouldGenerateGoodRotatedPathIfPathHasMultipleDotsWithinPathWithExtension() {
        Path goodPath = Paths.get("/mnt", "c..", "..test.", "i...mage.jpg");
        Path newPath = ImageClientUtils.newOutputName(goodPath, "jpg");

        String expectedPath = "/mnt/c../..test./i...mage_rotated.jpg";
        String actualPath = newPath.toString();

        assertEquals(expectedPath, actualPath);
    }

    @Test
    public void shouldGenerateGoodRotatedPathIfPathHasMultipleDotsWithinPathWithoutExtension() {
        Path goodPath = Paths.get("/mnt", "c..", "..test.", "i.mage");
        Path newPath = ImageClientUtils.newOutputName(goodPath, "jpg");

        String expectedPath = "/mnt/c../..test./i.mage_rotated";
        String actualPath = newPath.toString();

        assertEquals(expectedPath, actualPath);
    }
}
