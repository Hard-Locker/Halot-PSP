package com.halot.nikitazolin.psp.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.halot.nikitazolin.psp.model.CurrencyCode;
import com.halot.nikitazolin.psp.model.Status;
import com.halot.nikitazolin.psp.model.Transaction;

@DisplayName("TransactionRepository Unit Tests")
public class TransactionRepositoryTest {

  private Database database;
  private TransactionRepository transactionRepository;

  private Transaction transaction1 = new Transaction(UUID.fromString("00000000-0000-0000-0000-000000000001"), "123456",
      "1234", YearMonth.of(2027, 5), new BigDecimal("100.50"), CurrencyCode.USD, "abc123",
      LocalDateTime.of(2025, 1, 1, 12, 30, 50), LocalDateTime.of(2025, 1, 1, 12, 31, 50), Status.APPROVED,
      Status.APPROVED);
  private Transaction transaction2 = new Transaction(UUID.fromString("00000000-0000-0000-0000-000000000002"), "654321",
      "4321", YearMonth.of(2030, 10), new BigDecimal("200.50"), CurrencyCode.USD, "321cba",
      LocalDateTime.of(2025, 12, 12, 12, 59, 59), LocalDateTime.of(2025, 12, 12, 22, 59, 59), Status.DENIED,
      Status.DENIED);

  @BeforeEach
  void setUp() {
    database = new Database();
    transactionRepository = new TransactionRepository(database);

    // Reset ID counter
    database.getTransactionIdCounter().set(0);
  }

  @Test
  void insert_InsertTransaction_ReturnsTrue() {
    // Act
    boolean result = transactionRepository.insert(transaction1);

    // Assert
    assertTrue(result, "The insert should return true");
  }

  @Test
  void insert_MultipleInsertTransactions_RetyrnsDbSizeMustBe2() {
    // Act
    transactionRepository.insert(transaction1);
    transactionRepository.insert(transaction2);

    // Assert
    assertEquals(2, database.getTransactions().size(), "DB should have 2 transactions");
  }

  @Test
  void update_UpdateExistingTransaction_ReturnsTrue() {
    // Arrange
    transactionRepository.insert(transaction1);
    transaction1.setAcquirerResponseStatus(Status.DENIED);

    // Act
    boolean result = transactionRepository.update(transaction1);
    Optional<Transaction> found = transactionRepository.findById(1L);
    Transaction updatedTransaction = found.get();

    // Assert
    assertTrue(result, "Updating an existing transaction should return true");
    assertEquals(Status.DENIED, updatedTransaction.getAcquirerResponseStatus(),
        "Acquirer response status should be updated to DENIED");
  }

  @Test
  void update_UpdateNonExistingTransaction_ReturnsFalse() {
    // Act
    boolean result = transactionRepository.update(transaction1);

    // Assert
    assertFalse(result, "Updating a non-existent transaction should return false");
  }

  @Test
  void delete_DeleteExistingTransaction_ReturnsTrue() {
    // Arrange
    transactionRepository.insert(transaction1);

    // Act
    boolean result = transactionRepository.delete(1L);

    // Assert
    assertTrue(result, "Deleting an existing transaction should return true");
  }

  @Test
  void delete_DeleteNonExistingTransaction_ReturnsFalse() {
    // Act
    boolean result = transactionRepository.delete(1L);

    // Assert
    assertFalse(result, "Deleting a non-existing transaction should return false");
  }

  @Test
  void findById_FindExistingId_ReturnsValueSameToInsert() {
    // Arrange
    transactionRepository.insert(transaction1);

    // Act
    Optional<Transaction> found = transactionRepository.findById(1L);
    Transaction updatedTransaction = found.get();

    // Assert
    assertEquals(transaction1, updatedTransaction, "The inserted transaction must be found");
  }

  @Test
  void findById_FindNonExistingId_ReturnsEmpty() {
    // Act
    Optional<Transaction> found = transactionRepository.findById(1L);

    // Assert
    assertTrue(found.isEmpty(), "Transaction should not be found");
  }

  @Test
  void findByUuid_FindExistingUuid_ReturnsValueSameToInsert() {
    // Arrange
    transactionRepository.insert(transaction1);

    // Act
    Optional<Transaction> found = transactionRepository.findByUuid(transaction1.getId());
    Transaction updatedTransaction = found.get();

    // Assert
    assertEquals(transaction1, updatedTransaction, "The inserted transaction must be found");
  }

  @Test
  void findByUuid_FindNonExistingUuid_ReturnsEmpty() {
    // Act
    Optional<Transaction> found = transactionRepository.findByUuid(transaction1.getId());

    // Assert
    assertTrue(found.isEmpty(), "Transaction should not be found");
  }

  @Test
  void findAll_FindExistingUuid_ReturnsDbSizeMustBe2() {
    // Arrange
    transactionRepository.insert(transaction1);
    transactionRepository.insert(transaction2);

    // Act
    List<Transaction> found = transactionRepository.findAll();

    // Assert
    assertEquals(2, found.size(), "The inserted transaction must be found");
  }

  @Test
  void findAll_FindNonExistingUuid_ReturnsEmpty() {
    // Act
    List<Transaction> found = transactionRepository.findAll();

    // Assert
    assertTrue(found.isEmpty(), "Transaction should not be found");
  }
}
