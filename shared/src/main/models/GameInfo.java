package models;

public record GameInfo(int gameID, String whiteUsername, String blackUsername, String gameName) {
  static public GameInfo fromGame(Game game) {
    return new GameInfo(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName());
  }

  public Game toGame() {
    return new Game(gameID, whiteUsername, blackUsername, gameName, null);
  }
}
