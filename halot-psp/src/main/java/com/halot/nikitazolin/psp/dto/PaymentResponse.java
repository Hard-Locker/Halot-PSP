package com.halot.nikitazolin.psp.dto;

import com.halot.nikitazolin.psp.model.Status;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResponse {

  /**
   * Unique identifier for the payment transaction
   */
  private String transactionId;

  /**
   * Current status of the payment (APPROVED, DENIED, WAITING)
   */
  private Status status;

  /**
   * Human-readable message describing the transaction outcome
   */
  private String message;
}
