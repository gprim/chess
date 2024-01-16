package chess;

import java.util.function.BiFunction;

public class BoardImple implements ChessBoard {
  ChessPiece[][] board=new ChessPiece[8][8];

  public static ChessBoard deserialize(String serializedGame) {
    ChessBoard board=new BoardImple();
    for (int x=0; x < 8; ++x) {
      for (int y=0; y < 8; ++y) {
        var piece=PieceImple.deserialize(serializedGame.charAt(x * 8 + y));
        board.addPiece(new PositionImple(x, y), piece);
      }
    }
    return board;
  }

  @Override
  public void addPiece(ChessPosition position, ChessPiece piece) {
    board[position.getRow()][position.getColumn()]=piece;
  }

  @Override
  public ChessPiece getPiece(ChessPosition position) {
    return board[position.getRow()][position.getColumn()];
  }

  @Override
  public void resetBoard() {
    // PieceFactory
    BiFunction<ChessGame.TeamColor, ChessPiece.PieceType, PieceImple> pf=
            PieceImple::new;
    var white=ChessGame.TeamColor.WHITE;
    var black=ChessGame.TeamColor.BLACK;
    var rook=ChessPiece.PieceType.ROOK;
    var knight=ChessPiece.PieceType.KNIGHT;
    var bishop=ChessPiece.PieceType.BISHOP;
    var king=ChessPiece.PieceType.KING;
    var queen=ChessPiece.PieceType.QUEEN;
    var pawn=ChessPiece.PieceType.PAWN;

    board=new ChessPiece[][]{
            {pf.apply(white, rook), pf.apply(white, knight), pf.apply(white, bishop), pf.apply(white, king), pf.apply(white, queen), pf.apply(white, bishop), pf.apply(white, knight), pf.apply(white, rook)},
            {pf.apply(white, pawn), pf.apply(white, pawn), pf.apply(white, pawn), pf.apply(white, pawn), pf.apply(white, pawn), pf.apply(white, pawn), pf.apply(white, pawn), pf.apply(white, pawn)},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {pf.apply(black, pawn), pf.apply(black, pawn), pf.apply(black, pawn), pf.apply(black, pawn), pf.apply(black, pawn), pf.apply(black, pawn), pf.apply(black, pawn), pf.apply(black, pawn)},
            {pf.apply(black, rook), pf.apply(black, knight), pf.apply(black, bishop), pf.apply(black, king), pf.apply(black, queen), pf.apply(black, bishop), pf.apply(black, knight), pf.apply(black, rook)},
    };
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (obj.getClass() != getClass()) return false;
    var otherBoard=(BoardImple) obj;
    for (int row=0; row < 8; ++row) {
      for (int col=0; col < 8; ++col) {
        var piece=board[row][col];
        var otherPiece=otherBoard.board[row][col];
        if (piece == null && otherPiece == null) continue;
        if (piece == null) return false;
        if (!piece.equals(otherPiece)) return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    var sb=new StringBuilder().append('|');
    for (int row=0; row < 8; ++row) {
      for (int col=0; col < 8; ++col) {
        var piece=board[row][col];
        sb.append(piece == null ? "." : piece).append('|');
      }
      if (row != 7) sb.append("\n|");
    }
    return sb.toString();
  }

  @Override
  public String serialize() {
    var sb=new StringBuilder();
    for (int row=0; row < 8; ++row) {
      for (int col=0; col < 8; ++col) {
        var piece=board[row][col];
        sb.append(piece == null ? "." : piece);
      }
    }
    return sb.toString();
  }
}
