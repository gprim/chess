package services;

import dataAccess.DataAccessException;
import dataAccess.DatabaseAccess;

public class TestingService {
  DatabaseAccess dao;

  public TestingService(DatabaseAccess dao) {
    this.dao=dao;
  }

  /**
   * Clears all data from the server
   */
  public void clear() throws DataAccessException {
    dao.clear();
  }
}
