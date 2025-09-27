package com.halot.nikitazolin.psp.service;

import org.springframework.stereotype.Service;

import com.halot.nikitazolin.psp.dto.PaymentRequest;
import com.halot.nikitazolin.psp.dto.PaymentResponse;
import com.halot.nikitazolin.psp.model.Status;
import com.halot.nikitazolin.psp.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

  private final TransactionRepository transactionRepository;

  // TODO
  public PaymentResponse processPayment(PaymentRequest request) {
    return new PaymentResponse("TEMP-ID", Status.WAITING, "Processing");
  }

  // TODO
  public PaymentResponse getPaymentById(String transactionId) {
    return new PaymentResponse(transactionId, Status.WAITING, "Transaction lookup not yet implemented");
  }
}
