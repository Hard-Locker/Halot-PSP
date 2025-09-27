package com.halot.nikitazolin.psp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Error response object")
public class ErrorResponse {
  @Schema(description = "Machine-readable error code", example = "VALIDATION_ERROR")
  private String code;

  @Schema(description = "Human-readable error message", example = "amount: must be greater than 0")
  private String message;
}
