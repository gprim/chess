package chess;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class GameImple implements ChessGame {
  TeamColor currentTeamTurn=TeamColor.WHITE;
  BoardImple board=new BoardImple();

  public static void main(String[] args) {
    var game=new GameImple();
    var move=new MoveImple(new PositionImple(1, 1), new PositionImple(1, 2), null);
    try {
      game.makeMove(move);
    } catch (InvalidMoveException e) {
    }
  }

  public static GameImple deserialize(String serializedGame, TeamColor currentTeamTurn) {
    var game=new GameImple();
    game.setTeamTurn(currentTeamTurn);
    game.setBoard(BoardImple.deserialize(serializedGame));
    return game;
  }

  @Override
  public TeamColor getTeamTurn() {
    return currentTeamTurn;
  }

  @Override
  public void setTeamTurn(TeamColor team) {
    currentTeamTurn=team;
  }

  @Override
  public Collection<ChessMove> validMoves(ChessPosition startPosition) {
    var piece=board.getPiece(startPosition);
    if (piece == null) return Collections.emptyList();

    return piece.pieceMoves(board, startPosition).stream().filter(move -> !moveCausesCheck(move)).collect(Collectors.toSet());
  }

  @Override
  public void makeMove(ChessMove move) throws InvalidMoveException {
    var pieceToMove=board.getPiece(move.getStartPosition());
    if (pieceToMove == null) throw new InvalidMoveException("Not a valid piece to move!");
    if (pieceToMove.getTeamColor() != currentTeamTurn) throw new InvalidMoveException("Not correct team color!");
    var placeToMove=board.getPiece(move.getEndPosition());
    if (placeToMove != null && pieceToMove.getTeamColor() == placeToMove.getTeamColor())
      throw new InvalidMoveException("Cannot capture friendly pieces!");
    var allMoves=pieceToMove.pieceMoves(board, move.getStartPosition());
    if (!allMoves.contains(move)) throw new InvalidMoveException("Not a valid move!");
    board.addPiece(move.getStartPosition(), null);
    if (move.getPromotionPiece() == null) board.addPiece(move.getEndPosition(), pieceToMove);
    else
      board.addPiece(move.getEndPosition(), new PieceImple(pieceToMove.getTeamColor(), move.getPromotionPiece()));
    if (isInCheck(currentTeamTurn)) {
      if (placeToMove == null) board.addPiece(move.getEndPosition(), null);
      else
        board.addPiece(move.getEndPosition(), new PieceImple(pieceToMove.getTeamColor(), placeToMove.getPieceType()));
      board.addPiece(move.getStartPosition(), new PieceImple(pieceToMove.getTeamColor(), pieceToMove.getPieceType()));
      throw new InvalidMoveException("Move would result in check!");
    }
    setTeamTurn(currentTeamTurn == TeamColor.BLACK ? TeamColor.WHITE : TeamColor.BLACK);
  }

  private boolean moveCausesCheck(ChessMove move) {
    var pieceToMove=board.getPiece(move.getStartPosition());
    var pieceAtEndPos=board.getPiece(move.getEndPosition());

    var startPos=move.getStartPosition();
    var endPos=move.getEndPosition();

    var teamColor=pieceToMove.getTeamColor();
    var newPieceType=move.getPromotionPiece() != null ? move.getPromotionPiece() : pieceToMove.getPieceType();
    var newPiece=new PieceImple(teamColor, newPieceType);

    board.addPiece(startPos, null);
    board.addPiece(endPos, newPiece);

    boolean causesCheck=isInCheck(teamColor);

    board.addPiece(startPos, pieceToMove);
    board.addPiece(endPos, pieceAtEndPos);

    return causesCheck;
  }

  private void getAllMoves(Set<ChessMove> blackMoves, Set<ChessMove> whiteMoves) {
    for (int row=0; row < 8; ++row) {
      for (int col=0; col < 8; ++col) {
        var pos=new PositionImple(row, col);
        var piece=board.getPiece(pos);
        if (piece == null) continue;
        if (piece.getTeamColor() == TeamColor.BLACK) blackMoves.addAll(piece.pieceMoves(board, pos));
        else whiteMoves.addAll(piece.pieceMoves(board, pos));
      }
    }
  }

  @Override
  public boolean isInCheck(TeamColor teamColor) {
    var moves=new HashSet<ChessPosition>();
    ChessPosition kingPos=new PositionImple(-1, -1);
    for (int row=0; row < 8; ++row) {
      for (int col=0; col < 8; ++col) {
        var pos=new PositionImple(row, col);
        var piece=board.getPiece(pos);
        if (piece == null) continue;
        if (piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING) kingPos=pos;
        if (piece.getTeamColor() == teamColor) continue;
        for (var move : piece.pieceMoves(board, pos)) moves.add(move.getEndPosition());
      }
    }
    return moves.contains(kingPos);
  }

  @Override
  public boolean isInCheckmate(TeamColor teamColor) {
    var blackMoves=new HashSet<ChessMove>();
    var whiteMoves=new HashSet<ChessMove>();
    getAllMoves(blackMoves, whiteMoves);
    var moves=teamColor == TeamColor.BLACK ? blackMoves : whiteMoves;
    for (var move : moves) {
      if (!moveCausesCheck(move)) return false;
    }
    return true;
  }

  @Override
  public boolean isInStalemate(TeamColor teamColor) {
    var kingsMoves=new HashSet<ChessMove>();
    var enemyMoves=new HashSet<ChessMove>();
    for (int row=0; row < 8; ++row) {
      for (int col=0; col < 8; ++col) {
        var pos=new PositionImple(row, col);
        var piece=board.getPiece(pos);
        if (piece == null) continue;
        if (piece.getTeamColor() == teamColor) {
          if (piece.getPieceType() == ChessPiece.PieceType.KING) kingsMoves.addAll(piece.pieceMoves(board, pos));
          else if (!piece.pieceMoves(board, pos).isEmpty()) return false;
        } else {
          enemyMoves.addAll(piece.pieceMoves(board, pos));
        }
      }
    }
    return kingsMoves.stream().filter(enemyMoves::contains).collect(Collectors.toSet()).isEmpty();
  }

  @Override
  public ChessBoard getBoard() {
    return board;
  }

  @Override
  public void setBoard(ChessBoard board) {
    this.board=(BoardImple) board;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (obj.getClass() != getClass()) return false;
    var otherGame=(GameImple) obj;
    return board.equals(otherGame.board);
  }

  @Override
  public String serialize() {
    return board.serialize();
  }

  @Override
  public String toString() {
    return board.toString();
  }
}
