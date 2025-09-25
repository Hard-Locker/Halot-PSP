package com.halot.nikitazolin.psp.repository;

import org.springframework.stereotype.Repository;

import com.halot.nikitazolin.psp.model.Transaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class TransactionRepository implements ITransactionRepository {

  @Override
  public boolean insert(Transaction transaction) {
    //
    return false;
  }

  @Override
  public boolean update(Transaction transaction) {
    //
    return false;
  }

  @Override
  public boolean delete(Long transactionId) {
    //
    return false;
  }

}
