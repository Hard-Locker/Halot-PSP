package com.halot.nikitazolin.psp.dto;

import java.math.BigDecimal;

import com.halot.nikitazolin.psp.model.CurrencyCode;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PaymentRequest {

  @NotBlank
  @Pattern(regexp = "\\d{13,19}", message = "Card number must be 13-19 digits")
  private String cardNumber;

  @Min(1)
  @Max(12)
  private int expiryMonth;

  @Min(2020)
  @Max(2100)
  private int expiryYear;

  @NotBlank
  @Pattern(regexp = "\\d{3,4}", message = "CVV must be 3 or 4 digits")
  private String cvv;

  @NotNull
  @DecimalMin(value = "0.01", message = "Amount must be positive")
  private BigDecimal amount;

  @NotBlank
  private CurrencyCode currency;

  @NotBlank
  private String merchantId;
}
