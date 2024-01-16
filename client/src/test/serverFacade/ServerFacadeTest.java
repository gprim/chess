package serverFacade;

import models.AuthToken;
import models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ui.ClientException;

import static org.junit.jupiter.api.Assertions.*;

class ServerFacadeTest {

  ServerFacade server=new ServerFacade();
  User user=new User("user", "pass", "email");

  @BeforeEach
  void setUp() {
    try {
      server.clear();
    } catch (ClientException e) {
      System.out.println(e.getMessage());
    }
  }

  @Test
  void clear() {
    assertDoesNotThrow(() -> server.clear());
  }

  @Test
  void registerUserSuccess() {
    var authToken=assertDoesNotThrow(() -> server.registerUser(user));
    assertNotNull(authToken);
    assertEquals(user.username(), authToken.username());
    assertNotNull(authToken.authToken());
  }

  @Test
  void registerUserAlreadyExists() {
    var authToken=assertDoesNotThrow(() -> server.registerUser(user));
    assertNotNull(authToken);
    assertEquals(user.username(), authToken.username());
    assertNotNull(authToken.authToken());
    var ex=assertThrows(ClientException.class, () -> server.registerUser(user));
    assertEquals("Error: already taken", ex.getMessage());
  }

  @Test
  void loginSuccess() {
    var authToken=assertDoesNotThrow(() -> server.registerUser(user));
    assertNotNull(authToken);
    assertEquals(user.username(), authToken.username());
    assertNotNull(authToken.authToken());

    var loginAuthToken=assertDoesNotThrow(() -> server.login(user));
    assertNotNull(loginAuthToken);
    assertEquals(user.username(), loginAuthToken.username());
    assertNotNull(loginAuthToken.authToken());

    assertNotEquals(authToken.authToken(), loginAuthToken.authToken());
  }

  @Test
  void loginUnauthorized() {
    var ex=assertThrows(ClientException.class, () -> server.login(user));
    assertEquals("Error: unauthorized", ex.getMessage());
  }

  @Test
  void logoutSuccess() {
    var authToken=assertDoesNotThrow(() -> server.registerUser(user));
    assertDoesNotThrow(() -> server.logout(authToken));
    var ex=assertThrows(ClientException.class, () -> server.listGames(authToken));
    assertEquals("Error: unauthorized", ex.getMessage());
  }

  @Test
  void logoutUnauthorized() {
    var authToken=new AuthToken("user");
    var ex=assertThrows(ClientException.class, () -> server.logout(authToken));

    assertEquals("Error: unauthorized", ex.getMessage());
  }

  @Test
  void listGamesSuccess() {
    var authToken=assertDoesNotThrow(() -> server.registerUser(user));
    assertNotNull(authToken);
    assertEquals(user.username(), authToken.username());
    assertNotNull(authToken.authToken());

    var game=assertDoesNotThrow(() -> server.createGame(authToken, "game"));
    assertNotNull(game);
    var games=assertDoesNotThrow(() -> server.listGames(authToken));
    assertNotNull(games);
    assertEquals(games.get(0).gameID(), game.gameID());
  }

  @Test
  void listGamesUnauthorized() {
    var authToken=new AuthToken("user");
    var ex=assertThrows(ClientException.class, () -> server.listGames(authToken));
    assertEquals("Error: unauthorized", ex.getMessage());
  }

  @Test
  void createGameSuccess() {
    var authToken=assertDoesNotThrow(() -> server.registerUser(user));
    var game=assertDoesNotThrow(() -> server.createGame(authToken, "game"));
    assertNotNull(game);
  }

  @Test
  void createGameUnauthorized() {
    var authToken=new AuthToken("user");
    var ex=assertThrows(ClientException.class, () -> server.createGame(authToken, "game"));
    assertEquals("Error: unauthorized", ex.getMessage());
  }

  @Test
  void joinGameSuccess() {
    var authToken=assertDoesNotThrow(() -> server.registerUser(user));
    var game=assertDoesNotThrow(() -> server.createGame(authToken, "game"));
    assertDoesNotThrow(() -> server.joinGame(authToken, game.gameID(), "WHITE"));
  }

  @Test
  void joinGameSlotTaken() {
    var authToken=assertDoesNotThrow(() -> server.registerUser(user));
    var game=assertDoesNotThrow(() -> server.createGame(authToken, "game"));
    assertDoesNotThrow(() -> server.joinGame(authToken, game.gameID(), "WHITE"));
    var ex=assertThrows(ClientException.class, () -> server.joinGame(authToken, game.gameID(), "WHITE"));
    assertEquals("Error: already taken", ex.getMessage());
  }
}
