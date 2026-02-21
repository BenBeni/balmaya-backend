package com.balmaya.config;

import java.util.*;
import org.springframework.core.convert.converter.Converter;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/actuator/health").permitAll()
        .requestMatchers("/auth/login", "/auth/register", "/auth/otp", "/auth/otp/resend", "/auth/otp/validate").permitAll()
        .requestMatchers("/webhooks/stripe").permitAll()
        .requestMatchers("/admin/read/**").hasAnyRole("admin","assistant")
        .requestMatchers("/admin/write/**").hasRole("admin")
        .anyRequest().authenticated()
      )
      .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter())));
    return http.build();
  }

  @Bean
  Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthConverter() {
    return new Converter<Jwt, AbstractAuthenticationToken>() {
      @Override
      public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        Object realmAccess = jwt.getClaim("realm_access");
        if (realmAccess instanceof Map<?,?> map) {
          Object roles = map.get("roles");
          if (roles instanceof Collection<?> rs) {
            for (Object r : rs) authorities.add(new SimpleGrantedAuthority("ROLE_" + r));
          }
        }
        return new JwtAuthenticationToken(jwt, authorities);
      }
    };
  }
}

