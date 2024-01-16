package models;

import java.util.UUID;

/**
 * Contains AuthToken info
 *
 * @param authToken Generated uuid
 * @param username  Username of user
 */
public record AuthToken(String authToken, String username) {
  /**
   * Generates a new AuthToken when supplied with only the username
   *
   * @param username Username of the AuthToken bearer
   */
  public AuthToken(String username) {
    this(UUID.randomUUID().toString(), username);
  }
}
