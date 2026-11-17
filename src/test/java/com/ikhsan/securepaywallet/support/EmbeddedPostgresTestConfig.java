package com.ikhsan.securepaywallet.support;

import java.io.IOException;

import javax.sql.DataSource;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

@TestConfiguration(proxyBeanMethods = false)
public class EmbeddedPostgresTestConfig {

    @Bean(destroyMethod = "close")
    EmbeddedPostgres embeddedPostgres() throws IOException {
        return EmbeddedPostgres.start();
    }

    @Bean
    DataSource dataSource(EmbeddedPostgres embeddedPostgres) {
        return embeddedPostgres.getPostgresDatabase();
    }
}
