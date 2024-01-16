package webSocketMessages.userCommands;

public class JoinObserverMessage extends UserGameCommand {
  private final int gameID;

  public JoinObserverMessage(String authToken, int gameID) {
    super(authToken);
    this.gameID=gameID;
    this.commandType=CommandType.JOIN_OBSERVER;
  }

  public int getGameID() {
    return gameID;
  }
}
