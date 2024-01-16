package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

enum TakeStatus {CANT_TAKE, CAN_TAKE, MUST_TAKE}

public class MoveCalculator {
  private static final ChessPiece.PieceType[] promotions=new ChessPiece.PieceType[]{ChessPiece.PieceType.QUEEN, ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.ROOK};

  private MoveCalculator() {
  }

  public static Collection<ChessMove> getMoves(ChessBoard board, ChessPosition pos, ChessPiece piece) {
    var type=piece.getPieceType();
    MoveFunction moveFunction=null;
    switch (type) {
      case ROOK -> moveFunction=MoveCalculator::rookMoves;
      case KNIGHT -> moveFunction=MoveCalculator::knightMoves;
      case BISHOP -> moveFunction=MoveCalculator::bishopMoves;
      case KING -> moveFunction=MoveCalculator::kingMoves;
      case QUEEN -> moveFunction=MoveCalculator::queenMoves;
      case PAWN -> moveFunction=MoveCalculator::pawnMoves;
    }

    if (moveFunction == null) return new HashSet<ChessMove>();

    var moveManager=new MoveManager(board, piece, pos);

    return moveFunction.getMoves(moveManager);
  }

  private static Set<ChessMove> rookMoves(MoveManager moveManager) {
    var moves=moveManager.moves;
    var pos=moveManager.getStartPos();

    interface MoveChecker {
      boolean checkMove(int row, int col);
    }

    MoveChecker moveChecker=(int row, int col) -> {
      var newPos=moveManager.newPosition(row, col);

      if (moveManager.isValidMove(newPos)) moves.add(moveManager.newMove(newPos));

      // if there is a piece, then we need to tell it to break
      return moveManager.getPiece(newPos) != null;
    };

    for (int row=pos.getRow() + 1; row < 8; ++row) {
      if (moveChecker.checkMove(row, pos.getColumn())) break;
    }
    for (int row=pos.getRow() - 1; row >= 0; --row) {
      if (moveChecker.checkMove(row, pos.getColumn())) break;
    }
    for (int col=pos.getColumn() + 1; col < 8; ++col) {
      if (moveChecker.checkMove(pos.getRow(), col)) break;
    }
    for (int col=pos.getColumn() - 1; col >= 0; --col) {
      if (moveChecker.checkMove(pos.getRow(), col)) break;
    }
    return moves;
  }

  private static Set<ChessMove> knightMoves(MoveManager moveManager) {
    var moves=moveManager.moves;
    var pos=moveManager.getStartPos();
    var row=pos.getRow();
    var col=pos.getColumn();

    var positions=new int[][]{
            {row + 1, col - 2},
            {row + 2, col + 1},
            {row + 1, col + 2},
            {row + 2, col - 1},
            {row - 1, col - 2},
            {row - 2, col + 1},
            {row - 1, col + 2},
            {row - 2, col - 1},
    };

    for (int index=0; index < 8; ++index) {
      var row1=positions[index][0];
      var col1=positions[index][1];
      var newPos=moveManager.newPosition(row1, col1);
      if (moveManager.isValidMove(newPos)) moves.add(moveManager.newMove(newPos));
    }
    return moves;
  }

  private static Set<ChessMove> bishopMoves(MoveManager moveManager) {
    var moves=moveManager.moves;
    var pos=moveManager.getStartPos();

    interface MoveChecker {
      boolean checkMove(int row, int column);
    }

    MoveChecker moveChecker=(int row, int column) -> {
      var newPos=moveManager.newPosition(row, column);
      if (moveManager.isValidMove(newPos)) moves.add(moveManager.newMove(newPos));

      // if we hit a piece we need to break
      return column <= 0 || column >= 7 || moveManager.getPiece(newPos) != null;
    };

    var col=0;
    for (int row=pos.getRow() + 1; row < 8; ++row) {
      if (moveChecker.checkMove(row, pos.getColumn() + ++col)) break;
    }
    col=0;
    for (int row=pos.getRow() - 1; row >= 0; --row) {
      if (moveChecker.checkMove(row, pos.getColumn() + ++col)) break;
    }
    col=0;
    for (int row=pos.getRow() + 1; row < 8; ++row) {
      if (moveChecker.checkMove(row, pos.getColumn() - ++col)) break;
    }
    col=0;
    for (int row=pos.getRow() - 1; row >= 0; --row) {
      if (moveChecker.checkMove(row, pos.getColumn() - ++col)) break;
    }

    return moves;
  }

