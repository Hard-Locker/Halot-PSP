package com.halot.nikitazolin.psp.service.acquirer;

import com.halot.nikitazolin.psp.model.Status;

public interface Acquirer {

  String getName();

  Status process(AcquirerRequest request);
}
