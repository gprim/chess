package webSocketMessages.userCommands;

public class LeaveMessage extends UserGameCommand {
  private final int gameID;

  public LeaveMessage(String authToken, int gameID) {
    super(authToken);
    this.gameID=gameID;
    this.commandType=CommandType.LEAVE;
  }

  public int getGameID() {
    return gameID;
  }
}
