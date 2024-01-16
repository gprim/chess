package models;

/**
 * Record to store User information
 *
 * @param username Username
 * @param password Password of the user
 * @param email    Email of the user
 */
public record User(String username, String password, String email) {
  /**
   * Create a user with only username and password. Used for Database lookups and AuthToken lookups
   *
   * @param username Username
   * @param password Password of the user
   */
  public User(String username, String password) {
    this(username, password, "");
  }
}
