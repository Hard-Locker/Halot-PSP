package com.halot.nikitazolin.psp.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.halot.nikitazolin.psp.dto.PaymentRequest;
import com.halot.nikitazolin.psp.dto.PaymentResponse;
import com.halot.nikitazolin.psp.model.CurrencyCode;
import com.halot.nikitazolin.psp.model.Status;
import com.halot.nikitazolin.psp.model.Transaction;
import com.halot.nikitazolin.psp.repository.TransactionRepository;
import com.halot.nikitazolin.psp.service.acquirer.Acquirer;
import com.halot.nikitazolin.psp.service.acquirer.AcquirerRequest;
import com.halot.nikitazolin.psp.service.acquirer.AcquirerRouter;
import com.halot.nikitazolin.psp.util.CardValidator;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

  @InjectMocks
  private PaymentService paymentService;

  @Mock
  private TransactionRepository transactionRepository;

  @Mock
  private AcquirerRouter acquirerRouter;

  @Mock
  private Acquirer mockAcquirer;

  private MockedStatic<CardValidator> mockedCardValidator;

  private PaymentRequest validRequest;
  private final String VALID_CARD = "12345678901";
  private final String INVALID_CARD = "12345678901";

  @BeforeEach
  void setUp() {
    mockedCardValidator = Mockito.mockStatic(CardValidator.class);
    mockedCardValidator.when(() -> CardValidator.isValidCardNumber(anyString())).thenReturn(true);
    mockedCardValidator.when(() -> CardValidator.isCardExpired(any(YearMonth.class))).thenReturn(false);

    validRequest = new PaymentRequest();
    validRequest.setMerchantId("ID123");
    validRequest.setAmount(new BigDecimal("100.00"));
    validRequest.setCurrency(CurrencyCode.EUR);
    validRequest.setCardNumber(VALID_CARD);
    validRequest.setCvv("123");
    validRequest.setExpiryMonth(12);
    validRequest.setExpiryYear(YearMonth.now().getYear() + 1);
  }

  @AfterEach
  void tearDown() {
    mockedCardValidator.close();
  }

  // processPayment

  @Test
  void processPayment_ApprovedByAcquirer_ReturnsApprovedStatusAndSaves() {
    // Arrange
    when(acquirerRouter.route(anyString())).thenReturn(mockAcquirer);
    when(mockAcquirer.getName()).thenReturn("TestAcquirer");
    when(mockAcquirer.process(any(AcquirerRequest.class))).thenReturn(Status.APPROVED);

    ArgumentCaptor<Transaction> insertedTransactionCaptor = ArgumentCaptor.forClass(Transaction.class);
    when(transactionRepository.insert(insertedTransactionCaptor.capture())).thenReturn(true);

    ArgumentCaptor<Transaction> updatedTransactionCaptor = ArgumentCaptor.forClass(Transaction.class);

    // Act
    PaymentResponse response = paymentService.processPayment(validRequest);

    // Assert
    assertEquals(Status.APPROVED, response.getStatus());
    verify(transactionRepository, times(1)).insert(any(Transaction.class));
    verify(transactionRepository, times(1)).update(updatedTransactionCaptor.capture());
    assertSame(insertedTransactionCaptor.getValue(), updatedTransactionCaptor.getValue(),
        "The updated object must be the same as the inserted one");
    assertEquals(Status.APPROVED, updatedTransactionCaptor.getValue().getTransactionStatus(),
        "The transaction status in the database should be APPROVED");
  }

  @Test
  void processPayment_DeniedByAcquirer_ReturnsDeniedStatusAndSaves() {
    // Arrange
    when(acquirerRouter.route(anyString())).thenReturn(mockAcquirer);
    when(mockAcquirer.process(any(AcquirerRequest.class))).thenReturn(Status.DENIED);

    ArgumentCaptor<Transaction> insertedTransactionCaptor = ArgumentCaptor.forClass(Transaction.class);
    when(transactionRepository.insert(insertedTransactionCaptor.capture())).thenReturn(true);

    ArgumentCaptor<Transaction> updatedTransactionCaptor = ArgumentCaptor.forClass(Transaction.class);

    // Act
    PaymentResponse response = paymentService.processPayment(validRequest);

    // Assert
    assertEquals(Status.DENIED, response.getStatus());
    verify(transactionRepository, times(1)).insert(any(Transaction.class));
    verify(transactionRepository, times(1)).update(updatedTransactionCaptor.capture());
    assertSame(insertedTransactionCaptor.getValue(), updatedTransactionCaptor.getValue());
    assertEquals(Status.DENIED, updatedTransactionCaptor.getValue().getTransactionStatus());
  }

  @Test
  void processPayment_AcquirerThrowsException_TransactionStaysWaiting() {
    // Arrange
    when(acquirerRouter.route(anyString())).thenReturn(mockAcquirer);
    when(mockAcquirer.process(any(AcquirerRequest.class))).thenThrow(new RuntimeException("Acquirer timeout"));

    when(transactionRepository.insert(any(Transaction.class))).thenReturn(true);

    // Act & Assert
    assertThrows(RuntimeException.class, () -> paymentService.processPayment(validRequest));
    verify(transactionRepository, times(1)).insert(any(Transaction.class));
    verify(transactionRepository, never()).update(any(Transaction.class));
  }

  @Test
  void processPayment_InvalidCardNumberLuhn_ReturnsDeniedWithoutExternalCalls() {
    // Arrange
    validRequest.setCardNumber(INVALID_CARD);
    mockedCardValidator.when(() -> CardValidator.isValidCardNumber(INVALID_CARD)).thenReturn(false);

    // Act
    PaymentResponse response = paymentService.processPayment(validRequest);

    // Assert
    assertEquals(Status.DENIED, response.getStatus(), "DENIED status expected for invalid card number");
    assertTrue(response.getMessage().contains("Luhn check failed"), "The message should indicate a validation error");
    verify(transactionRepository, never()).insert(any(Transaction.class));
    verify(transactionRepository, never()).update(any(Transaction.class));
    verify(acquirerRouter, never()).route(anyString());
    verify(mockAcquirer, never()).process(any(AcquirerRequest.class));
    mockedCardValidator.verify(() -> CardValidator.isCardExpired(any(YearMonth.class)), times(1));
  }

  // getPaymentById

  @Test
  void getPaymentById_TransactionFound_ReturnsSuccessResponse() {
    // Arrange
    UUID existingId = UUID.randomUUID();
    Transaction foundTransaction = new Transaction(existingId, "bin", "last4", YearMonth.now(), BigDecimal.TEN,
        CurrencyCode.USD, "MERCH1", null, null, Status.APPROVED, Status.APPROVED);

    when(transactionRepository.findByUuid(existingId)).thenReturn(Optional.of(foundTransaction));

    // Act
    PaymentResponse response = paymentService.getPaymentById(existingId.toString());

    // Assert
    assertEquals(Status.APPROVED, response.getStatus());
    assertEquals(existingId.toString(), response.getTransactionId());
    verify(transactionRepository, times(1)).findByUuid(existingId);
  }

  @Test
  void getPaymentById_TransactionNotFound_ReturnsDeniedNotFoundResponse() {
    // Arrange
    UUID nonExistentId = UUID.randomUUID();
    when(transactionRepository.findByUuid(nonExistentId)).thenReturn(Optional.empty());

    // Act
    PaymentResponse response = paymentService.getPaymentById(nonExistentId.toString());

    // Assert
    assertEquals(Status.DENIED, response.getStatus());
    assertEquals("Transaction not found", response.getMessage());
    verify(transactionRepository, times(1)).findByUuid(nonExistentId);
  }

  @Test
  void getPaymentById_InvalidIdFormat_ReturnsDeniedInvalidFormatResponse() {
    // Arrange
    String invalidId = "not-a-uuid";

    // Act
    PaymentResponse response = paymentService.getPaymentById(invalidId);

    // Assert
    assertEquals(Status.DENIED, response.getStatus());
    assertEquals("Invalid transaction ID format", response.getMessage());
    verify(transactionRepository, never()).findByUuid(any(UUID.class));
  }
}
