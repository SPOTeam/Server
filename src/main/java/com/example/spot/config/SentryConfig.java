package com.example.spot.config;

import io.sentry.Sentry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("prod")
@Slf4j
public class SentryConfig {

    // Sentry DSN
    @Value("${sentry.dsn}")
    private String sentryDsn;

    // Sentry Environment
    @Value("${sentry.environment}")
    private String environment;

    /**
     * Sentry를 초기화합니다.
     */
    @PostConstruct
    public void init() {
        Sentry.init(options -> {
            options.setDsn(sentryDsn);
            options.setEnvironment(environment);
        });
    }
}

