package com.redsport.backend.catalog.interfaces.rest.resources;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record SaveProductResource(
    @NotBlank @Pattern(regexp = "[A-Z0-9]{1,10}") String productCode,
    @NotBlank @Size(max = 150) String name,
    @Size(max = 255) String description,
    @Size(max = 60) String category,
    @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal price,
    boolean published,
    boolean featured,
    @NotEmpty @Size(max = 500) List<@Valid VariantData> variants,
    @NotNull @Size(max = 20) List<@NotBlank @Size(max = 500) String> imageUrls) {
  public com.redsport.backend.catalog.application.commands.SaveProductCommand toCommand() {
    return new com.redsport.backend.catalog.application.commands.SaveProductCommand(
        productCode,
        name,
        description,
        category,
        price,
        published,
        featured,
        variants.stream()
            .map(
                v ->
                    new com.redsport.backend.catalog.application.commands.SaveProductCommand
                        .VariantData(v.sku(), v.gender(), v.size(), v.color(), v.totalStock()))
            .toList(),
        imageUrls);
  }

  public record VariantData(
      @NotBlank String sku,
      @NotBlank String gender,
      @NotBlank String size,
      @NotBlank String color,
      @Min(0) int totalStock) {}
}
