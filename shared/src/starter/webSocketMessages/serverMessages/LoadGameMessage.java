package webSocketMessages.serverMessages;

import chess.ChessGame;
import chess.GameImple;

public class LoadGameMessage extends ServerMessage {
  private final String game;
  private final ChessGame.TeamColor currentTeam;

  public LoadGameMessage(GameImple game) {
    super(ServerMessageType.LOAD_GAME);
    this.game=game.serialize();
    currentTeam=game.getTeamTurn();
  }

  public GameImple getGame() {
    return GameImple.deserialize(game, currentTeam);
  }

  @Override
  public String toString() {
    return "LoadGameMessage{" +
            "game='" + game + '\'' +
            ", currentTeam=" + currentTeam +
            '}';
  }
}
