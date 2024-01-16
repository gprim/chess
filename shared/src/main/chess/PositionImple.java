package chess;

public class PositionImple implements ChessPosition {
  private static final char[] fileNames=new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
  private static final char[] rankNames=new char[]{'1', '2', '3', '4', '5', '6', '7', '8'};
  private final int row;
  private final int column;

  public PositionImple(int row, int column) {
    this.column=column;
    this.row=row;
  }

  @Override
  public int getRow() {
    return row;
  }

  @Override
  public int getColumn() {
    return column;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (obj.getClass() != getClass()) return false;
    var otherPos=(PositionImple) obj;
    return row == otherPos.row && column == otherPos.column;
  }

  @Override
  public int hashCode() {
    return row + column + row * column;
  }

  @Override
  public String toString() {
    return "(" + fileNames[column] + ',' + rankNames[row] + ')';
  }
}
