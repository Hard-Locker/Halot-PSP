package com.halot.nikitazolin.psp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.halot.nikitazolin.psp.dto.ErrorResponse;
import com.halot.nikitazolin.psp.dto.PaymentRequest;
import com.halot.nikitazolin.psp.dto.PaymentResponse;
import com.halot.nikitazolin.psp.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for processing payment-related requests. Provides endpoints for
 * accepting payments and retrieving payment information using a transaction ID.
 */

@Slf4j
@RestController
@RequestMapping(path = "/payments", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Payments", description = "API for processing and retrieving payment information")
public class PaymentController {

  private final PaymentService paymentService;

  /**
   * <p>
   * Processes a request to accept a payment from a client (merchant).
   * </p>
   * <p>
   * It accepts a {@code PaymentRequest} object, validates it, and passes it to
   * the service layer for further processing (e.g., transaction execution,
   * database storage).
   * </p>
   *
   * @param request The {@link PaymentRequest} object containing payment details
   *                (merchant ID, amount, currency, etc.). Must be valid.
   * @return {@link ResponseEntity} with a {@link PaymentResponse} object, which
   *         contains the result of the payment processing (status, transaction
   *         ID, etc.).
   */
  @PostMapping
  @Operation(
      summary = "Process a new payment",
      description = "Accepts and processes a request to execute a payment transaction."
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "201", 
          description = "Payment processed successfully", 
          content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
      @ApiResponse(
          responseCode = "400", 
          description = "Bad Request (validation error)", 
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "500", 
          description = "Internal Server Error", 
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
  public ResponseEntity<PaymentResponse> receivingNewPayment(@Valid @RequestBody PaymentRequest request) {
    log.info("Received payment request for merchantId={}", request.getMerchantId());
    PaymentResponse response = paymentService.processPayment(request);
    log.info("Processed transaction, id={}, status={}", response.getTransactionId(), response.getStatus());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * <p>
   * Retrieves payment information using its unique transaction identifier.
   * </p>
   * <p>
   * Uses the provided {@code transactionId} to look up and return the
   * corresponding payment details.
   * </p>
   *
   * @param transactionId The unique string identifier of the transaction.
   * @return {@link ResponseEntity} with a {@link PaymentResponse} object, which
   *         contains the details and current status of the payment.
   */
  @GetMapping("/{transactionId}")
  @Operation(
      summary = "Get payment information by ID",
      description = "Returns the details of a payment using its unique transaction ID."
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200", 
          description = "Payment information successfully retrieved", 
          content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
      @ApiResponse(
          responseCode = "404", 
          description = "Payment with the specified ID was not found", 
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "500", 
          description = "Internal Server Error", 
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
  public ResponseEntity<PaymentResponse> getPayment(
      @Parameter(
          description = "The ID of the transaction (format: XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX)", 
          required = true, 
          example = "ecfcdaae-892a-4908-8265-e587fbcb99a3") 
      @PathVariable String transactionId) {
    log.info("Fetching payment info for transactionId={}", transactionId);
    PaymentResponse response = paymentService.getPaymentById(transactionId);
    log.info("Fetched transactionId={}, status={}", transactionId, response.getStatus());

    return ResponseEntity.ok(response);
  }
}
