package com.halot.nikitazolin.psp.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.halot.nikitazolin.psp.dto.ErrorResponse;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ApiResponse(
      responseCode = "400", 
      description = "Bad Request (validation error)", 
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  public ErrorResponse handleValidationException(MethodArgumentNotValidException ex) {
    String errorMessage = ex.getBindingResult().getFieldErrors().stream()
        .map(err -> String.format("%s: %s", err.getField(), err.getDefaultMessage())).collect(Collectors.joining("; "));
    log.warn("Validation error: {}", errorMessage);
    return new ErrorResponse(ErrorCode.VALIDATION_ERROR.name(),
        errorMessage.isEmpty() ? "Invalid request" : errorMessage);
  }

  @ExceptionHandler(PaymentNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ApiResponse(
      responseCode = "404", 
      description = "Payment with the specified ID was not found", 
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  public ErrorResponse handlePaymentNotFound(PaymentNotFoundException ex) {
    log.warn("Payment not found: {}", ex.getMessage());
    return new ErrorResponse(ErrorCode.NOT_FOUND.name(), ex.getMessage());
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ApiResponse(
      responseCode = "500", 
      description = "Internal Server Error", 
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  public ErrorResponse handleGenericException(Exception ex) {
    log.error("Unexpected error", ex);
    return new ErrorResponse(ErrorCode.INTERNAL_ERROR.name(), "Unexpected error occurred");
  }
}