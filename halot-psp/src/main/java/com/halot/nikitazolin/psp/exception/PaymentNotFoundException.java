package com.halot.nikitazolin.psp.exception;

public class PaymentNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public PaymentNotFoundException(String transactionId) {
    super("Payment with transactionId=" + transactionId + " was not found");
  }
}