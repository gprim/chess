package services;

import dataAccess.DataAccessException;
import dataAccess.DatabaseAccess;
import dataAccess.MySqlDAO;
import models.AuthToken;
import models.Game;

import java.util.List;

public class GameService {
  DatabaseAccess dao;

  public GameService() {
    this.dao=MySqlDAO.getInstance();
  }

  public GameService(DatabaseAccess dao) {
    this.dao=dao;
  }

  /**
   * Lists all games
   *
   * @param authToken authorized token of user
   * @return A list of all games
   */
  public List<Game> listGames(AuthToken authToken) throws DataAccessException {
    return dao.listGames(authToken);
  }

  /**
   * Creates a new game
   *
   * @param authToken authorized token of user
   * @param game      game to create
   * @return a new empty game
   */
  public Game createGame(AuthToken authToken, Game game) throws DataAccessException {
    return dao.createGame(authToken, game);
  }

  /**
   * Joins a game
   *
   * @param gameToJoin Game to join. Username must be in the correct color slot or with throw error
   */
  public void joinGame(AuthToken authToken, Game gameToJoin) throws DataAccessException {
    dao.joinGame(authToken, gameToJoin);
  }

  public void updateGame(AuthToken authToken, Game gameToUpdate) throws DataAccessException {
    dao.updateGame(authToken, gameToUpdate);
  }

  public Game getGame(AuthToken authToken, int gameID) throws DataAccessException {
    return dao.getGame(authToken, gameID);
  }
}
