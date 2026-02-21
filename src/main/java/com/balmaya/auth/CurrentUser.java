package com.balmaya.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public final class CurrentUser {
  private CurrentUser() {}

  public static String userId(Authentication auth) {
    if (auth instanceof JwtAuthenticationToken jwt) return jwt.getToken().getSubject();
    throw new IllegalStateException("Unsupported authentication type: " + auth);
  }
}

