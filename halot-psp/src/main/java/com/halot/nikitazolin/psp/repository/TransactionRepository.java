package com.halot.nikitazolin.psp.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.halot.nikitazolin.psp.model.Transaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class TransactionRepository implements ITransactionRepository {

  private final Database database;

  @Override
  public boolean insert(Transaction transaction) {
    long newId = database.getTransactionIdCounter().incrementAndGet();
    database.getTransactions().put(newId, transaction);
    log.info("Inserted transaction: dbId={}, uuid={}", newId, transaction.getId());
    return true;
  }

  @Override
  public boolean update(Transaction transaction) {
    Optional<Map.Entry<Long, Transaction>> entry = database.getTransactions().entrySet().stream()
        .filter(e -> e.getValue().getId().equals(transaction.getId())).findFirst();

    if (entry.isPresent()) {
      database.getTransactions().put(entry.get().getKey(), transaction);
      log.info("Updated transaction: dbId={}, uuid={}", entry.get().getKey(), transaction.getId());
      return true;
    } else {
      log.warn("Transaction not found for update, uuid={}", transaction.getId());
      return false;
    }
  }

  @Override
  public boolean delete(Long transactionId) {
    Transaction removed = database.getTransactions().remove(transactionId);

    if (removed != null) {
      log.info("Deleted transaction: dbId={}, uuid={}", transactionId, removed.getId());
      return true;
    } else {
      log.warn("Transaction not found for delete, dbId={}", transactionId);
      return false;
    }
  }

  @Override
  public Optional<Transaction> findById(Long transactionId) {
    return Optional.ofNullable(database.getTransactions().get(transactionId));
  }

  @Override
  public Optional<Transaction> findByUuid(UUID transactionUuid) {
    return database.getTransactions().values().stream().filter(tx -> tx.getId().equals(transactionUuid)).findFirst();
  }

  @Override
  public List<Transaction> findAll() {
    return new ArrayList<>(database.getTransactions().values());
  }
}
