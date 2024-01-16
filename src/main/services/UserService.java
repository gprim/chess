package services;

import dataAccess.DataAccessException;
import dataAccess.DatabaseAccess;
import models.AuthToken;
import models.User;

public class UserService {
  DatabaseAccess dao;

  public UserService(DatabaseAccess dao) {
    this.dao=dao;
  }

  /**
   * Registers a user
   *
   * @param user User to register
   * @return AuthToken associated with new user
   */
  public AuthToken registerUser(User user) throws DataAccessException {
    return dao.insertUser(user);
  }
}
