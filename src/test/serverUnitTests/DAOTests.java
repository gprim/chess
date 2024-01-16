package serverUnitTests;

import dataAccess.DataAccessException;
import dataAccess.DatabaseAccess;
import dataAccess.MemoryDAO;
import dataAccess.MySqlDAO;
import models.AuthToken;
import models.Game;
import models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class DAOTests {
  User user;

  @BeforeEach
  void prepTest() {
    user=new User("username", "password", "email");
  }

  DatabaseAccess instantiateDatabase(Class<DatabaseAccess> dao) {
    try {
      var d=dao.getDeclaredConstructor().newInstance();
      d.clear();
      return d;
    } catch (Exception err) {
      System.out.println("Error: " + err.getMessage());
    }
    return null;
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAO.class, MySqlDAO.class})
  void clear(Class<DatabaseAccess> daoClass) {
    var dao=instantiateDatabase(daoClass);

    assertDoesNotThrow(dao::clear);
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAO.class, MySqlDAO.class})
  void insertUserSuccess(Class<DatabaseAccess> daoClass) {
    var dao=instantiateDatabase(daoClass);

    assertDoesNotThrow(() -> dao.insertUser(user));
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAO.class, MySqlDAO.class})
  void insertUserAlreadyExists(Class<DatabaseAccess> daoClass) {
    var dao=instantiateDatabase(daoClass);

    assertDoesNotThrow(() -> dao.insertUser(user));

    var err=assertThrows(DataAccessException.class, () -> dao.insertUser(user));

    assertEquals("already taken", err.getMessage());
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAO.class, MySqlDAO.class})
  void insertUserBadRequest(Class<DatabaseAccess> daoClass) {
    var dao=instantiateDatabase(daoClass);

    user=new User("username", null, "email");

    var err=assertThrows(DataAccessException.class, () -> dao.insertUser(user));

    assertEquals("bad request", err.getMessage());
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAO.class, MySqlDAO.class})
  void loginUserSuccess(Class<DatabaseAccess> daoClass) {
    var dao=instantiateDatabase(daoClass);

    assertDoesNotThrow(() -> dao.insertUser(user));

    var token=assertDoesNotThrow(() -> dao.loginUser(user));

    assertNotNull(token);
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAO.class, MySqlDAO.class})
  void loginUserNoSuchUser(Class<DatabaseAccess> daoClass) {
    var dao=instantiateDatabase(daoClass);

    var err=assertThrows(DataAccessException.class, () -> dao.loginUser(user));

    assertEquals("unauthorized", err.getMessage());
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAO.class, MySqlDAO.class})
  void loginUserUnauthorized(Class<DatabaseAccess> daoClass) {
    var dao=instantiateDatabase(daoClass);

    assertDoesNotThrow(() -> dao.insertUser(user));

    var wrongPasswordUser=new User(user.username(), user.password() + "wrong", user.email());

    var err=assertThrows(DataAccessException.class, () -> dao.loginUser(wrongPasswordUser));

    assertEquals("unauthorized", err.getMessage());
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAO.class, MySqlDAO.class})
  void logoutUserSuccess(Class<DatabaseAccess> daoClass) {
    var dao=instantiateDatabase(daoClass);

    var authToken=assertDoesNotThrow(() -> dao.insertUser(user));
    assertDoesNotThrow(() -> dao.logoutUser(authToken));
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAO.class, MySqlDAO.class})
  void logoutUserUnauthorized(Class<DatabaseAccess> daoClass) {
    var dao=instantiateDatabase(daoClass);

    var authToken=new AuthToken(user.username());

    var err=assertThrows(DataAccessException.class, () -> dao.logoutUser(authToken));

    assertEquals("unauthorized", err.getMessage());
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAO.class, MySqlDAO.class})
  void listGamesSuccess(Class<DatabaseAccess> daoClass) {
    var dao=instantiateDatabase(daoClass);

    var authToken=assertDoesNotThrow(() -> dao.insertUser(user));

    var gameToInsert=new Game(1234);

    var game=assertDoesNotThrow(() -> dao.createGame(authToken, gameToInsert));

    var games=assertDoesNotThrow(() -> dao.listGames(authToken));

    var containsGame=false;

    for (var returnedGame : games) {
      if (returnedGame.equals(game)) {
        containsGame=true;
        break;
      }
    }

    assertTrue(containsGame);
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAO.class, MySqlDAO.class})
  void listGamesUnauthorized(Class<DatabaseAccess> daoClass) {
    var dao=instantiateDatabase(daoClass);

    var authToken=assertDoesNotThrow(() -> dao.insertUser(user));

    var gameToInsert=new Game(1234);

    assertDoesNotThrow(() -> dao.createGame(authToken, gameToInsert));

    var unauthorizedToken=new AuthToken(user.username());

    var err=assertThrows(DataAccessException.class, () -> dao.listGames(unauthorizedToken));

    assertEquals("unauthorized", err.getMessage());
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAO.class, MySqlDAO.class})
  void createGameSuccess(Class<DatabaseAccess> daoClass) {
    var dao=instantiateDatabase(daoClass);

    var authToken=assertDoesNotThrow(() -> dao.insertUser(user));

    var gameToInsert=new Game(1234);

    assertDoesNotThrow(() -> dao.createGame(authToken, gameToInsert));
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAO.class, MySqlDAO.class})
  void createGameSuccessWithTwoGamesSameName(Class<DatabaseAccess> daoClass) {
    var dao=instantiateDatabase(daoClass);

    var authToken=assertDoesNotThrow(() -> dao.insertUser(user));

    var gameToInsert1=new Game(1234, "", "", "gameName", null);
    var gameToInsert2=new Game(1234, "", "", "gameName", null);

    assertDoesNotThrow(() -> dao.createGame(authToken, gameToInsert1));
    assertDoesNotThrow(() -> dao.createGame(authToken, gameToInsert2));
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAO.class, MySqlDAO.class})
  void createGameUnauthorized(Class<DatabaseAccess> daoClass) {
    var dao=instantiateDatabase(daoClass);

    var gameToInsert=new Game(1234);

    var err=assertThrows(DataAccessException.class, () -> dao.createGame(new AuthToken(user.username()), gameToInsert));

    assertEquals("unauthorized", err.getMessage());
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAO.class, MySqlDAO.class})
  void joinGameSuccess(Class<DatabaseAccess> daoClass) {
    var dao=instantiateDatabase(daoClass);

    var authToken=assertDoesNotThrow(() -> dao.insertUser(user));

    var gameToInsert=new Game(1234, "white", null, "gameName", null);

    var game=assertDoesNotThrow(() -> dao.createGame(authToken, gameToInsert));

    assertDoesNotThrow(() -> dao.joinGame(authToken, new Game(game.gameID(), null, user.username(), game.gameName(), null)));
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAO.class, MySqlDAO.class})
  void joinGameCantBeAlreadySelectedPlayer(Class<DatabaseAccess> daoClass) {
    var dao=instantiateDatabase(daoClass);

    var authToken=assertDoesNotThrow(() -> dao.insertUser(user));

    var gameToInsert=new Game(1234, null, "white", "gameName", null);

    var game=assertDoesNotThrow(() -> dao.createGame(authToken, gameToInsert));

    var err=assertThrows(DataAccessException.class, () -> dao.joinGame(authToken, new Game(game.gameID(), null, user.username(), game.gameName(), null)));

    assertEquals("already taken", err.getMessage());
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAO.class, MySqlDAO.class})
  void joinGameUnauthorized(Class<DatabaseAccess> daoClass) {
    var dao=instantiateDatabase(daoClass);

    var authToken=assertDoesNotThrow(() -> dao.insertUser(user));

    var gameToInsert=new Game(1234, null, "white", "gameName", null);

    var game=assertDoesNotThrow(() -> dao.createGame(authToken, gameToInsert));

    var err=assertThrows(DataAccessException.class, () -> dao.joinGame(new AuthToken(user.username()), new Game(game.gameID(), null, user.username(), game.gameName(), null)));

    assertEquals("unauthorized", err.getMessage());
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAO.class, MySqlDAO.class})
  void joinGameCantJoinFullGame(Class<DatabaseAccess> daoClass) {
    var dao=instantiateDatabase(daoClass);

    var authToken=assertDoesNotThrow(() -> dao.insertUser(user));

    var gameToInsert=new Game(1234, "black", "white", "gameName", null);

    var game=assertDoesNotThrow(() -> dao.createGame(authToken, gameToInsert));

    var err=assertThrows(DataAccessException.class, () -> dao.joinGame(authToken, new Game(game.gameID(), null, user.username(), game.gameName(), null)));

    assertEquals("already taken", err.getMessage());
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAO.class, MySqlDAO.class})
  void joinGameCantJoinGameThatDoesNotExist(Class<DatabaseAccess> daoClass) {
    var dao=instantiateDatabase(daoClass);

    var authToken=assertDoesNotThrow(() -> dao.insertUser(user));

    var gameToInsert=new Game(1234, null, "white", "gameName", null);

    var game=assertDoesNotThrow(() -> dao.createGame(authToken, gameToInsert));

    var err=assertThrows(DataAccessException.class, () -> dao.joinGame(authToken, new Game(game.gameID() + 1, null, user.username(), game.gameName(), null)));

    assertEquals("bad request", err.getMessage());
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAO.class, MySqlDAO.class})
  void joinGameObserver(Class<DatabaseAccess> daoClass) {
    var dao=instantiateDatabase(daoClass);

    var authToken=assertDoesNotThrow(() -> dao.insertUser(user));

    var gameToInsert=new Game(1234, null, null, "gameName", null);

    var game=assertDoesNotThrow(() -> dao.createGame(authToken, gameToInsert));

    assertDoesNotThrow(() -> dao.joinGame(authToken, new Game(game.gameID(), null, user.username(), game.gameName(), null)));
  }
}
