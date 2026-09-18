package chess;

import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType pieceType;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.pieceType = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return pieceType;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        throw new RuntimeException("Not implemented");
    }

        private boolean inBounds(int row, int col) {
        return row >=1 && row <=8 && col >=1 && col <=8;
    }

    private boolean canMoveTo(ChessBoard board, int row, int col) {
        if (!inBounds(row,col)) {
            return false;
        }
        ChessPiece target = board.getPiece(new ChessPosition(row, col));
        return target == null || target.getTeamColor() != this.pieceColor;
    }

    private void jumpMove(ChessBoard board, ChessPosition start,List<ChessMove> moves, int[][] directions) {
        for (int[] direction : directions) {
            int row = start.getRow() + direction[0];
            int col = start.getColumn() + direction[1];

            if (canMoveTo(board, row, col)) {
                moves.add(new ChessMove(
                        start,
                        new ChessPosition(row, col),
                        null
                ));
            }
        }
    }

    private void lineMove(ChessBoard board, ChessPosition start, List<ChessMove> moves, int[][] directions) {
        for (int[] direction : directions) {
            int row = start.getRow() + direction[0];
            int col = start.getColumn() + direction[1];

            while (inBounds(row,col)) {
                ChessPiece target = board.getPiece(new ChessPosition(row, col));
                if (target == null) {
                    moves.add(new ChessMove(start, new ChessPosition(row, col), null));
                } else {
                    if (target.getTeamColor() != this.pieceColor) {
                        moves.add(new ChessMove(start, new ChessPosition(row, col), null));
                    }
                    break;
                }
                row += direction[0];
                col += direction[1];
            }
        }
    }

    private void pawnMove(ChessBoard board, ChessPosition start, List<ChessMove> moves) {
    }



    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ChessPiece other)) {
            return false;
        }
        return pieceColor == other.pieceColor && pieceType == other.pieceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, pieceType);
    }
}
