package com.redsport.backend.catalog.interfaces.rest;

import com.redsport.backend.catalog.infrastructure.media.ImageStorage;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class MediaController {
  private final ImageStorage storage;

  public MediaController(ImageStorage storage) {
    this.storage = storage;
  }

  @PostMapping("/api/admin/media")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.CREATED)
  public Object upload(@RequestParam("file") MultipartFile file) throws IOException {
    return Map.of("url", storage.save(file));
  }

  @GetMapping("/api/media/{name}")
  public ResponseEntity<byte[]> read(@PathVariable String name) throws IOException {
    return ResponseEntity.ok()
        .contentType(MediaType.IMAGE_PNG)
        .header("X-Content-Type-Options", "nosniff")
        .body(storage.load(name));
  }
}
