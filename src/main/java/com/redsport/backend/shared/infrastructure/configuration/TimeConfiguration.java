package com.redsport.backend.shared.infrastructure.configuration;

import java.time.Clock;
import org.springframework.context.annotation.*;

@Configuration
public class TimeConfiguration {
  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }
}
