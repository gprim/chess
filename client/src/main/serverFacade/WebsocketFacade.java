package serverFacade;

import chess.ChessGame;
import chess.MoveImple;
import com.google.gson.Gson;
import models.AuthToken;
import ui.ChessClient;
import ui.ClientException;
import webSocketMessages.serverMessages.ErrorMessage;
import webSocketMessages.serverMessages.LoadGameMessage;
import webSocketMessages.serverMessages.NotificationMessage;
import webSocketMessages.serverMessages.ServerMessage;
import webSocketMessages.userCommands.UserGameCommand;
import webSocketMessages.userCommands.UserMessageFactory;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebsocketFacade extends Endpoint {
  Session session;
  NotificationHandler notificationHandler;
  UserMessageFactory messageFactory;

  public WebsocketFacade(String url, NotificationHandler notificationHandler, AuthToken authToken, ChessClient client) throws ClientException {
    try {
      url=url.replace("http", "ws");
      var socketURI=new URI(url + "/connect");
      this.messageFactory=new UserMessageFactory(authToken);

      var container=ContainerProvider.getWebSocketContainer();
      this.session=container.connectToServer(this, socketURI);
      this.notificationHandler=notificationHandler;

      session.addMessageHandler(new MessageHandler.Whole<String>() {
        @Override
        public void onMessage(String message) {
          var serverMessage=new Gson().fromJson(message, ServerMessage.class);
          switch (serverMessage.getServerMessageType()) {
            case LOAD_GAME -> {
              var gameMessage=fromJson(message, LoadGameMessage.class);
              notificationHandler.notify(client.displayGame(gameMessage.getGame()));
            }
            case ERROR -> {
              var error=fromJson(message, ErrorMessage.class);
              notificationHandler.notify(error.getErrorMessage());
            }
            case NOTIFICATION -> {
              var notification=fromJson(message, NotificationMessage.class);
              notificationHandler.notify(notification.getMessage());
            }
          }
        }
      });

    } catch (URISyntaxException | DeploymentException | IOException e) {
      throw new ClientException(500, e.getMessage());
    }
  }

  private <T> T fromJson(String json, Class<T> type) {
    return new Gson().fromJson(json, type);
  }

  private String toJson(Object o) {
    return new Gson().toJson(o);
  }

  private void sendMessage(UserGameCommand message) throws ClientException {
    try {
      session.getBasicRemote().sendText(toJson(message));
    } catch (IOException e) {
      throw new ClientException(400, e.getMessage());
    }
  }

  public void close() throws ClientException {
    try {
      session.close();
    } catch (IOException e) {
      throw new ClientException(e.getMessage());
    }
  }

  public void joinPlayer(int gameID, ChessGame.TeamColor teamColor) throws ClientException {
    sendMessage(messageFactory.joinPlayer(gameID, teamColor));
  }

  public void joinObserver(int gameID) throws ClientException {
    sendMessage(messageFactory.joinObserver(gameID));
  }

  public void makeMove(MoveImple move) throws ClientException {
    sendMessage(messageFactory.makeMove(move));
  }

  public void leave(int gameID) throws ClientException {
    sendMessage(messageFactory.leave(gameID));
  }

  public void resign(int gameID) throws ClientException {
    sendMessage(messageFactory.resign(gameID));
  }

  @Override
  public void onOpen(Session session, EndpointConfig endpointConfig) {
  }
}
