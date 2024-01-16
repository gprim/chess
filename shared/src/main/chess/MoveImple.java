package chess;

public class MoveImple implements ChessMove {
  PositionImple startPos;
  PositionImple endPos;
  ChessPiece.PieceType promotionType;

  public MoveImple(ChessPosition startPos, ChessPosition endPos, ChessPiece.PieceType promotionType) {
    this.startPos=(PositionImple) startPos;
    this.endPos=(PositionImple) endPos;
    this.promotionType=promotionType;
  }

  public MoveImple(ChessPosition startPos, ChessPosition endPos) {
    this.startPos=(PositionImple) startPos;
    this.endPos=(PositionImple) endPos;
    this.promotionType=null;
  }

  @Override
  public ChessPosition getStartPosition() {
    return startPos;
  }

  @Override
  public ChessPosition getEndPosition() {
    return endPos;
  }

  @Override
  public ChessPiece.PieceType getPromotionPiece() {
    return promotionType;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (obj.getClass() != getClass()) return false;
    var otherMove=(MoveImple) obj;
    if (otherMove.promotionType != promotionType) return false;
    return startPos.equals(otherMove.startPos) && endPos.equals(otherMove.endPos);
  }

  @Override
  public int hashCode() {
    var startHash=startPos.hashCode();
    var endHash=endPos.hashCode();
    return startHash * endHash + startHash + endHash;
  }

  @Override
  public String toString() {
    var sb=new StringBuilder();
    sb.append(startPos).append("->").append(endPos);
    if (promotionType != null) sb.append(" (").append(promotionType).append(')');
    return sb.toString();
  }
}
