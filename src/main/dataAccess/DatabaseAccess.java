package dataAccess;

import models.AuthToken;
import models.Game;
import models.User;

import java.util.List;

public interface DatabaseAccess {
  /**
   * Clears the database
   *
   * @throws DataAccessException
   */
  void clear() throws DataAccessException;

  /**
   * Insert new user
   *
   * @param newUser user to insert
   * @return new auth token associated with the user
   * @throws DataAccessException
   */
  AuthToken insertUser(User newUser) throws DataAccessException;

  /**
   * Logs a user in
   *
   * @param user user to log in
   * @return new auth token associated with the user
   * @throws DataAccessException
   */
  AuthToken loginUser(User user) throws DataAccessException;

  /**
   * Log out a user
   *
   * @param authToken auth token associated with user to log out
   * @throws DataAccessException
   */
  void logoutUser(AuthToken authToken) throws DataAccessException;

  /**
   * List all games
   *
   * @param authToken Auth token of a registered user
   * @return List of all games
   * @throws DataAccessException
   */
  List<Game> listGames(AuthToken authToken) throws DataAccessException;

  /**
   * Creates a new game
   *
   * @param authToken Auth token of user creating the game
   * @param game      Game to create (name required)
   * @return new game stored in the database
   * @throws DataAccessException
   */
  Game createGame(AuthToken authToken, Game game) throws DataAccessException;

  /**
   * Join a game in progress
   *
   * @param authToken Auth token of a user to join
   * @param game      Game to join (game id required)
   * @throws DataAccessException
   */
  void joinGame(AuthToken authToken, Game game) throws DataAccessException;

  /**
   * Verifies a given AuthToken, and gives a full AuthToken back (has both username and authtoken)
   *
   * @param authToken Authtoken to verify
   * @return full AuthToken with username and authtoken, or null for unauthenticated
   * @throws DataAccessException
   */
  AuthToken verifyAuthToken(AuthToken authToken) throws DataAccessException;

  void updateGame(AuthToken authToken, Game game) throws DataAccessException;

  Game getGame(AuthToken authToken, int gameID) throws DataAccessException;
}
