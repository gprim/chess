package serverUnitTests;

import dataAccess.DataAccessException;
import dataAccess.DatabaseAccess;
import dataAccess.MemoryDAO;
import models.AuthToken;
import models.Game;
import models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import services.GameService;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {

  DatabaseAccess dao;
  User user;
  GameService gameService;

  @BeforeEach
  void prepTest() {
    user=new User("username", "password");
    dao=MemoryDAO.getInstance();
    gameService=new GameService(dao);
    try {
      dao.clear();
    } catch (Exception err) {
      System.out.println("Error: " + err.getMessage());
    }
  }

  @Test
  void listGamesSuccess() {
    var authToken=assertDoesNotThrow(() -> dao.insertUser(user));

    var gameToInsert=new Game(1234);

    var game=assertDoesNotThrow(() -> dao.createGame(authToken, gameToInsert));

    var games=assertDoesNotThrow(() -> gameService.listGames(authToken));

    assertTrue(games.contains(game));
  }

  @Test
  void listGamesUnauthorized() {
    var authToken=assertDoesNotThrow(() -> dao.insertUser(user));

    var gameToInsert=new Game(1234);

    var game=assertDoesNotThrow(() -> dao.createGame(authToken, gameToInsert));

    var unauthorizedToken=new AuthToken(user.username());

    var err=assertThrows(DataAccessException.class, () -> gameService.listGames(unauthorizedToken));

    assertEquals("unauthorized", err.getMessage());
  }

  @Test
  void createGameSuccess() {
    var authToken=assertDoesNotThrow(() -> dao.insertUser(user));

    var gameToInsert=new Game(1234);

    assertDoesNotThrow(() -> gameService.createGame(authToken, gameToInsert));
  }

  @Test
  void createGameUnauthorized() {
    var gameToInsert=new Game(1234);

    var err=assertThrows(DataAccessException.class, () -> gameService.createGame(new AuthToken(user.username()), gameToInsert));

    assertEquals("unauthorized", err.getMessage());
  }

  @Test
  void joinGameSuccess() {
    var authToken=assertDoesNotThrow(() -> dao.insertUser(user));

    var gameToInsert=new Game(1234, "white", null, "gameName", null);

    var game=assertDoesNotThrow(() -> dao.createGame(authToken, gameToInsert));

    assertDoesNotThrow(() -> gameService.joinGame(authToken, new Game(game.gameID(), null, user.username(), game.gameName(), null)));
  }

  @Test
  void joinGameUnauthorized() {
    var authToken=assertDoesNotThrow(() -> dao.insertUser(user));

    var gameToInsert=new Game(1234, null, "white", "gameName", null);

    var game=assertDoesNotThrow(() -> dao.createGame(authToken, gameToInsert));

    var err=assertThrows(DataAccessException.class, () -> gameService.joinGame(new AuthToken(user.username()), new Game(game.gameID(), null, user.username(), game.gameName(), null)));

    assertEquals("unauthorized", err.getMessage());
  }
}