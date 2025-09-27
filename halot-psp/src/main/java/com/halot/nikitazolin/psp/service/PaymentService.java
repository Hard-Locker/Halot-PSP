package com.halot.nikitazolin.psp.service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.halot.nikitazolin.psp.dto.PaymentRequest;
import com.halot.nikitazolin.psp.dto.PaymentResponse;
import com.halot.nikitazolin.psp.model.Status;
import com.halot.nikitazolin.psp.model.Transaction;
import com.halot.nikitazolin.psp.repository.TransactionRepository;
import com.halot.nikitazolin.psp.service.acquirer.Acquirer;
import com.halot.nikitazolin.psp.service.acquirer.AcquirerRequest;
import com.halot.nikitazolin.psp.service.acquirer.AcquirerRouter;
import com.halot.nikitazolin.psp.util.CardValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class responsible for handling the core business logic of payment
 * processing. It manages card validation, transaction persistence, routing to
 * external acquirers, and updating transaction status.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

  private final TransactionRepository transactionRepository;
  private final AcquirerRouter acquirerRouter;

  /**
   * Processes a payment request by performing card validation, creating an
   * initial transaction, routing the transaction to an appropriate acquirer, and
   * updating the transaction status based on the acquirer's response.
   *
   * The workflow includes:
   * 1. Card expiration check (logging only).
   * 2. Card number validation via the Luhn algorithm.
   * 3. Initial transaction creation and insertion into the database.
   * 4. Routing to an Acquirer based on the card's BIN.
   * 5. Sending the request to the selected Acquirer.
   * 6. Updating the transaction record with the final status.
   * 7. Generating a response for the merchant.
   *
   * @param request The {@link PaymentRequest} containing payment details.
   * @return A {@link PaymentResponse} indicating the outcome of the payment process.
   */
  public PaymentResponse processPayment(PaymentRequest request) {
    log.info("Starting payment processing for merchantId: {}", request.getMerchantId());

    // 1. Checking the card expiration date
    if (CardValidator.isCardExpired(YearMonth.of(request.getExpiryYear(), request.getExpiryMonth()))) {
      log.trace("Card is expired YYYY-MM: {}/{}", request.getExpiryYear(), request.getExpiryMonth());
    }
    
    log.debug("Card expiration check passed for merchantId: {}", request.getMerchantId());

    // 2. Checking card number by Luhn algorithm
    boolean validNumber = CardValidator.isValidCardNumber(request.getCardNumber());

    if (!validNumber) {
      log.warn("Invalid card number (Luhn check failed) for merchantId: {}", request.getMerchantId());
      return new PaymentResponse("NONE", Status.DENIED, "Invalid card number (Luhn check failed)");
    }

    log.debug("Card number validated successfully for merchantId: {}", request.getMerchantId());

    // 3. Create transaction
    Transaction transaction = createInitialTransaction(request);
    transactionRepository.insert(transaction);
    log.debug("Created initial transaction with ID: {} for merchantId: {}", transaction.getId(),
        request.getMerchantId());
    log.debug("Transaction {} inserted into database", transaction.getId());

    // 4. Choose routing
    Acquirer acquirer = acquirerRouter.route(transaction.getBin());
    log.info("Routed transaction {} to acquirer: {}", transaction.getId(), acquirer.getName());

    // 5. Send to the acquirer
    AcquirerRequest acquirerRequest = createAcquirerRequest(request);
    log.debug("Sending request to acquirer : {} for transaction: {}", acquirer.getName(), transaction.getId());
    Status acquirerResponse = acquirer.process(acquirerRequest);
    log.debug("Received response from acquirer for transaction {}: {}", transaction.getId(), acquirerResponse.name());

    // 6. Update transaction in database
    transaction.setTransactionStatus(acquirerResponse);
    transaction.setAcquirerResponseStatus(acquirerResponse);
    transaction.setUpdatedAt(LocalDateTime.now());
    transactionRepository.update(transaction);
    log.debug("Updated transaction {} in database with status: {}", transaction.getId(), acquirerResponse.name());

    // 7. Generating response to the merchant
    String message = acquirerResponse == Status.APPROVED ? "Payment approved" : "Payment denied";
    log.info("Payment processing completed for transaction {}: {}", transaction.getId(), message);
    return new PaymentResponse(transaction.getId().toString(), acquirerResponse, message);
  }

  /**
   * Retrieves the status and details of a specific payment transaction by its ID.
   *
   * The process involves:
   * 1. Validating the transaction ID format (must be a valid UUID).
   * 2. Searching for the transaction in the database.
   * 3. Returning the transaction status or an error if not found.
   *
   * @param transactionId The unique identifier (UUID) of the transaction as a String.
   * @return A {@link PaymentResponse} containing the transaction status and details, or an error message.
   */
  public PaymentResponse getPaymentById(String transactionId) {
    log.info("Retrieving payment status for transactionId: {}", transactionId);

    UUID uuid;

    // Check valid UUID
    try {
      uuid = UUID.fromString(transactionId);
      log.debug("Transaction ID {} is a valid UUID", transactionId);
    } catch (IllegalArgumentException e) {
      log.debug("Invalid transaction ID format: {}", transactionId);
      return new PaymentResponse(transactionId, Status.DENIED, "Invalid transaction ID format");
    }

    // Search in database
    Optional<Transaction> transactionOpt = transactionRepository.findByUuid(uuid);

    if (transactionOpt.isEmpty()) {
      log.debug("Transaction not found for ID: {}", transactionId);
      return new PaymentResponse(transactionId, Status.DENIED, "Transaction not found");
    }

    // Show information about the found transaction
    Transaction transaction = transactionOpt.get();
    log.debug("Transaction found for ID: {}, status: {}", transactionId, transaction.getTransactionStatus().name());
    return new PaymentResponse(transaction.getId().toString(), transaction.getTransactionStatus(), "Transaction found");
  }

  /**
   * Creates an initial {@link Transaction} object from the
   * {@link PaymentRequest}. This sets the initial state, generates a new UUID,
   * and extracts non-sensitive card parts (BIN, last four).
   *
   * @param request The incoming payment request.
   * @return A new {@link Transaction} object ready for database insertion.
   */
  private Transaction createInitialTransaction(PaymentRequest request) {
    String cardNumber = request.getCardNumber();
    String bin = cardNumber.substring(0, 6);
    String lastFour = cardNumber.substring(cardNumber.length() - 4);

    return new Transaction(UUID.randomUUID(), bin, lastFour,
        YearMonth.of(request.getExpiryYear(), request.getExpiryMonth()), request.getAmount(), request.getCurrency(),
        request.getMerchantId(), LocalDateTime.now(), LocalDateTime.now(), Status.WAITING, Status.WAITING);
  }

  /**
   * Converts the merchant-facing {@link PaymentRequest} into an
   * {@link AcquirerRequest} format suitable for communication with the external
   * Acquirer/Payment Processor.
   *
   * @param request The incoming payment request.
   * @return An {@link AcquirerRequest} object.
   */
  private AcquirerRequest createAcquirerRequest(PaymentRequest request) {
    return new AcquirerRequest(request.getCardNumber(), request.getExpiryMonth(), request.getExpiryYear(),
        request.getCvv(), request.getAmount(), request.getCurrency(), request.getMerchantId());
  }
}
