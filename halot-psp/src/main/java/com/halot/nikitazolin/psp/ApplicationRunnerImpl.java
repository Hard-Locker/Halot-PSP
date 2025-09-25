package com.halot.nikitazolin.psp;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@Profile("development")
@RequiredArgsConstructor
public class ApplicationRunnerImpl implements ApplicationRunner {

  @Override
  public void run(ApplicationArguments args) throws Exception {
    //
  }
}
