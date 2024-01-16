package dataAccess;

/**
 * Used to store the config for logging into and accessing mysql server
 *
 * @param serverUrl
 * @param username
 * @param password
 * @param dbName
 */
public record MySqlDataAccessConfig(String serverUrl, String username, String password, String dbName) {
}
