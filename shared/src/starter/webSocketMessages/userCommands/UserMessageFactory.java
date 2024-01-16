package webSocketMessages.userCommands;

import chess.ChessGame;
import chess.MoveImple;
import models.AuthToken;

public class UserMessageFactory {
  AuthToken authToken;

  public UserMessageFactory(AuthToken authToken) {
    this.authToken=authToken;
  }

  public JoinObserverMessage joinObserver(int gameID) {
    return new JoinObserverMessage(authToken.authToken(), gameID);
  }

  public JoinPlayerMessage joinPlayer(int gameID, ChessGame.TeamColor teamColor) {
    return new JoinPlayerMessage(authToken.authToken(), gameID, teamColor);
  }

  public MakeMoveMessage makeMove(MoveImple move) {
    return new MakeMoveMessage(authToken.authToken(), move);
  }

  public LeaveMessage leave(int gameID) {
    return new LeaveMessage(authToken.authToken(), gameID);
  }

  public ResignMessage resign(int gameID) {
    return new ResignMessage(authToken.authToken(), gameID);
  }
}
