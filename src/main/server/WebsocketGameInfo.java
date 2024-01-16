package server;

import chess.ChessGame;
import chess.GameImple;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataAccess.DataAccessException;
import models.AuthToken;
import models.Game;
import org.eclipse.jetty.websocket.api.Session;
import services.GameService;
import webSocketMessages.serverMessages.ErrorMessage;
import webSocketMessages.serverMessages.LoadGameMessage;
import webSocketMessages.serverMessages.NotificationMessage;
import webSocketMessages.serverMessages.ServerMessage;
import webSocketMessages.userCommands.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class WebsocketGameInfo {
  private static WebsocketGameInfo instance;
  private final GameService gameService;
  private final HashMap<String, Connection> connections=new HashMap<>();
  private final HashMap<Integer, HashSet<Connection>> games=new HashMap<>();
  private final HashSet<Integer> finishedGames=new HashSet<>();

  private WebsocketGameInfo() {
    gameService=new GameService();
  }

  public static WebsocketGameInfo getInstance() {
    if (instance == null) instance=new WebsocketGameInfo();
    return instance;
  }

  public void clear() {
    connections.clear();
    games.clear();
    finishedGames.clear();
  }

  public void addConnection(Session session, AuthToken authToken, JoinPlayerMessage message) {
    var connection=new Connection(authToken, session, message.getGameID());
    var gameID=message.getGameID();
    var game=getGame(authToken, gameID);
    if (game == null) {
      sendMessage(new ErrorMessage("Error: no such game"), connection);
      return;
    }
    connections.put(authToken.authToken(), connection);
    if (!games.containsKey(gameID)) games.put(gameID, new HashSet<>());
    games.get(gameID).add(connection);

    if ((message.getPlayerColor() == ChessGame.TeamColor.WHITE && !authToken.username().equals(game.whiteUsername())) ||
            (message.getPlayerColor() == ChessGame.TeamColor.BLACK && !authToken.username().equals(game.blackUsername()))) {
      sendMessage(new ErrorMessage("Error: player slot already taken"), connection);
      return;
    }

    sendMessage(new LoadGameMessage((GameImple) game.game()), connection);
    broadcast(gameID, new NotificationMessage(authToken.username() + " has joined as the " + message.getPlayerColor() + " player."), authToken.authToken());
  }

  public void addConnection(Session session, AuthToken authToken, JoinObserverMessage message) {
    var connection=new Connection(authToken, session, message.getGameID());
    var gameID=message.getGameID();
    var game=getGame(authToken, gameID);
    if (game == null) {
      sendMessage(new ErrorMessage("Error: no such game"), connection);
      return;
    }
    connections.put(authToken.authToken(), connection);
    if (!games.containsKey(gameID)) games.put(gameID, new HashSet<>());
    games.get(gameID).add(connection);

    sendMessage(new LoadGameMessage((GameImple) game.game()), connection);
    broadcast(gameID, new NotificationMessage(authToken.username() + " has joined as an observer."), authToken.authToken());
  }

  public void makeMove(Session session, AuthToken authToken, MakeMoveMessage message) {
    var connection=getConnection(authToken);

    if (connection == null) {
      sendMessage(new ErrorMessage("Error: Cannot make move if you are not in a game!"), new Connection(authToken, session, 0));
      return;
    }

    var game=getGame(authToken, connection.gameID());

    if (!authToken.username().equals(game.blackUsername()) && !authToken.username().equals(game.whiteUsername())) {
      sendMessage(new ErrorMessage("Error: Cannot make move if you are not a player!"), new Connection(authToken, session, 0));
      return;
    }

    var currentPlayer=game.game().getTeamTurn() == ChessGame.TeamColor.WHITE ? game.whiteUsername() : game.blackUsername();

    if (!authToken.username().equals(currentPlayer)) {
      sendMessage(new ErrorMessage("Error: Cannot make move if it is not your turn!"), new Connection(authToken, session, 0));
      return;
    }

    if (finishedGames.contains(game.gameID())) {
      sendMessage(new ErrorMessage("Error: Cannot move after the completion of the game!"), new Connection(authToken, session, 0));
      return;
    }

    try {
      game.game().makeMove(message.getMove());
      gameService.updateGame(authToken, game);
    } catch (InvalidMoveException | DataAccessException e) {
      sendMessage(new ErrorMessage("Error: " + e.getMessage()), new Connection(authToken, session, 0));
      return;
    }

    var loadGame=new LoadGameMessage((GameImple) game.game());
    var moveMade=new NotificationMessage(authToken.username() + " moved " + message.getMove().toString());

    broadcast(game.gameID(), loadGame);
    broadcast(game.gameID(), moveMade, authToken.authToken());

    var g=game.game();

    if (g.isInStalemate(g.getTeamTurn())) {
      broadcast(game.gameID(), new NotificationMessage("The game is a stalemate!"));
      finishedGames.add(game.gameID());
    } else if (g.isInCheckmate(g.getTeamTurn())) {
      broadcast(game.gameID(), new NotificationMessage((g.getTeamTurn() == ChessGame.TeamColor.BLACK ? game.blackUsername() : game.whiteUsername()) + " has been checkmated!"));
      finishedGames.add(game.gameID());
    } else if (g.isInCheck(g.getTeamTurn()))
      broadcast(game.gameID(), new NotificationMessage((g.getTeamTurn() == ChessGame.TeamColor.BLACK ? game.blackUsername() : game.whiteUsername()) + " is in check!"));
  }

  public void leave(Session session, AuthToken authToken, LeaveMessage message) {
    var connection=getConnection(authToken);

    if (connection == null) {
      sendMessage(new ErrorMessage("Error: Cannot leave if you are not in a game!"), new Connection(authToken, session, 0));
      return;
    }

    connections.remove(authToken.authToken());
    games.get(message.getGameID()).remove(connection);
    if (session.isOpen()) session.close();

    broadcast(connection.gameID(), new NotificationMessage(authToken.username() + " has left the game."), authToken.authToken());
  }

  public void resign(Session session, AuthToken authToken, ResignMessage message) {
    var connection=getConnection(authToken);

    if (connection == null) {
      sendMessage(new ErrorMessage("Error: Cannot resign if you are not in a game!"), new Connection(authToken, session, 0));
      return;
    }

    var game=getGame(authToken, connection.gameID());

    if (!authToken.username().equals(game.blackUsername()) && !authToken.username().equals(game.whiteUsername())) {
      sendMessage(new ErrorMessage("Error: Cannot resign if you are not a player!"), new Connection(authToken, session, 0));
      return;
    }

    if (finishedGames.contains(game.gameID())) {
      sendMessage(new ErrorMessage("Error: Cannot resign after the completion of the game!"), new Connection(authToken, session, 0));
      return;
    }

    broadcast(connection.gameID(), new NotificationMessage(authToken.username() + " has resigned from the game."));
    connections.remove(authToken.authToken());
    games.get(message.getGameID()).remove(connection);
    if (session.isOpen()) session.close();
    finishedGames.add(connection.gameID());
  }

  public void broadcast(int gameID, ServerMessage message, String excludedAuthToken) {
    var gameConnections=games.get(gameID);

    var deadConnections=new ArrayList<Connection>();

    for (var connection : gameConnections) {
      boolean toSkip=false;
      if (!connection.session().isOpen()) {
        deadConnections.add(connection);
        toSkip=true;
      } else if (excludedAuthToken.equals(connection.authToken().authToken())) toSkip=true;

      if (toSkip) continue;

      sendMessage(message, connection);
    }

    for (var connection : deadConnections) {
      connections.remove(connection.authToken().authToken());
      gameConnections.remove(connection);
    }
  }

  public void sendMessage(ServerMessage message, Connection connection) {
    try {
      System.out.println("Message for " + connection.authToken().username() + ": " + message.toString());
      connection.session().getRemote().sendString(toJson(message));
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }

  public void broadcast(int gameID, ServerMessage message) {
    broadcast(gameID, message, "");
  }

  public Game getGame(AuthToken authToken, int gameID) {
    try {
      return gameService.getGame(authToken, gameID);
    } catch (DataAccessException ignored) {
      return null;
    }
  }

  public Connection getConnection(AuthToken authToken) {
    return connections.get(authToken.authToken());
  }

  private String toJson(Object o) {
    return new Gson().toJson(o);
  }
}
