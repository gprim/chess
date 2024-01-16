package chess;

import java.util.Collection;

public class PieceImple implements ChessPiece {
  ChessGame.TeamColor color;
  PieceType type;

  public PieceImple(ChessGame.TeamColor color, PieceType type) {
    this.color=color;
    this.type=type;
  }

  public static ChessPiece deserialize(char piece) {
    ChessPiece newPiece;

    switch (piece) {
      case 'r' -> newPiece=new PieceImple(ChessGame.TeamColor.BLACK, PieceType.ROOK);
      case 'n' -> newPiece=new PieceImple(ChessGame.TeamColor.BLACK, PieceType.KNIGHT);
      case 'b' -> newPiece=new PieceImple(ChessGame.TeamColor.BLACK, PieceType.BISHOP);
      case 'k' -> newPiece=new PieceImple(ChessGame.TeamColor.BLACK, PieceType.KING);
      case 'q' -> newPiece=new PieceImple(ChessGame.TeamColor.BLACK, PieceType.QUEEN);
      case 'p' -> newPiece=new PieceImple(ChessGame.TeamColor.BLACK, PieceType.PAWN);
      case 'R' -> newPiece=new PieceImple(ChessGame.TeamColor.WHITE, PieceType.ROOK);
      case 'N' -> newPiece=new PieceImple(ChessGame.TeamColor.WHITE, PieceType.KNIGHT);
      case 'B' -> newPiece=new PieceImple(ChessGame.TeamColor.WHITE, PieceType.BISHOP);
      case 'K' -> newPiece=new PieceImple(ChessGame.TeamColor.WHITE, PieceType.KING);
      case 'Q' -> newPiece=new PieceImple(ChessGame.TeamColor.WHITE, PieceType.QUEEN);
      case 'P' -> newPiece=new PieceImple(ChessGame.TeamColor.WHITE, PieceType.PAWN);
      default -> newPiece=null;
    }

    return newPiece;
  }

  @Override
  public ChessGame.TeamColor getTeamColor() {
    return color;
  }

  @Override
  public PieceType getPieceType() {
    return type;
  }

  @Override
  public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
    return MoveCalculator.getMoves(board, myPosition, this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (obj.getClass() != getClass()) return false;
    var otherPiece=(PieceImple) obj;
    return otherPiece.getPieceType() == type && otherPiece.getTeamColor() == color;
  }

  @Override
  public String toString() {
    int offset=color == ChessGame.TeamColor.BLACK ? 0 : 32;
    char letter='e';

    switch (type) {
      case ROOK -> letter='r';
      case KNIGHT -> letter='n';
      case BISHOP -> letter='b';
      case KING -> letter='k';
      case QUEEN -> letter='q';
      case PAWN -> letter='p';
    }

    return String.valueOf((char) (letter - offset));
  }
}
