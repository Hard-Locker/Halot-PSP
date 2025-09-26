package com.halot.nikitazolin.psp.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import com.halot.nikitazolin.psp.model.Transaction;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Component
@Getter
@Slf4j
public class Database {

  // Fictional analog of database
  private Map<Long, Transaction> transactions = new ConcurrentHashMap<>();
  private AtomicLong transactionIdCounter = new AtomicLong(0);
}
