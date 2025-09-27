package com.halot.nikitazolin.psp.service.acquirer;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AcquirerRouter {

  private final AcquirerA acquirerA;
  private final AcquirerB acquirerB;

  public Acquirer route(String bin) {
    int sum = bin.chars().map(Character::getNumericValue).sum();

    return (sum % 2 == 0) ? acquirerA : acquirerB;
  }
}
