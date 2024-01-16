package chess;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this interface, but you should not alter the existing
 * methods.
 */
public interface ChessPosition {
  /**
   * @return which row this position is in
   * 1 codes for the bottom row
   */
  int getRow();

  /**
   * @return which column this position is in
   * 1 codes for the left row
   */
  int getColumn();

  boolean equals(Object obj);

  int hashCode();

  String toString();
}
