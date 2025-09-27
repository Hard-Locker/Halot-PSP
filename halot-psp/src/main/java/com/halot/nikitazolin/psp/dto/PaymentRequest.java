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
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PaymentRequest {

  @NotBlank(message = "Card number must not be blank")
  @Pattern(regexp = "\\d{13,19}", message = "Card number must be 13-19 digits")
  private String cardNumber;

  @Min(value = 1, message = "Expiry month must be between 1 and 12")
  @Max(value = 12, message = "Expiry month must be between 1 and 12")
  private int expiryMonth;

  @Min(value = 2020, message = "Expiry year must not be earlier than 2020")
  @Max(value = 2100, message = "Expiry year must not be later than 2100")
  private int expiryYear;

  @NotBlank(message = "CVV must not be blank")
  @Pattern(regexp = "\\d{3,4}", message = "CVV must be 3 or 4 digits")
  private String cvv;

  @NotNull(message = "Amount must be specified")
  @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be at least 0.01")
  private BigDecimal amount;

  @NotNull(message = "Currency must be specified")
  private CurrencyCode currency;

  @NotBlank(message = "Merchant ID must not be blank")
  private String merchantId;
}
