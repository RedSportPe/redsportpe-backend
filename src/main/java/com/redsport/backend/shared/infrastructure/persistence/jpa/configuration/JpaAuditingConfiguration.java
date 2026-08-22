package com.redsport.backend.shared.infrastructure.persistence.jpa.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Turns on @CreatedDate / @LastModifiedDate auto-filling across the app. */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfiguration {
}