package services;

import dataAccess.DataAccessException;
import dataAccess.DatabaseAccess;
import models.AuthToken;
import models.User;

public class AuthService {
  DatabaseAccess dao;

  public AuthService(DatabaseAccess dao) {
    this.dao=dao;
  }

  /**
   * @param user Partial User with only username and password
   * @return Returns AuthToken associated with the user (if it exists)
   */
  public AuthToken login(User user) throws DataAccessException {
    return dao.loginUser(user);
  }

  /**
   * When provided with an AuthToken, logs a user out of their session
   *
   * @param authToken AuthToken associated with a user
   */
  public void logout(AuthToken authToken) throws DataAccessException {
    dao.logoutUser(authToken);
  }

  /**
   * Verifies authtoken
   *
   * @param authToken
   * @return either full authtoken or null
   * @throws DataAccessException
   */
  public AuthToken verifyAuthToken(AuthToken authToken) throws DataAccessException {
    return dao.verifyAuthToken(authToken);
  }
}
