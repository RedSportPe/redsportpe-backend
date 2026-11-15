package com.redsport.backend;

import static org.junit.jupiter.api.Assertions.*;

import com.redsport.backend.catalog.infrastructure.media.ImageStorage;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class ImageStorageTest {
  @TempDir Path directory;

  @Test
  void savesAReencodedImageUnderAServerGeneratedName() throws Exception {
    var bytes = new ByteArrayOutputStream();
    ImageIO.write(new BufferedImage(3, 2, BufferedImage.TYPE_INT_RGB), "png", bytes);
    var storage = new ImageStorage(directory.toString());
    String url =
        storage.save(
            new MockMultipartFile("file", "../unsafe.html", "text/html", bytes.toByteArray()));
    assertTrue(url.matches("/api/media/[0-9a-f-]{36}\\.png"));
    var decoded =
        ImageIO.read(
            new ByteArrayInputStream(storage.load(url.substring(url.lastIndexOf('/') + 1))));
    assertEquals(3, decoded.getWidth());
    assertEquals(2, decoded.getHeight());
  }

  @Test
  void rejectsNonImagesAndPathTraversal() {
    var storage = new ImageStorage(directory.toString());
    assertThrows(
        IllegalArgumentException.class,
        () ->
            storage.save(
                new MockMultipartFile("file", "x.png", "image/png", "not an image".getBytes())));
    assertThrows(RuntimeException.class, () -> storage.load("../secret.png"));
  }
}
