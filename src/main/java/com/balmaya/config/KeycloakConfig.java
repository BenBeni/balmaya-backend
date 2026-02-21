package com.balmaya.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(KeycloakProperties.class)
public class KeycloakConfig {
  @Bean
  RestTemplate keycloakRestTemplate(RestTemplateBuilder builder) {
    return builder.build();
  }
}