  private static Set<ChessMove> kingMoves(MoveManager moveManager) {
    var moves=moveManager.moves;
    var pos=moveManager.getStartPos();
    for (int row=-1; row < 2; ++row) {
      for (int col=-1; col < 2; ++col) {
        var newPos=moveManager.newPosition(pos.getRow() + row, pos.getColumn() + col);
        if (moveManager.isValidMove(newPos)) moves.add(moveManager.newMove(newPos));
      }
    }
    return moves;
  }

  private static Set<ChessMove> queenMoves(MoveManager moveManager) {
    var moves=new HashSet<ChessMove>();
    moves.addAll(rookMoves(moveManager));
    moves.addAll(bishopMoves(moveManager));
    return moves;
  }

  // white advances down, black advances up
  private static Set<ChessMove> pawnMoves(MoveManager moveManager) {
    int toMove;
    int notMovedRow;
    var pos=moveManager.getStartPos();
    var piece=moveManager.getPiece(pos);
    var moves=moveManager.moves;

    if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
      toMove=1;
      notMovedRow=1;
    } else {
      toMove=-1;
      notMovedRow=6;
    }

    var forward=moveManager.newPosition(pos.getRow() + toMove, pos.getColumn());
    var canMoveForward=moveManager.isValidMove(forward, TakeStatus.CANT_TAKE);

    // if can move directly forward
    if (canMoveForward) {
      // if directly forward would move to promotion row
      if (forward.getRow() == 0 || forward.getRow() == 7) {
        for (int i=0; i < 4; ++i) moves.add(moveManager.newMove(forward, promotions[i]));
      }
      // if move is normal move
      else moves.add(moveManager.newMove(forward));

      // if can do double move at beginning
      if (pos.getRow() == notMovedRow) {
        var doubleForward=moveManager.newPosition(pos.getRow() + toMove * 2, pos.getColumn());
        if (moveManager.isValidMove(doubleForward, TakeStatus.CANT_TAKE))
          moves.add(moveManager.newMove(doubleForward));
      }
    }

    // for taking pieces
    var left=moveManager.newPosition(pos.getRow() + toMove, pos.getColumn() - 1);
    var right=moveManager.newPosition(pos.getRow() + toMove, pos.getColumn() + 1);
    if (moveManager.isValidMove(left, TakeStatus.MUST_TAKE)) {
      if (left.getRow() == 0 || left.getRow() == 7) {
        for (int i=0; i < 4; ++i) moves.add(moveManager.newMove(left, promotions[i]));
      } else moves.add(moveManager.newMove(left));
    }
    if (moveManager.isValidMove(right, TakeStatus.MUST_TAKE)) {
      if (right.getRow() == 0 || right.getRow() == 7) {
        for (int i=0; i < 4; ++i) moves.add(moveManager.newMove(right, promotions[i]));
      } else moves.add(moveManager.newMove(right));
    }

    return moves;
  }

  interface MoveFactory {
    ChessMove newMove(ChessPosition pos);
  }

  interface MoveValidator {
    boolean isValidMove(ChessPosition pos);

    boolean isValidMove(ChessPosition pos, TakeStatus status);
  }

  interface MoveFunction {
    Set<ChessMove> getMoves(MoveManager moveManager);
  }

  private static class MoveManager {
    public final Set<ChessMove> moves=new HashSet<>();
    private final ChessBoard board;
    private final ChessPiece piece;
    private final ChessPosition startPos;

    MoveManager(ChessBoard board, ChessPiece piece, ChessPosition startPos) {
      this.board=board;
      this.piece=piece;
      this.startPos=startPos;
    }

    public ChessMove newMove(ChessPosition pos) {
      return new MoveImple(startPos, pos);
    }

    public ChessMove newMove(ChessPosition pos, ChessPiece.PieceType promotionType) {
      return new MoveImple(startPos, pos, promotionType);
    }

    public boolean isValidMove(ChessPosition newPos, TakeStatus status) {
      if (newPos.getColumn() > 7 || newPos.getColumn() < 0 || newPos.getRow() > 7 || newPos.getRow() < 0) return false;
      var toMove=board.getPiece(newPos);
      if (status == TakeStatus.MUST_TAKE && toMove == null) return false;
      if (status == TakeStatus.CANT_TAKE && toMove != null) return false;
      return toMove == null || toMove.getTeamColor() != piece.getTeamColor();
    }

    public boolean isValidMove(ChessPosition newPos) {
      return isValidMove(newPos, TakeStatus.CAN_TAKE);
    }

    public ChessPiece getPiece(ChessPosition pos) {
      return board.getPiece(pos);
    }

    public ChessPosition newPosition(int row, int col) {
      return new PositionImple(row, col);
    }

    public ChessPosition getStartPos() {
      return startPos;
    }
  }
}
