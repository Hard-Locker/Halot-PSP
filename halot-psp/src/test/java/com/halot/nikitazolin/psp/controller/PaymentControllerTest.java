package com.halot.nikitazolin.psp.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.halot.nikitazolin.psp.dto.PaymentRequest;
import com.halot.nikitazolin.psp.dto.PaymentResponse;
import com.halot.nikitazolin.psp.exception.GlobalExceptionHandler;
import com.halot.nikitazolin.psp.exception.PaymentNotFoundException;
import com.halot.nikitazolin.psp.model.CurrencyCode;
import com.halot.nikitazolin.psp.model.Status;
import com.halot.nikitazolin.psp.service.PaymentService;

@WebMvcTest(PaymentController.class)
@Import(GlobalExceptionHandler.class)
class PaymentControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private PaymentService paymentService;

  private PaymentRequest createValidPaymentRequest() {
    PaymentRequest request = new PaymentRequest();
    request.setMerchantId("ID123");
    request.setAmount(new BigDecimal("100.00"));
    request.setCurrency(CurrencyCode.USD);
    request.setCardNumber("4444555566667777");
    request.setCvv("123");
    request.setExpiryMonth(12);
    request.setExpiryYear(2025);
    return request;
  }

  //POST /payments

  @Test
  void receivingNewPayment_ValidRequest_Returns201Created() throws Exception {
    // Arrange
    PaymentRequest request = createValidPaymentRequest();
    String transactionId = UUID.randomUUID().toString();
    PaymentResponse expectedResponse = new PaymentResponse(transactionId, Status.APPROVED, "Transaction accepted.");

    when(paymentService.processPayment(any(PaymentRequest.class))).thenReturn(expectedResponse);

    // Act & Assert
    mockMvc
        .perform(
            post("/payments").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.transactionId", is(transactionId)))
        .andExpect(jsonPath("$.status", is(Status.APPROVED.toString())))
        .andExpect(jsonPath("$.message", is("Transaction accepted.")));
  }

  @Test
  void receivingNewPayment_InvalidCardNumber_Returns400BadRequest() throws Exception {
    // Arrange
    PaymentRequest invalidRequest = createValidPaymentRequest();
    invalidRequest.setCardNumber("123");

    // Act & Assert
    mockMvc
        .perform(post("/payments").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")));
  }

  @Test
  void receivingNewPayment_ServiceThrowsInternalError_Returns500InternalServerError() throws Exception {
    // Arrange
    PaymentRequest request = createValidPaymentRequest();

    when(paymentService.processPayment(any(PaymentRequest.class)))
        .thenThrow(new RuntimeException("Database error during transaction."));

    // Act & Assert
    mockMvc
        .perform(
            post("/payments").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
  }

  //GET /payments/{transactionId}

  @Test
  void getPayment_PaymentFound_Returns200Ok() throws Exception {
    // Arrange
    String transactionId = "ecfcdaae-892a-4908-8265-e587fbcb99a3";
    PaymentResponse expectedResponse = new PaymentResponse(transactionId, Status.APPROVED, "Payment settled.");

    when(paymentService.getPaymentById(transactionId)).thenReturn(expectedResponse);

    // Act & Assert
    mockMvc.perform(get("/payments/{transactionId}", transactionId).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.transactionId", is(transactionId)))
        .andExpect(jsonPath("$.status", is(Status.APPROVED.toString())))
        .andExpect(jsonPath("$.message", is("Payment settled.")));
  }

  @Test
  void getPayment_PaymentNotFound_Returns404NotFound() throws Exception {
    // Arrange
    String nonExistentId = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";

    when(paymentService.getPaymentById(nonExistentId)).thenThrow(new PaymentNotFoundException("Payment not found"));

    // Act & Assert
    mockMvc.perform(get("/payments/{transactionId}", nonExistentId).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code", is("NOT_FOUND")));
  }

  @Test
  void getPayment_ServiceThrowsException_Returns500InternalServerError() throws Exception {
    // Arrange
    String validId = "11111111-2222-3333-4444-555555555555";

    when(paymentService.getPaymentById(validId))
        .thenThrow(new IllegalStateException("System is temporarily unavailable."));

    // Act & Assert
    mockMvc.perform(get("/payments/{transactionId}", validId).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
  }
}
