package dataAccess;

import chess.GameImple;
import models.AuthToken;
import models.Game;
import models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MemoryDAO implements DatabaseAccess {
  private static MemoryDAO instance;
  int gameID=1000;
  HashMap<Integer, Game> games=new HashMap<>();
  HashMap<String, User> users=new HashMap<>();
  HashMap<String, AuthToken> authTokens=new HashMap<>();

  public static DatabaseAccess getInstance() {
    if (instance == null) instance=new MemoryDAO();
    return instance;
  }

  @Override
  public void clear() throws DataAccessException {
    games.clear();
    users.clear();
    authTokens.clear();
  }

  @Override
  public AuthToken insertUser(User newUser) throws DataAccessException {
    if (newUser == null) throw new DataAccessException("bad request");

    if (newUser.password() == null || newUser.username() == null || newUser.email() == null)
      throw new DataAccessException("bad request");
    var userToInsert=users.get(newUser.username());
    if (userToInsert != null) throw new DataAccessException("already taken");

    users.put(newUser.username(), newUser);

    return loginUser(newUser);
  }

  @Override
  public AuthToken loginUser(User user) throws DataAccessException {
    if (user == null) throw new DataAccessException("bad request");

    var userToLogin=users.get(user.username());
    if (userToLogin == null) throw new DataAccessException("unauthorized");
    if (!userToLogin.password().equals(user.password())) throw new DataAccessException("unauthorized");

    var authToken=new AuthToken(userToLogin.username());

    authTokens.put(authToken.authToken(), authToken);

    return authToken;
  }

  @Override
  public void logoutUser(AuthToken authToken) throws DataAccessException {
    if (authToken == null) throw new DataAccessException("unauthorized");

    var verifiedAuthToken=authTokens.get(authToken.authToken());
    if (verifiedAuthToken == null) throw new DataAccessException("unauthorized");
    authTokens.remove(verifiedAuthToken.authToken());
  }

  @Override
  public List<Game> listGames(AuthToken authToken) throws DataAccessException {
    if (authToken == null) throw new DataAccessException("unauthorized");

    var verifiedAuthToken=authTokens.get(authToken.authToken());
    if (verifiedAuthToken == null) throw new DataAccessException("unauthorized");
    return new ArrayList<>(games.values());
  }

  @Override
  public Game createGame(AuthToken authToken, Game game) throws DataAccessException {
    if (authToken == null) throw new DataAccessException("unauthorized");
    if (game == null) throw new DataAccessException("bad request");

    var verifiedAuthToken=authTokens.get(authToken.authToken());
    if (verifiedAuthToken == null) throw new DataAccessException("unauthorized");
    var newGame=new Game(++gameID, game.whiteUsername(), game.blackUsername(), game.gameName(), new GameImple());
    games.put(gameID, newGame);
    return newGame;
  }

  @Override
  public void joinGame(AuthToken authToken, Game game) throws DataAccessException {
    if (authToken == null) throw new DataAccessException("unauthorized");
    if (game == null) throw new DataAccessException("bad request");

    var verifiedAuthToken=authTokens.get(authToken.authToken());
    if (verifiedAuthToken == null) throw new DataAccessException("unauthorized");


    var username=verifiedAuthToken.username();

    var gameToJoin=games.get(game.gameID());

    if (gameToJoin == null) throw new DataAccessException("bad request");

    if (game.blackUsername() == null && game.whiteUsername() == null) {
      return;
    }

    var whiteUsername=gameToJoin.whiteUsername();
    var blackUsername=gameToJoin.blackUsername();
    var nullUserToReplace=game.blackUsername() != null ? blackUsername : whiteUsername;

    if (nullUserToReplace != null) throw new DataAccessException("already taken");

    if (game.whiteUsername() == null) blackUsername=username;
    else whiteUsername=username;

    var newGame=new Game(game.gameID(), whiteUsername, blackUsername, gameToJoin.gameName(), gameToJoin.game());

    games.put(game.gameID(), newGame);
  }

  @Override
  public AuthToken verifyAuthToken(AuthToken authToken) throws DataAccessException {
    if (!authTokens.containsKey(authToken.authToken())) return null;
    return authTokens.get(authToken.authToken());
  }

  @Override
  public void updateGame(AuthToken authToken, Game game) throws DataAccessException {
    
  }

  @Override
  public Game getGame(AuthToken authToken, int gameID) throws DataAccessException {
    return null;
  }
}
