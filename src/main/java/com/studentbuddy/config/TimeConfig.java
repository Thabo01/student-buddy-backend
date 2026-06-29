package com.studentbuddy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Exposes a {@link Clock} bean. Production uses the system clock; tests can
 * inject a fixed clock so time-dependent logic (e.g. "this month") is
 * deterministic and repeatable.
 */
@Configuration
public class TimeConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
