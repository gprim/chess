package webSocketMessages.userCommands;

import chess.MoveImple;

public class MakeMoveMessage extends UserGameCommand {
  private final MoveImple move;

  public MakeMoveMessage(String authToken, MoveImple move) {
    super(authToken);
    this.commandType=CommandType.MAKE_MOVE;
    this.move=move;
  }

  public MoveImple getMove() {
    return move;
  }
}
