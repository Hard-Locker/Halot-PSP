package com.halot.nikitazolin.psp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a payment transaction in the PSP system. Stores essential
 * transaction details with masked sensitive data and tracks updates.
 */

@Data
@AllArgsConstructor
public class Transaction {

  /**
   * Unique identifier for the transaction.
   */
  private UUID id;

  /**
   * Bank Identification Number (first 6 digits of the card number) for routing
   * and analytics.
   */
  private String bin;

  /**
   * Last 4 digits of the card number for identification (e.g., 1234).
   */
  private String lastFourDigits;

  /**
   * Expiry date of the card (month and year, e.g., 2027-05).
   */
  private YearMonth expiryDate;

  /**
   * Transaction amount.
   */
  private BigDecimal amount;

  /**
   * Currency code (e.g., USD, EUR) following ISO 4217.
   */
  private CurrencyCode currency;

  /**
   * Unique identifier of the merchant initiating the transaction.
   */
  private String merchantId;

  /**
   * Timestamp when the transaction was created.
   */
  private LocalDateTime createdAt;

  /**
   * Timestamp of the last update to the transaction (e.g., status change).
   */
  private LocalDateTime updatedAt;

  /**
   * Current status of the transaction (APPROVED, DENIED, WAITING).
   */
  private Status transactionStatus;

  /**
   * Current status of the response from the acquirer (APPROVED, DENIED, WAITING).
   */
  private Status acquirerResponseStatus;
}
