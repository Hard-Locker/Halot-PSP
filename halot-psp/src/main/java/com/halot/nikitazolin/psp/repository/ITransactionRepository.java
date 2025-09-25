package com.halot.nikitazolin.psp.repository;

import com.halot.nikitazolin.psp.model.Transaction;

public interface ITransactionRepository {

  boolean insert(Transaction transaction);

  boolean update(Transaction transaction);

  boolean delete(Long transactionId);
}
