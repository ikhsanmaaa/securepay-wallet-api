package com.ikhsan.securepaywallet.common.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
