package com.balmaya.auth;

import com.balmaya.api.dto.AuthRegisterRequest;
import com.balmaya.api.dto.AuthRegisterResponse;
import com.balmaya.api.dto.AuthTokenResponse;
import com.balmaya.api.dto.UserProfileResponse;
import com.balmaya.config.KeycloakProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.*;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class KeycloakAuthService {
  private final RestTemplate keycloakRestTemplate;
  private final KeycloakProperties properties;
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public AuthTokenResponse login(String username, String password) {
    KeycloakProperties.ClientApp clientApp = properties.getClientApp();

    String url = tokenUrl(properties.getRealm());
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("grant_type", "password");
    form.add("client_id", clientApp.getClientId());
    addIfPresent(form, "client_secret", clientApp.getClientSecret());
    form.add("username", username);
    form.add("password", password);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    try {
      return keycloakRestTemplate.postForObject(url, new HttpEntity<>(form, headers), AuthTokenResponse.class);
    } catch (HttpStatusCodeException ex) {
      if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
      }
      throw new ResponseStatusException(ex.getStatusCode(), "Keycloak login failed");
    }
  }

  public KeycloakUserInfo userInfo(String accessToken) {
    String userId = subjectFromAccessToken(accessToken);
    if (userId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing subject in access token");
    }
    UserProfileResponse profile = getUserProfile(userId);
    KeycloakUserInfo info = new KeycloakUserInfo();
    info.setSub(profile.getUserId());
    info.setEmail(profile.getEmail());
    return info;
  }

  public AuthRegisterResponse register(AuthRegisterRequest request) {
    String adminToken = clientAppAccessToken();
    String url = adminUserUrl();

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("username", request.getUsername());
    body.put("email", request.getEmail());
    body.put("firstName", request.getFirstName());
    body.put("lastName", request.getLastName());
    body.put("attributes", Map.of("phoneNumber", List.of(request.getPhoneNumber())));
    body.put("enabled", true);
    body.put("emailVerified", false);
    body.put("credentials", List.of(Map.of(
      "type", "password",
      "value", request.getPassword(),
      "temporary", false
    )));

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(adminToken);
    headers.setContentType(MediaType.APPLICATION_JSON);

    try {
      ResponseEntity<Void> response = keycloakRestTemplate.postForEntity(url, new HttpEntity<>(body, headers), Void.class);
      String userId = extractUserId(response.getHeaders().getLocation());
      return new AuthRegisterResponse(userId);
    } catch (HttpStatusCodeException ex) {
      if (ex.getStatusCode() == HttpStatus.CONFLICT) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
      }
      throw new ResponseStatusException(ex.getStatusCode(), "Keycloak registration failed");
    }
  }

  public void updatePassword(String userId, String oldPassword, String newPassword) {
    if (oldPassword == null || oldPassword.isBlank() || newPassword == null || newPassword.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old and new password are required");
    }
    if (oldPassword.equals(newPassword)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be different from old password");
    }

    UserProfileResponse profile = getUserProfile(userId);
    if (profile.getUsername() == null || profile.getUsername().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to resolve username for password update");
    }

    try {
      login(profile.getUsername(), oldPassword);
    } catch (ResponseStatusException ex) {
      if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old password is incorrect");
      }
      throw ex;
    }

    String adminToken = clientAppAccessToken();
    String url = adminResetPasswordUrl(userId);

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("type", "password");
    body.put("value", newPassword);
    body.put("temporary", false);

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(adminToken);
    headers.setContentType(MediaType.APPLICATION_JSON);

    try {
      keycloakRestTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(body, headers), Void.class);
    } catch (HttpStatusCodeException ex) {
      throw new ResponseStatusException(ex.getStatusCode(), "Keycloak password update failed");
    }
  }

  public UserProfileResponse getUserProfile(String userId) {
    String adminToken = clientAppAccessToken();
    String url = adminUserUrl(userId);

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(adminToken);

    try {
      ResponseEntity<KeycloakUser> response = keycloakRestTemplate.exchange(
        url, HttpMethod.GET, new HttpEntity<>(headers), KeycloakUser.class
      );
      KeycloakUser user = response.getBody();
      if (user == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
      }
      return UserProfileResponse.builder()
        .userId(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .phoneNumber(extractPhoneNumber(user.getAttributes()))
        .build();
    } catch (HttpStatusCodeException ex) {
      if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
      }
      throw new ResponseStatusException(ex.getStatusCode(), "Keycloak user lookup failed");
    }
  }

  private String clientAppAccessToken() {
    KeycloakProperties.ClientApp clientApp = properties.getClientApp();
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();

    boolean hasClientSecret = clientApp.getClientSecret() != null && !clientApp.getClientSecret().isBlank();
    if (!hasClientSecret) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Keycloak client app credentials not configured");
    }

    form.add("grant_type", "client_credentials");
    form.add("client_id", clientApp.getClientId());
    form.add("client_secret", clientApp.getClientSecret());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    AuthTokenResponse token = keycloakRestTemplate.postForObject(
      tokenUrl(clientApp.getRealm()),
      new HttpEntity<>(form, headers),
      AuthTokenResponse.class
    );

    if (token == null || token.getAccessToken() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to obtain Keycloak admin token");
    }

    return token.getAccessToken();
  }

  private String tokenUrl(String realm) {
    return baseUrl() + "/realms/" + realm + "/protocol/openid-connect/token";
  }

  private String adminUserUrl() {
    return baseUrl() + "/admin/realms/" + properties.getRealm() + "/users";
  }

  private String adminUserUrl(String userId) {
    return adminUserUrl() + "/" + userId;
  }

  private String adminResetPasswordUrl(String userId) {
    return adminUserUrl(userId) + "/reset-password";
  }

  private String baseUrl() {
    return properties.getBaseUrl().replaceAll("/+$", "");
  }

  private void addIfPresent(MultiValueMap<String, String> form, String key, String value) {
    if (value != null && !value.isBlank()) form.add(key, value);
  }

  private String extractUserId(URI location) {
    if (location == null) return null;
    String path = location.getPath();
    int idx = path.lastIndexOf('/');
    return idx >= 0 ? path.substring(idx + 1) : null;
  }

  private String extractPhoneNumber(Map<String, List<String>> attributes) {
    if (attributes == null) return null;
    List<String> fromCamel = attributes.get("phoneNumber");
    if (fromCamel != null && !fromCamel.isEmpty()) return fromCamel.get(0);
    List<String> fromSnake = attributes.get("phone_number");
    if (fromSnake != null && !fromSnake.isEmpty()) return fromSnake.get(0);
    return null;
  }

  private String subjectFromAccessToken(String accessToken) {
    if (accessToken == null || accessToken.isBlank()) return null;
    try {
      String[] parts = accessToken.split("\\.");
      if (parts.length < 2) return null;
      byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
      Map<?, ?> payload = OBJECT_MAPPER.readValue(new String(decoded, StandardCharsets.UTF_8), Map.class);
      Object sub = payload.get("sub");
      return sub != null ? sub.toString() : null;
    } catch (Exception ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid access token");
    }
  }

  @Getter
  @Setter
  private static class KeycloakUser {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Map<String, List<String>> attributes;
  }

  @Getter
  @Setter
  public static class KeycloakUserInfo {
    private String sub;
    private String email;
  }
}
