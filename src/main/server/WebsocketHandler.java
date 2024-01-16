package server;

import com.google.gson.Gson;
import dataAccess.DataAccessException;
import dataAccess.MySqlDAO;
import models.AuthToken;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import services.AuthService;
import webSocketMessages.serverMessages.ErrorMessage;
import webSocketMessages.userCommands.*;

@WebSocket
public class WebsocketHandler {
  WebsocketGameInfo gameInfo;
  AuthService authService;

  public WebsocketHandler() {
    authService=new AuthService(MySqlDAO.getInstance());
    gameInfo=WebsocketGameInfo.getInstance();
  }

  @OnWebSocketMessage
  public void onMessage(Session session, String message) {
    var genericCommand=new Gson().fromJson(message, UserGameCommand.class);

    var authToken=verifyAuthToken(genericCommand.getAuthString());

    if (authToken == null) {
      gameInfo.sendMessage(new ErrorMessage("Error: unauthorized"), new Connection(new AuthToken("Unknown"), session, 0));
      return;
    }

    switch (genericCommand.getCommandType()) {
      case JOIN_PLAYER -> gameInfo.addConnection(session, authToken, fromJson(message, JoinPlayerMessage.class));
      case JOIN_OBSERVER -> gameInfo.addConnection(session, authToken, fromJson(message, JoinObserverMessage.class));
      case MAKE_MOVE -> gameInfo.makeMove(session, authToken, fromJson(message, MakeMoveMessage.class));
      case LEAVE -> gameInfo.leave(session, authToken, fromJson(message, LeaveMessage.class));
      case RESIGN -> gameInfo.resign(session, authToken, fromJson(message, ResignMessage.class));
    }
  }

  private AuthToken verifyAuthToken(String authToken) {
    try {
      return authService.verifyAuthToken(new AuthToken(authToken, ""));
    } catch (DataAccessException e) {
      System.out.println(e.getMessage());
      return null;
    }
  }

  private <T> T fromJson(String json, Class<T> type) {
    return new Gson().fromJson(json, type);
  }
}
