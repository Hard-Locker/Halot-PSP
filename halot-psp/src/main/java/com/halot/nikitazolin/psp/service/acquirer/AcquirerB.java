package com.halot.nikitazolin.psp.service.acquirer;

import org.springframework.stereotype.Component;

import com.halot.nikitazolin.psp.model.Status;

@Component
public class AcquirerB implements Acquirer {

  private final String name = "FastestAcquirer";

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Status process(AcquirerRequest request) {
    char lastDigit = request.getCardNumber().charAt(request.getCardNumber().length() - 1);
    return (lastDigit - '0') % 2 == 0 ? Status.APPROVED : Status.DENIED;
  }
}
