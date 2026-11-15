package com.redsport.backend.catalog.infrastructure.media;

import com.redsport.backend.shared.domain.model.DomainException;
import java.io.*;
import java.nio.file.*;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageStorage {
  private final Path directory;

  public ImageStorage(@Value("${app.media.directory:./storage/media}") String directory) {
    this.directory = Path.of(directory).toAbsolutePath().normalize();
  }

  public String save(MultipartFile file) throws IOException {
    if (file.isEmpty() || file.getSize() > 5 * 1024 * 1024)
      throw new IllegalArgumentException("La imagen debe pesar entre 1 byte y 5 MB");
    try (var stream = ImageIO.createImageInputStream(file.getInputStream())) {
      var readers = ImageIO.getImageReaders(stream);
      if (!readers.hasNext()) throw new IllegalArgumentException("Imagen inválida");
      var reader = readers.next();
      try {
        reader.setInput(stream);
        long pixels = (long) reader.getWidth(0) * reader.getHeight(0);
        if (pixels > 20000000)
          throw new IllegalArgumentException("La imagen supera los 20 megapíxeles");
        var image = reader.read(0);
        String name = UUID.randomUUID() + ".png";
        Files.createDirectories(directory);
        Path output = directory.resolve(name);
        if (!ImageIO.write(image, "png", output.toFile()))
          throw new IOException("No se pudo guardar la imagen");
        return "/api/media/" + name;
      } finally {
        reader.dispose();
      }
    }
  }

  public byte[] load(String name) throws IOException {
    if (!name.matches("[0-9a-f-]{36}\\.png")) throw DomainException.missing("Imagen no encontrada");
    var file = directory.resolve(name).normalize();
    if (!file.getParent().equals(directory) || !Files.isRegularFile(file))
      throw DomainException.missing("Imagen no encontrada");
    return Files.readAllBytes(file);
  }
}
