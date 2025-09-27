package com.halot.nikitazolin.psp.service.acquirer;

import java.math.BigDecimal;

import com.halot.nikitazolin.psp.model.CurrencyCode;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AcquirerRequest {

  private String cardNumber;
  private int expiryMonth;
  private int expiryYear;
  private String cvv;
  private BigDecimal amount;
  private CurrencyCode currency;
  private String merchantId;
}
