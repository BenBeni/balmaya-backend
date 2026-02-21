package com.balmaya.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {
  private String baseUrl;
  private String realm;
  private ClientApp clientApp = new ClientApp();

  @Data
  public static class ClientApp {
    private String realm;
    private String clientId;
    private String clientSecret;
  }
}

