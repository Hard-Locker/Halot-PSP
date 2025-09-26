package com.halot.nikitazolin.psp.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.halot.nikitazolin.psp.model.Transaction;

public interface ITransactionRepository {

  boolean insert(Transaction transaction);

  boolean update(Transaction transaction);

  boolean delete(Long transactionId);

  Optional<Transaction> findById(Long transactionId);

  Optional<Transaction> findByUuid(UUID transactionUuid);

  List<Transaction> findAll();

}
