package webSocketMessages.userCommands;

public class ResignMessage extends UserGameCommand {
  private final int gameID;

  public ResignMessage(String authToken, int gameID) {
    super(authToken);
    this.gameID=gameID;
    this.commandType=CommandType.RESIGN;
  }

  public int getGameID() {
    return gameID;
  }
}
